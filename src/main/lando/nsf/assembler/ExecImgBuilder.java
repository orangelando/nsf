package lando.nsf.assembler;

import java.util.ArrayList;
import java.util.List;

import lando.nsf.ExecutableImage;
import lando.nsf.MemorySegment;

final class ExecImgBuilder implements ByteConsumer {

    private final ExecutableImage img;
    
    private int segStart = 0;
    private List<Integer> segBytes = new ArrayList<>();
    
    ExecImgBuilder(ExecutableImage img) {
        this.img = img;
    }

    @Override
    public void setAddress(int address) {
        flush();
        
        segStart = address;
    }

    @Override
    public void emitByte(int b) {
        segBytes.add(b);
    }
    
    void flush() {
        if( ! segBytes.isEmpty() ) {
            addSegment(img, segStart, segBytes);
            segBytes.clear();
        }
    }
    
    private void addSegment(ExecutableImage img, int segAddress, List<Integer> segBytes) {

        MemorySegment seg = new MemorySegment(segAddress, HexDumpReader.asBytes(segBytes));
        
        img.addMemorySegment(seg);
    }
}
