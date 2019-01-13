package lando.nsf.assembler;

import static lando.nsf.core6502.AddrMode.ABSOLUTE;
import static lando.nsf.core6502.AddrMode.ABSOLUTE_X;
import static lando.nsf.core6502.AddrMode.ABSOLUTE_Y;
import static lando.nsf.core6502.AddrMode.ACCUMULATOR;
import static lando.nsf.core6502.AddrMode.IMMEDIATE;
import static lando.nsf.core6502.AddrMode.IMPLIED;
import static lando.nsf.core6502.AddrMode.INDIRECT;
import static lando.nsf.core6502.AddrMode.INDIRECT_X;
import static lando.nsf.core6502.AddrMode.INDIRECT_Y;
import static lando.nsf.core6502.AddrMode.RELATIVE;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE_X;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE_Y;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;

import lando.nsf.ExecutableImage;
import lando.nsf.MemorySegment;
import lando.nsf.core6502.AddrMode;
import lando.nsf.core6502.Instruction;
import lando.nsf.core6502.Instructions;
import lando.nsf.core6502.OpCodeName;


/**
 * Takes a single list of lines and assemblers an executable image.
 */
public final class SimpleAssembler {
    
    private static final String DEFINE = "define";
    private static final String ADDR_LABEL_DELIM = ":";
    
    public static final int DEFAULT_START_ADDR = 0x0600;
    
    private final Map<String, Integer> defines = new HashMap<>();
    private final Map<String, Integer> addressLabels = new HashMap<>();
    
    private int programCounter;
    
    public AssemblerResults build(List<String> lines) {
        return build(DEFAULT_START_ADDR, lines);
    }

    /**
     * Any address deceleration (ie ORG commands) will override "startAddress".
     */
    public AssemblerResults build(int startAddress, List<String> lines) {
        Validate.notNull(lines);
        Validate.notEmpty(lines);
        Validate.noNullElements(lines);
        Validate.isTrue(startAddress >= 0 && startAddress < MemorySegment.MAX_LEN);
        
        LineReader reader = new LineReader();
        
        this.defines.clear();
        reader.read(lines, (line, lineNum) -> addAnyDefinedSymbol(line));
        
        this.programCounter = startAddress;
        this.addressLabels.clear();
        reader.read(lines, (line, lineNum) -> addAnyAddressLabel(line));
        
        //logSymbolsAndLabels();
        
        ExecutableImage img = new ExecutableImage();
        ExecImgBuilder bc2 = new ExecImgBuilder(img);
        InstrByteEmitter bc = new InstrByteEmitter(bc2);
        
        this.programCounter = startAddress;
        bc2.setAddress(startAddress);
        
        TreeMap<Integer, Integer> addrsToLineNum = new TreeMap<>();
        
        reader.read(lines, (line, lineNum) -> processLine(line, lineNum, addrsToLineNum, bc));
        
        bc2.flush();
        
        return new AssemblerResults(img, addrsToLineNum, addressLabels);
    }
    
    private void logSymbolsAndLabels() {
        logMap("defines", defines);
        logMap("addressLabels", addressLabels);
    }
    
    private static void logMap(String label, Map<String, Integer> map) {

        System.err.println();
        System.err.println("[" + label  + "]");
        
        if( map.isEmpty() ) {
            return;
        }
        
        int maxKeyLen = map.keySet().stream().mapToInt(key -> key.length()).max().getAsInt();
        
        map.keySet().stream().sorted().forEach(key -> {
            System.err.printf("%-"+maxKeyLen+"s => $%4x%n", key, map.get(key));
        });
    }
    
    private String[] tokenize(String line) {
        String[] tokens = line.split("\\s+");
        Validate.isTrue(tokens.length > 0);
        
        return tokens;
    }
    
    private void addAnyDefinedSymbol(String line) {
        line = stripComments(line);
        line = retabAndTrim(line);
        
        if( line.isEmpty() ) {
            return;
        }
        
        String[] tokens = tokenize(line);
        
        if( isDefine(tokens) ) {
            Validate.isTrue(tokens.length == 3, "defines shoulds only have 3 lines");
            
            defineValue(tokens[1], tokens[2]);
        }        
    }
    
    /**
     * You're allowed to reference labels ahead of the current line
     * so we have to collect them in a first pass so the labels can be 
     * resolved like this:
     * JMP loop
     * ...
     * loop:
     * ADC ...
     * ROR ...
     */
    private void addAnyAddressLabel(String line) {
        line = stripComments(line);
        line = retabAndTrim(line);
        
        if( line.isEmpty() ) {
            return;
        }
        
        String[] tokens = tokenize(line);

        if( isDefine(tokens) ) {
            return;
        }
        
        if( hasAddrLabel(tokens) ) {
            saveAddressLabel(tokens[0]);
            tokens = shift(tokens);
            
            if( tokens.length == 0 ) {
                return;  //line was only an address label
            }
        }
        
        OpCodeName name = OpCodeName.valueOf(tokens[0].toUpperCase());
        Validate.notNull(name);
        
        Map<AddrMode, Instruction> instrs = Instructions.BY_NAME_AND_ADDR_MODE.get(name);
        Validate.notNull(instrs);
               
        if( isSingleByteDecleration(tokens) ) {
            advancePC(IMPLIED);
            return;
        }
        
        Validate.isTrue(tokens.length == 2 || tokens.length == 3);
        
        if( tokens[1].startsWith("#")) {
            advancePC(IMMEDIATE);
            return;
        }
        
        Validate.isTrue(tokens.length == 2);
        
        boolean hasComma  = tokens[1].indexOf(',') != -1;
        boolean hasParens = tokens[1].indexOf('(') != -1;
        
        if( ! hasComma && ! hasParens ) {
            //absolute, relative or zero page!
            int val = readValWithoutLabel(tokens[1]);
            int high = (val >> 8) & 255;
            
            if( high == 0 && instrs.containsKey(ZERO_PAGE)) {
                advancePC(ZERO_PAGE);
                return;
            } 
            
            if( instrs.containsKey(ABSOLUTE)) {
                advancePC(ABSOLUTE);
                return;
            } 
            
            if( instrs.containsKey(RELATIVE) ) {
                advancePC(RELATIVE);
                return;
            }
             
            throw new RuntimeException("Unknown addressing mode");
        } 
        
        if( hasComma && ! hasParens ) {
            String arg = tokens[1];
            
            boolean isX = arg.toUpperCase().endsWith(",X");
            boolean isY = arg.toUpperCase().endsWith(",Y");
            
            if( ! isX && ! isY ) {
                throw new RuntimeException("Unknown zero-page or absolute indexing mode.");
            }

            //strip off trailing ",X" or ",Y"
            arg = arg.substring(0, arg.length() - 2);
            
            int val = readValWithoutLabel(arg);            
            int high = (val >> 8) & 255;

            boolean hasHigh = high != 0;
            
            AddrMode addrMode;
            
            if( hasHigh ) {
                addrMode = isX ? ABSOLUTE_X : ABSOLUTE_Y;
            }
            
            else {
                addrMode = isX ? ZERO_PAGE_X : ZERO_PAGE_Y;
            }
            
            advancePC(addrMode);
            return;
        } 
        
        if( ! hasComma && hasParens ) {
            advancePC(INDIRECT);
            return;
        }
        
        if( hasComma && hasParens ) {
            advancePC(INDIRECT_X);
            return;
        }
        
        throw new IllegalStateException("Impossibru");
    }
    
    private void advancePC(AddrMode addrMode) {
        programCounter += addrMode.instrLen;
    }
    
    private static boolean isDefine(String[] tokens) {
        return DEFINE.equals(tokens[0]);
    }
    
    private static boolean hasAddrLabel(String [] tokens) {
        return tokens[0].endsWith(ADDR_LABEL_DELIM);
    }
    
    private void processLine(
            String line, 
            int lineNum, 
            SortedMap<Integer, Integer> addrsToLineNum, 
            InstrByteEmitter emitter) {
        
        line = stripComments(line);
        line = retabAndTrim(line);
        
        if( line.isEmpty() ) {
            return;
        }
                
        String[] tokens = tokenize(line);
        
        if( isDefine(tokens) ) {
            return;
        }
        
        if( hasAddrLabel(tokens) ) {
            tokens = shift(tokens);
            
            if( tokens.length == 0 ) {
                return;  //line was only an address label
            }
        }
        
        //I have a legit instruction!
        addrsToLineNum.put(programCounter, lineNum);
        
        OpCodeName name = OpCodeName.valueOf(tokens[0].toUpperCase());
        Validate.notNull(name);
        
        Map<AddrMode, Instruction> instrs = Instructions.BY_NAME_AND_ADDR_MODE.get(name);
        Validate.notNull(instrs);
       
        if( isSingleByteDecleration(tokens) ) {
            readZeroArgInstr(name, instrs, emitter);
            return;
        }
        
        Validate.isTrue(tokens.length == 2 || tokens.length == 3);
        
        if( tokens[1].startsWith("#")) {
            readImmediateModeInstr(name, tokens, instrs, emitter);
            return;
        }
        
        Validate.isTrue(tokens.length == 2);
        
        boolean hasComma  = tokens[1].indexOf(',') != -1;
        boolean hasParens = tokens[1].indexOf('(') != -1;
        
        if( ! hasComma && ! hasParens ) {
            //absolute, relative or zero page!
            int val = readVal(tokens[1]);
            int low = val & 255;
            int high = (val >> 8) & 255;
            
            if( high == 0 && instrs.containsKey(ZERO_PAGE)) {
                emitter.emit(instrs.get(ZERO_PAGE), low);
                advancePC(ZERO_PAGE);
                return;
            } 
            
            if( instrs.containsKey(ABSOLUTE)) {
                emitter.emit(instrs.get(ABSOLUTE), low, high);
                advancePC(ABSOLUTE);
                return;
            } 
            
            if( instrs.containsKey(RELATIVE) ) {
                //val is target
                int offset = val - (programCounter + RELATIVE.instrLen);
                
                if( offset < Byte.MIN_VALUE || offset > Byte.MAX_VALUE ) {
                    throw new RuntimeException("Out of range branch.");
                }
                
                emitter.emit(instrs.get(RELATIVE), offset);
                advancePC(RELATIVE);
                return;
            }
            
            throw new RuntimeException("Unknown addressing mode");
        }
        
        if( hasComma && ! hasParens ) {

            String arg = tokens[1];
            
            boolean isX = arg.toUpperCase().endsWith(",X");
            boolean isY = arg.toUpperCase().endsWith(",Y");
            
            if( ! isX && ! isY ) {
                throw new RuntimeException("Unknown zero-page or absolute indexing mode.");
            }

            //strip off trailing ",X" or ",Y"
            arg = arg.substring(0, arg.length() - 2);
            
            int val = readVal(arg);
            
            int low = val & 255;
            int high = (val >> 8) & 255;

            boolean hasHigh = high != 0;
            
            AddrMode addrMode;
            
            if( hasHigh ) {
                addrMode = isX ? ABSOLUTE_X : ABSOLUTE_Y;
            }
            
            else {
                addrMode = isX ? ZERO_PAGE_X : ZERO_PAGE_Y;
            }
            
            Instruction instr = instrs.get(addrMode);
            Validate.notNull(instr, addrMode + " not supported for instruction.");
            
            if( hasHigh ) {
                emitter.emit(instr, low, high);
            } else {
                emitter.emit(instr, low);
            }
            
            advancePC(instr.addrMode);
            return;
        } 
        
        if( ! hasComma && hasParens ) {
            Instruction instr = instrs.get(INDIRECT);
            Validate.notNull(instr, "INDIRECT addr mode not supported");
            
            String arg = tokens[1].substring(1, tokens[1].length() - 1); //remove parens
            int val = readVal(arg);
            int low = val & 255;
            int high = (val >> 8) & 255;

            emitter.emit(instr, low, high);
            advancePC(INDIRECT);
            return;
        }
        
        if( hasComma && hasParens ) {
            
            Instruction instr;
            
            if( tokens[1].toUpperCase().endsWith(",X)")) {
                instr = instrs.get(INDIRECT_X);
            }
            
            else if( tokens[1].toUpperCase().endsWith("),Y")) {
                instr = instrs.get(INDIRECT_Y);
            }
            
            else {
                throw new RuntimeException("Unknown indirect addr mode.");
            }
            
            Validate.notNull(instr, "Indirect address mode not supported");

            int end = Math.min(tokens[1].indexOf(','), tokens[1].indexOf(')'));
            Validate.isTrue(end >= 0, "Bad arg formatting");
            
            String arg = tokens[1].substring(1, end);
            int val = readVal(arg);
            
            emitter.emit(instr, val);
            advancePC(instr.addrMode);
            return;
        }
        
        throw new IllegalStateException("Impossibru");
    }
    
    private static boolean isSingleByteDecleration(String[] tokens) {
        return tokens.length == 1 || tokens.length == 2 && "A".equals(tokens[1].toUpperCase());
    }
    
    private void readImmediateModeInstr(OpCodeName name, String[] tokens, Map<AddrMode, Instruction> instrs, InstrByteEmitter emitter) {
        
        Instruction instr = instrs.get(IMMEDIATE);
        Validate.notNull(instr, "No immediate instr");
        
        String arg = tokens[1].substring(1);
        Validate.isTrue(! arg.isEmpty());
        
        boolean takeMSB = false;
        
        if( "LO".equals(arg.toUpperCase())) {
            arg = tokens[2];
        }
        
        if( "HI".equals(arg.toUpperCase())) {
            takeMSB = true;
            arg = tokens[2];
        }
            
        int val = readVal(arg);
        
        if( takeMSB ) {
            val >>= 8;
        }
        
        emitter.emit(instr, val);
        advancePC(instr.addrMode);
    }
    
    private int readVal(String arg) {
        if( '$' == arg.charAt(0) || Character.isDigit(arg.charAt(0)) ) {
            return readLiteral(arg);
        } 
        
        if( addressLabels.containsKey(arg) ) {
            return addressLabels.get(arg);
        }
        
        if( defines.containsKey(arg) ) {
            return defines.get(arg);
        }
        
        throw new RuntimeException("Unknown symbol " + arg);
    }
    
    private int readValWithoutLabel(String arg) {
        
        if( '$' == arg.charAt(0) || Character.isDigit(arg.charAt(0)) ) {
            return readLiteral(arg);
        }
        
        if( defines.containsKey(arg) ) {
            return defines.get(arg);
        }
        
        //force unresolved address labels to be 16 bits. 
        //don't know if this is correct
        return 0xFFFF;
    }

        
    private void readZeroArgInstr(OpCodeName name, Map<AddrMode, Instruction> instrs, InstrByteEmitter emitter) {
        /* I have no further args. 
         * verify that there is only 1 option
         * and it is IMPLIED or ACCUMULATOR address mode.
         */
        Instruction implied = instrs.get(IMPLIED);
        Instruction accumulator = instrs.get(ACCUMULATOR);
        
        if( implied == null && accumulator == null ) {
            throw new RuntimeException("No implied or accumulator addr-mode instruction to choose from.");
        }
        
        if( implied != null && accumulator != null ) {
            throw new RuntimeException("Have both implied and accumulator addr-mode instruction to choose from.");
        }
                
        Instruction instr = implied != null ? implied : accumulator;
        emitter.emit(instr);     
        advancePC(instr.addrMode);
    }

    private void defineValue(String defineName, String defineValueStr) {
        int defineValue = readLiteral(defineValueStr);
        
        if( defineValue < Byte.MIN_VALUE || defineValue > 255 ) {
            throw new IllegalArgumentException("Can only define values between [" + Byte.MIN_VALUE + ", " + Byte.MAX_VALUE + "]");
        }
        
        Integer prevValue = defines.put(defineName, defineValue);
        
        if( prevValue != null ) {
            System.err.println("Warning overwriting define " + defineName + " from " + prevValue + " to " + defineValue);
        }
    }
    
    private void saveAddressLabel(String label) {
        Validate.isTrue(label.endsWith(ADDR_LABEL_DELIM));
        
        label = label.substring(0, label.length() - ADDR_LABEL_DELIM.length());
        
        Integer prevValue = addressLabels.put(label, programCounter);
        
        if( prevValue != null ) {
            System.err.println("Warning overwriting address label " + label + " from " + prevValue + " to " + programCounter);
        }
    }
    
    private static String[] shift(String[] array) {
        String[] shifted = new String[array.length - 1];
        
        for(int i = 1; i < array.length; i++) {
            shifted[i - 1] = array[i];
        }
        
        return shifted;
    }
    
    private static int readLiteral(String lit) {
        if( lit.startsWith("$") ) {
            return Integer.parseInt(lit.substring(1), 16);
        }
        
        return Integer.parseInt(lit, 10);
    }
    
    private static String stripComments(String line) {
        int index = line.indexOf(';');
        
        if( index == -1 ) { //no comment
            return line;
        }
        
        return line.substring(0, index);
    }
    
    private static String retabAndTrim(String line) {
        return line.replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim();
    }
}
