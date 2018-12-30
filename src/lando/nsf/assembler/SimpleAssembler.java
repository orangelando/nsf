package lando.nsf.assembler;

import java.util.List;

import org.apache.commons.lang3.Validate;

import lando.nsf.ExecutableImage;
import lando.nsf.MemorySegment;

/**
 * Takes a single list of lines and assemblers an executable image.
 */
public final class SimpleAssembler {
    
    public static final int DEFAULT_START_ADDR = 0x0600;
    
    private int address;

    /**
     * Any address deceleration will override "startAddress".
     */
    public ExecutableImage build(List<String> lines, int startAddress) {
        Validate.notNull(lines);
        Validate.notEmpty(lines);
        Validate.noNullElements(lines);
        Validate.isTrue(startAddress >= 0 && startAddress < MemorySegment.MAX_LEN);
        
        this.address = startAddress;
        
        ExecutableImage img = new ExecutableImage();
        
        return img;
    }
    
    public ExecutableImage build(List<String> lines) {
        return build(lines, DEFAULT_START_ADDR);
    }
}
