package lando.nsf.initload;

import static lando.nsf.DisassemblerUtils.opCodeText;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import lando.nsf.HexUtils;
import lando.nsf.app.towav.NES;
import lando.nsf.cpu.AddrMode;
import lando.nsf.cpu.Instruction;
import lando.nsf.cpu.Instructions;
import lando.nsf.cpu.OpCodeName;
import lando.nsf.gui.StringUtils;

public final class GuessAtRoutinesApp {

    private static PrintStream out = System.out;
    
    public static void main(String [] args) throws Exception {
        
        Path path = Paths.get(args[0]);
        out.println("loading " + path);
        
        NES nes = NES.buildForPathNoMemMonitor(path);
        
        Validate.isTrue(
                ! nes.nsf.isBanked(), 
                "banked NSF's not supported.");
        
        Validate.isTrue(
                nes.nsf.header.getSupportedExtraSoundChips().isEmpty(),
                "Extra sound chips not supported.");
        
        List<Routine> routines = new ArrayList<>();
        ListQueue<Routine> startingRoutines = new ListQueue<>();
        
        Addr initAddr = Addr.of(nes.nsf.header.initDataAddr);
        Addr playAddr = Addr.of(nes.nsf.header.playDataAddr);
        
        //startingRoutines.enqueue(
        //        new Routine("init at " + initAddr.hex(), initAddr));
        
        startingRoutines.enqueue(
                new Routine("play at " + playAddr.hex(), playAddr));
        
        while( ! startingRoutines.isEmpty() ) {
            Routine routine = startingRoutines.dequeue();
            
            routines.add(routine);

            readLines(routine, nes);
            
            for(RoutineLine line: routine.getLines()) {
                line.getJumpOrBranchAddr().ifPresent(jumpAddr -> {
                    
                    for(Routine other: startingRoutines) {
                        if( other.getStartAddr() == jumpAddr ) {
                            return;
                        }
                    }
                    
                    for(Routine other: routines) {
                        if( other.isAddrInRange(jumpAddr) && other.hasLineAtAddr(jumpAddr)) {
                            
                            if( ! other.hasLineAtAddr(jumpAddr) ) {
                                out.println("Jump to known non-addr " + jumpAddr.hex());
                            }
                            
                            return;
                        }
                    }
                                        
                    Routine jmpRoutine = new Routine(
                            line.instr.name + " to " + jumpAddr.hex(),
                            jumpAddr);
                    
                    startingRoutines.enqueue(jmpRoutine);
                });
            }
        }
        
        Routine[] prevRoutine = {null};
        
        routines.stream()
            .sorted( (a, b) -> Integer.compare(a.getStartAddr().val, b.getStartAddr().val))
            .forEach(routine -> {
        
            if( prevRoutine[0] == null || prevRoutine[0].getEndAddr().next() != routine.getStartAddr() ) {
                out.println("________________________________________________________________________");
                out.println(routine.getName() + " - " + routine.getEndAddr());
                out.println();
            } else {
                out.println("*joined*");
            }
            
            for(RoutineLine line: routine.getLines()) {
                out.println(line.asm);
            }
            
            prevRoutine[0] = routine;
        });
        
        out.println("=================================================================");
        
        out.println("done");
    }
    
    private static void readLines(Routine routine, NES nes) {
        PC pc = new PC(routine.getStartAddr(), nes.mem.bytes);
        
        while(pc.get() <= 0xFF_FF) {
            Optional<RoutineLine> lineOpt = fetchNextLine(pc);
                        
            if( lineOpt.isPresent() ) {
                RoutineLine line = lineOpt.get();
                
                if( line.instr.name == OpCodeName.BRK ) {
                    break;
                }
                
                routine.addLine(line);
                
                if( line.instr.name == OpCodeName.RTS || 
                    line.instr.name == OpCodeName.RTI ) {
                    break;
                }
            } else {
                break;
            }
        }
    }
    
    private static Optional<RoutineLine> fetchNextLine(PC pc) {

        StringBuilder line = new StringBuilder();

        line.append(HexUtils.toHex16(pc.get()));
        line.append(": ");
        
        int lineStartAddr = pc.get();
        int opCode = pc.nextByte();
        
        Instruction instr = Instructions.BY_OP_CODE[opCode & 0xFF];
        
        if( instr == null ) {
            return Optional.empty();
        }
        
        int b2, b3;
        
        switch(instr.addrMode.instrLen) {
        
        case 1: b2 = 0;
                b3 = 0;
                break;
                
        case 2: b2 = pc.nextByte()&255;
                b3 = 0;
                break;
                
        case 3: b2 = pc.nextByte()&255;
                b3 = pc.nextByte()&255; 
                break;
        
        default: 
            throw new RuntimeException("unknown instr " + instr);
        }
        
        line.append(opCodeText(instr,b2,b3));

        RoutineLine asmLine = new RoutineLine(
                Addr.of(lineStartAddr), instr, b2, b3, line.toString());
        
        return Optional.of(asmLine);
    }
}

final class PC {
    private int pc;
    private byte[] bytes;
    
    PC(Addr startAddr, byte[] bytes) {
        Validate.notNull(startAddr);
        Validate.notNull(bytes);
        
        this.pc = startAddr.val;
        this.bytes = bytes;
    }
    
    int get() {
        return pc;
    }
    
    byte nextByte() {
        return bytes[pc++];
    }
}

final class ListQueue<T> implements Iterable<T> {
    private final List<T> list = new ArrayList<>();
        
    int size() {
        return list.size();
    }
    
    boolean isEmpty() {
        return list.isEmpty();
    }
    
    void enqueue(T e) {
        Validate.notNull(e, "Cannot enqueue null element");
        list.add(e);
    }
    
    T dequeue() {
        Validate.isTrue(! isEmpty(), "Cannot dequeue from an empty queue" );
        T e = list.get(0);
        list.remove(0);
        
        return e;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}

final class Routine {
    
    private final String name;
    private final Addr startAddr;
    
    private RoutineLine firstLine = null;
    private RoutineLine lastLine = null;
    private final List<RoutineLine> lines = new ArrayList<>();
    private final Set<Addr> lineAddrs = new HashSet<>();
    
    Routine(String name, Addr startAddr) {
        this.name = Objects.requireNonNull(name);
        this.startAddr = Objects.requireNonNull(startAddr);
    }
    
    String getName() {
        return name;
    }
    
    Addr getStartAddr() {
        return startAddr;
    }
    
    Addr getEndAddr() {
        return lastLine.endAddr();
    }
    
    void addLine(RoutineLine line) {
        Validate.notNull(line);
        
        if( lines.isEmpty() ) {
            Validate.isTrue(line.addr == startAddr);
            firstLine = line;
            appendLine(line);
        }
        
        else {
            Validate.isTrue(
                    lastLine.endAddr().next() == line.addr,
                    lastLine.endAddr().next() + " != " + line.addr);
            
            appendLine(line);
        }
    }
    
    List<RoutineLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
    
    boolean isAddrInRange(Addr addr) {
        Validate.notNull(addr);
        
        if( lines.isEmpty() ) {
            return false;
        }
        
        return addr.val >= firstLine.addr.val && 
                addr.val <= lastLine.addr.val;
    }
    
    boolean hasLineAtAddr(Addr addr) {
        Validate.notNull(addr);
        
        return lineAddrs.contains(addr);
    }
    
    private void appendLine(RoutineLine line) {
        lines.add(line);
        lineAddrs.add(line.addr);
        lastLine = line;
    }
}

final class RoutineLine {
    final Addr addr;
    final Instruction instr;
    final int b2;
    final int b3;
    final String asm;
    
    RoutineLine(Addr addr, Instruction instr, int b2, int b3, String asm) {
        this.addr = Objects.requireNonNull(addr);
        this.instr = Objects.requireNonNull(instr);
        this.b2 = b2;
        this.b3 = b3;
        this.asm = Objects.requireNonNull(asm);
    }
    
    Addr endAddr() {
        return Addr.of(addr.val + instr.addrMode.instrLen - 1);
    }
    
    Addr absoluteAddr() {
        return Addr.of((b3<<8)|b2);
    }
    
    Addr relativeAddr() {
        int offset = (byte)b2;
        return Addr.of(addr.val + offset + 2);
    }
    
    Optional<Addr> getJumpOrBranchAddr() {
        PrintStream out = System.err;
        boolean isAbsolute = instr.addrMode == AddrMode.ABSOLUTE;
        boolean isRelative =  instr.addrMode == AddrMode.RELATIVE;
        
        switch(instr.name) {
        
        case JMP:
            if( isAbsolute ) {
                return Optional.of(absoluteAddr());
            } else {
                out.println("WARNING: " + instr.name + " is " + instr.addrMode );
            }
            break;
            
        case JSR:
            if( isAbsolute ) {
                return Optional.of(absoluteAddr());
            } else {
                out.println("WARNING: " + instr.name + " is " + instr.addrMode );
            }
            break;
            
        case BCC:
        case BCS:
        case BEQ:
        case BMI:
        case BNE:
        case BPL:
        case BVC:
        case BVS:
            if( isRelative ) {
                return Optional.of(relativeAddr());
            } else {
                out.println("WARNING: " + instr.name + " is " + instr.addrMode );
            }
            break;
        }

        return Optional.empty();
    }
}

final class Addr {
    
    private static final Addr[] ALL_ADDRS = new Addr[1<<16];
    
    static Addr of(int val) {
        Validate.isTrue(val >= 0x00_00 && val <= 0xFF_FF);
        
        Addr addr = ALL_ADDRS[val];
        
        if( addr == null ) {
            addr = new Addr(val);
            ALL_ADDRS[val] = addr;
        }
                
        return addr;
    }
    
    final int val;
    
    private Addr(int val) {
        this.val = val;
    }
    
    Addr next() {
        return Addr.of(val + 1);
    }
    
    String hex() {
        return StringUtils.toHex4(val);
    }
    
    @Override
    public String toString() {
        return hex();
    }
}
