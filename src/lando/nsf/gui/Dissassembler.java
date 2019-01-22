package lando.nsf.gui;

import static lando.nsf.DisassemblerUtils.opCodeText;
import static lando.nsf.cpu.StringUtils.toBin8;
import static lando.nsf.cpu.StringUtils.toHex2;
import static lando.nsf.cpu.StringUtils.toHex4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lando.nsf.HexUtils;
import lando.nsf.cpu.ByteArrayMem;
import lando.nsf.cpu.CPU;
import lando.nsf.cpu.Instruction;
import lando.nsf.cpu.Instructions;

public final class Dissassembler {

    private final CPU cpu;
    private final Supplier<byte[]> mem;
    private final List<String> asm;
    private final AtomicInteger numInstrs;
    private final TreeMap<Integer, Integer> addrsToLines;
    private final TreeMap<Integer, Integer> linesToAddrs;
    private final Map<Integer, String> addrsToLabels;
    
    public Dissassembler(
            CPU cpu, 
            Supplier<byte[]> mem, 
            List<String> asm, 
            AtomicInteger numInstrs,
            TreeMap<Integer, Integer> addrsToLines, 
            Map<String, Integer> labelsToAddrs) {
                
        this.cpu = Objects.requireNonNull(cpu);
        this.mem = Objects.requireNonNull(mem);
        this.asm = Objects.requireNonNull(asm);
        this.numInstrs = Objects.requireNonNull(numInstrs);
        this.addrsToLines = Objects.requireNonNull(addrsToLines);
        
        this.linesToAddrs = new TreeMap<>(
                addrsToLines.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getValue(), 
                            e -> e.getKey())));
        
        this.addrsToLabels = 
                labelsToAddrs.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getValue(), 
                            e -> e.getKey()));
    }
    
    public List<TextLine> currStatus() {
        List<TextLine> lines = new ArrayList<>();
        
        lines.add(new TextLine("num-instrs=" + numInstrs.get()));
        
        lines.add(new TextLine("A=" + toHex2(cpu.A)));
        lines.add(new TextLine("X=" + toHex2(cpu.X)));
        lines.add(new TextLine("Y=" + toHex2(cpu.Y)));
        lines.add(new TextLine("S=" + toHex2(cpu.S)));
        lines.add(new TextLine("PC=" + toHex4(cpu.PC)));
        lines.add(new TextLine("NV-BDIZC"));
        lines.add(new TextLine(toBin8(cpu.P)));
        
        lines.add(new TextLine(""));
                
        printCode(lines);
        
        return lines;
    }
    
    private void printCode(List<TextLine> lines) {
        
        int addr = getPrintStartAddr();
        int numInstrsToPrint = 10;
        AtomicInteger pc = new AtomicInteger(addr);
        Supplier<Byte> nextByte = () -> mem.get()[pc.getAndIncrement()];
        
        for(int i = 0; i < numInstrsToPrint; i++) {
            
            if( pc.get() < mem.get().length ) {
                if( addrsToLabels.containsKey(pc.get())) {
                    lines.add(new TextLine(""));
                    lines.add(new TextLine(addrsToLabels.get(pc.get()) + ":"));
                }
                
                lines.add(dissassemble(pc.get(), nextByte));
            }
        }
    }
    
    private int getPrintStartAddr() {
        Optional<Integer> lineNum;

        if( addrsToLines.containsKey(cpu.PC)) {
            lineNum = Optional.of(addrsToLines.get(cpu.PC));
        } else if( ! addrsToLines.isEmpty() && cpu.PC > addrsToLines.firstKey() && cpu.PC < addrsToLines.lastKey() ) {
            lineNum = Optional.of(addrsToLines.floorEntry(cpu.PC).getValue());
        } else {
            lineNum = Optional.empty();
        }
        
        int addr;
        
        if( lineNum.isPresent() ) {
            int n = lineNum.get()/10*10;            
            Map.Entry<Integer, Integer> m = linesToAddrs.ceilingEntry(n);
            
            if( m != null ) {
                addr = m.getValue();
            } else {
                addr = cpu.PC;
            }
        } else {
            addr = cpu.PC;
        }

        return addr;
    }
    
    private TextLine dissassemble(final int pc, Supplier<Byte> nextByte) {
        
        StringBuilder line = new StringBuilder();
        
        boolean highlighted;
        
        if( pc == cpu.PC ) {
            highlighted = true;
            line.append(" =>");
        } else {
            highlighted = false;
            line.append("   ");
        }
        
        line.append(HexUtils.toHex16(pc));
        line.append(": ");
        
        int opCode = nextByte.get();
        Instruction instr = Instructions.BY_OP_CODE[opCode & 0xFF];
        
        if( instr != null ) {
            switch(instr.addrMode.instrLen) {
            
            case 1: line.append(opCodeText(
                            instr,
                            0,
                            0));
                    break;
                    
            case 2: line.append(opCodeText(
                            instr, 
                            nextByte.get(), 
                            0)); 
                    break;
                    
            case 3: line.append(opCodeText(
                            instr, 
                            nextByte.get(), 
                            nextByte.get())); 
                    break;
            
            default: 
                throw new RuntimeException("unknown instr " + instr);
            }
            
        } else {
            line.append(toHex2(opCode));
            line.append(" ; unknown op-code");
        }
        
        if( addrsToLines.containsKey(pc) ) {
            //append file listing as well
            int asmLineNum = addrsToLines.get(pc);
            String asmLine = asm.get(asmLineNum - 1);
            int desiredWidth = 24;
            
            if( line.length() < desiredWidth ) {
                rep(line, " ", desiredWidth - line.length());
            }
            
            line.append(String.format("%5d: %s", asmLineNum, asmLine));
        }
        
        String txt = line.toString();
        TextLine tl = new TextLine(txt);
        
        if( highlighted ) {
            tl.addHighlightedSection(new LineSection(0, txt.length(), 0));
        }
        
        return tl;
    }
    
    private void rep(StringBuilder sb, String s, int n) {
        for(int i = 0; i < n; i++) {
            sb.append(s);
        }
    }
}
