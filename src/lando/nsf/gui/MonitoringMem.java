package lando.nsf.gui;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lando.nsf.cpu.Memory;

public final class MonitoringMem implements Memory {

    private Memory mem;
    
    private final Set<Integer> reads = new HashSet<>();
    private final Set<Integer> writes = new HashSet<>();
    
    public MonitoringMem() {
        this.mem = null;
    }
    
    public MonitoringMem(Memory mem) {
        this.mem = Objects.requireNonNull(mem);
    }
    
    public void setMemory(Memory mem) {
        this.mem = Objects.requireNonNull(mem);
    }
    
    public Set<Integer> getReads() {
        return reads;
    }
    
    public Set<Integer> getWrites() {
        return writes;
    }
    
    public void clearReadsAndWrites() {
        reads.clear();
        writes.clear();
    }
    
    public int readButDontMonitor(int addr) {
        return mem.read(addr);
    }

    @Override
    public int read(int addr) {
        reads.add(addr);
        return mem.read(addr);
    }

    @Override
    public void write(int addr, int data) {
        writes.add(addr);
        mem.write(addr, data);
    }
}
