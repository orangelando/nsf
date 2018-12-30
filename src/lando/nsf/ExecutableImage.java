package lando.nsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;

public final class ExecutableImage {

    private final List<MemorySegment> memorySegments = new ArrayList<>();
    
    public ExecutableImage() {
    }
    
    /**
     * segments are in order and do not overlap.
     */
    public List<MemorySegment> getMemorySegments() {
        return Collections.unmodifiableList(memorySegments);
    }
    
    public void addMemorySegment(MemorySegment seg) {
        Validate.notNull(seg);
        Validate.isTrue( ! hasOverlapping(seg) );
        
        memorySegments.add(seg);
        
        memorySegments.sort((a, b) -> Integer.compare(a.getStartAddress(), b.getStartAddress()));
    }
    
    private boolean hasOverlapping(MemorySegment a) {
        
        for(MemorySegment b: memorySegments) {
            if( a.getAddressRange().overlaps(b.getAddressRange())) {
                return true;
            }
        }
        
        return false;
    }
    
    public byte[] joinAllSegments() {
        byte[] bytes = new byte[1 << 16];
        
        for(MemorySegment seg: memorySegments) {
            System.arraycopy(seg.getBytes(), 0, bytes, seg.getStartAddress(), seg.getBytes().length);
        }
        
        return bytes;
    }
}
