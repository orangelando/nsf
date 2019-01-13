package lando.nsf.snakes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.HexUtils;
import lando.nsf.core6502.CPU;

final class MemoryMonitor {
    
    static final int STATUS_STACK = 1<<0;
    static final int STATUS_DIFF  = 1<<1;
    static final int STATUS_READ  = 1<<2;
    static final int STATUS_WRITE = 1<<3;
    
    private static final int MAX_SIZE = 1<<16;
    
    private final int startAddr;
    private final int size;
    private final int numCols;
    
    private final CPU cpu;
    private final MonitoringMem mem;
    
    private int[] prevBytes;
    private int[] currBytes;
    
    MemoryMonitor(int startAddr, int size, int numCols, CPU cpu, MonitoringMem mem) {
        Validate.isTrue(size > 0);
        Validate.isTrue(startAddr >= 0 && startAddr + size <= MAX_SIZE);
        Validate.isTrue(numCols > 0 && numCols <= size);
        
        this.startAddr = startAddr;
        this.size = size;
        this.numCols = numCols;
        this.prevBytes = new int[size];
        this.currBytes = new int[size];
        
        this.cpu = Objects.requireNonNull(cpu);
        this.mem = Objects.requireNonNull(mem);
    }
    
    List<TextLine> memData() {
        
        copyAndSwapBufs();
        
        List<TextLine> lines = new ArrayList<>();
        
        int endAddr = startAddr + size;
        int stackAddr = cpu.stackAddr();
        
        for(int yOffset = startAddr; yOffset < endAddr; yOffset += numCols) {
            
            StringBuilder txt = new StringBuilder();
            List<LineSection> highlighted = new ArrayList<>();
            
            txt.append(HexUtils.toHex16(yOffset));
            txt.append(":");
            
            for(int xOffset = 0; xOffset < numCols; xOffset++) {
            
                txt.append(" ");
                
                int addr = yOffset + xOffset;
                int i = addr - startAddr;
                int data = currBytes[i];
                int start = txt.length();
                
                txt.append(HexUtils.toHex8(data));
                
                int end = txt.length();
                int highlightStatus = 0;
                
                if( addr == stackAddr ) highlightStatus |= STATUS_STACK;
                if( prevBytes[i] != currBytes[i] ) highlightStatus |= STATUS_DIFF;
                if( mem.getReads().contains(addr) ) highlightStatus |= STATUS_READ;
                if( mem.getWrites().contains(addr) ) highlightStatus |= STATUS_WRITE;
                
                if( highlightStatus != 0 ) {
                    highlighted.add(new LineSection(start, end - start, highlightStatus));
                }
            }
            
            TextLine line = new TextLine(txt.toString());
            
            highlighted.forEach(line::addHighlightedSection);
            
            lines.add(line);
        }
        
        return lines;
    }
    
    private void copyAndSwapBufs() {
        
        int[] tmp = prevBytes;
        
        prevBytes = currBytes;
        currBytes = tmp;
        
        for(int i = 0; i < size; i++) {
            currBytes[i] = mem.readButDontMonitor(startAddr + i);
        }
    }
}
