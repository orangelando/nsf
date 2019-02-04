package lando.nsf;

import org.apache.commons.lang3.Validate;

/**
 * The 6502 has a 16 bit addressable memory address so this guarantees
 * the segment is a valid 16 address and does not overflow.
 * 
 */
public final class MemorySegment {
    
    public static final int MAX_LEN = 1<<16;

    private final int startAddress;
    private final byte[] bytes;
    
    public MemorySegment(int startAddress, byte[] bytes) {
        Validate.notNull(bytes);
        Validate.isTrue(bytes.length > 0);
        Validate.isTrue(startAddress >= 0 && startAddress < MAX_LEN);
        Validate.isTrue(startAddress + bytes.length <= MAX_LEN);
        
        this.startAddress = startAddress;
        this.bytes = bytes;
    }
    
    public int getStartAddress() {
        return startAddress;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public IntRange getAddressRange() {
        return IntRange.of(startAddress, startAddress + bytes.length);
    }
}
