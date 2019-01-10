package lando.nsf.core6502;

import org.apache.commons.lang3.Validate;

import lando.nsf.ExecutableImage;

public final class ByteArrayMem implements Memory {

    public final byte[] bytes = new byte[1<<16];
    
    @Override
    public int read(int addr) {
        return bytes[addr & 0xFFFF] & 0xFF;
    }

    @Override
    public void write(int addr, int data) {
        bytes[addr & 0xFFFF] = (byte)data;
    }
    
    public void load(ExecutableImage execImg) {
        Validate.notNull(execImg);
        
        byte[] imgBytes = execImg.joinAllSegments();
        
        Validate.isTrue(imgBytes != null);
        Validate.isTrue(imgBytes.length == (1<<16));
        
        System.arraycopy(imgBytes, 0, bytes, 0, imgBytes.length);
    }
}
