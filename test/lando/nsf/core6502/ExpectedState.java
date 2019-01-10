package lando.nsf.core6502;

import static lando.nsf.core6502.StringUtils.toHex4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;

final class ExpectedState {
    
    private static Map<Integer, Integer> toMem(int ... addrDataPairs) {
        Validate.notNull(addrDataPairs);
        Validate.isTrue(addrDataPairs.length%2 == 0);
        
        Map<Integer, Integer> mem = new TreeMap<>();
        
        for(int i = 0; i < addrDataPairs.length; i+= 2) {
            mem.put(addrDataPairs[i] & 0xFFFF, addrDataPairs[i + 1] & 0xFFFF);
        }
        
        return mem;
    }
    
    static ExpectedState copyRegisters(CPU cpu) {
        Validate.notNull(cpu);
        
        Map<Integer, Integer> mem = new TreeMap<>();
        
        return new ExpectedState(
                Optional.of(cpu.A),
                Optional.of(cpu.X),
                Optional.of(cpu.Y),
                Optional.of(cpu.S),
                Optional.of(cpu.PC),
                Optional.of(cpu.P),
                mem);
    }
    
    static ExpectedState onlyMem(int ... addrDataPairs) {
        return new ExpectedState(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                toMem(addrDataPairs));
    }
    
    static ExpectedState onlyRegisters(
            Optional<Integer> accumulator, 
            Optional<Integer> xIndex, 
            Optional<Integer> yIndex,
            Optional<Integer> stackPointer, 
            Optional<Integer> programCounter, 
            Optional<Integer> processorFlags
            ) {
        
        Map<Integer, Integer> mem = new TreeMap<>();
        
        return new ExpectedState(
                accumulator,
                xIndex,
                yIndex,
                stackPointer,
                programCounter,
                processorFlags,
                mem);
    }
    
    static ExpectedState of(
            Optional<Integer> accumulator, 
            Optional<Integer> xIndex, 
            Optional<Integer> yIndex,
            Optional<Integer> stackPointer, 
            Optional<Integer> programCounter, 
            Optional<Integer> processorFlags,
            int ... addrDataPairs
            ) {
                
        return new ExpectedState(
                accumulator,
                xIndex,
                yIndex,
                stackPointer,
                programCounter,
                processorFlags,
                toMem(addrDataPairs));
    }
    
    static ExpectedState empty() {
        
        Map<Integer, Integer> mem = new TreeMap<>();
        
        return new ExpectedState(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                mem);
    }


    final Optional<Integer> accumulator;
    final Optional<Integer> xIndex;
    final Optional<Integer> yIndex;
    final Optional<Integer> stackPointer;
    final Optional<Integer> programCounter;
    final Optional<Integer> processorFlags;
    final Map<Integer, Integer> memory;
    
    ExpectedState(
            Optional<Integer> accumulator, 
            Optional<Integer> xIndex, 
            Optional<Integer> yIndex,
            Optional<Integer> stackPointer, 
            Optional<Integer> programCounter, 
            Optional<Integer> processorFlags,
            Map<Integer, Integer> memory) {
        
        this.accumulator    = Objects.requireNonNull(accumulator);
        this.xIndex         = Objects.requireNonNull(xIndex);
        this.yIndex         = Objects.requireNonNull(yIndex);
        this.stackPointer   = Objects.requireNonNull(stackPointer);
        this.programCounter = Objects.requireNonNull(programCounter);
        this.processorFlags = Objects.requireNonNull(processorFlags);
        this.memory         = Objects.requireNonNull(memory);
    }
    
    boolean registersMatch(CPU cpu) {
        return  registerMatches(accumulator,    cpu.A ) &&
                registerMatches(xIndex,         cpu.X ) &&
                registerMatches(yIndex,         cpu.Y ) &&
                registerMatches(stackPointer,   cpu.S ) &&
                registerMatches(programCounter, cpu.PC) &&
                registerMatches(processorFlags, cpu.P );
    }
    
    List<DataDiff> dataDiffs(Memory mem) {
        List<DataDiff> diffs = new ArrayList<>();
        
        for(Integer addr: memory.keySet()) {
            int expected = memory.get(addr);
            int actual   = mem.read(addr);
            
            if( expected != actual ) {
                diffs.add(new DataDiff(addr, expected, actual));
            }
        }
        
        return diffs;
    }
    
    String registersString() {
        return "{" +
                accumulator   .map(a  -> "A="  + toHex4(a)  + ",").orElse("") +
                xIndex        .map(x  -> "X="  + toHex4(x)  + ",").orElse("") + 
                yIndex        .map(y  -> "Y="  + toHex4(y)  + ",").orElse("") + 
                stackPointer  .map(s  -> "S="  + toHex4(s)  + ",").orElse("") + 
                programCounter.map(pc -> "PC=" + toHex4(pc) + ",").orElse("") + 
                processorFlags.map(p  -> "P="  + flags(p)  + ",").orElse("") + 
                "}";
    }
    
    private String flags(int p) {
        return "{" + 
                "N=" + flag(p, CPU.STATUS_N) + "," + 
                "V=" + flag(p, CPU.STATUS_O) + "," + 
                "B=" + flag(p, CPU.STATUS_B) + "," + 
                "D=" + flag(p, CPU.STATUS_D) + "," + 
                "I=" + flag(p, CPU.STATUS_I) + "," + 
                "Z=" + flag(p, CPU.STATUS_Z) + "," + 
                "C=" + flag(p, CPU.STATUS_C) + 
                "}";
    }
    
    private int flag(int p, int bit) {
        return (p&bit) != 0 ? 1 : 0;
    }
    
    private boolean registerMatches(Optional<Integer> expected, int actual) {
        return expected.isPresent() ? expected.get() == actual : true;
    }
}

