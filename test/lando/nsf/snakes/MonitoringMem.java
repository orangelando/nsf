package lando.nsf.snakes;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lando.nsf.core6502.Memory;

final class MonitoringMem implements Memory {

    private final Memory mem;
    private final Set<Integer> reads = new HashSet<>();
    private final Set<Integer> writes = new HashSet<>();
    
    MonitoringMem(Memory mem) {
        this.mem = Objects.requireNonNull(mem);
    }
    
    Set<Integer> getReads() {
        return reads;
    }
    
    Set<Integer> getWrites() {
        return writes;
    }
    
    void clearReadsAndWrites() {
        reads.clear();
        writes.clear();
    }
    
    int readButDontMonitor(int addr) {
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
