package lando.wav;

import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

public final class WAVWriter {
    
    private final int numChannels = 1;
    private final int samplesPerSecond = 44_100;
    private final int bytesPerSample = 2;
    private final int blockAlign = bytesPerSample*numChannels;
    
    private final OutputStream out;
    
    public WAVWriter(OutputStream out) {
        this.out = Objects.requireNonNull(out);
    }
    
    public void write(ShortArray samples) throws Exception {
        Validate.notNull(samples);
        
        //XXX: this doubles head usage!
        byte[] bytes = convertToBytes(samples.getArray(), samples.getSize());
        int numBytes = samples.getSize()*2;
        
        Validate.isTrue(bytes.length == numBytes, bytes.length + " != " + numBytes);
        
        writeASCII ("RIFF");
        writeUInt32(4+8+16+8+numBytes);
        writeASCII ("WAVE");
        
        //don't need to align
        
        writeASCII ("fmt "); //chunk id
        writeUInt32(16); //chunk data size
        writeUInt16( 1); //compression code - 1 PCM
        writeUInt16(numChannels);
        writeUInt32(samplesPerSecond);
        writeUInt32(samplesPerSecond*blockAlign);
        writeUInt16(blockAlign);
        writeUInt16(bytesPerSample*8);
        
        //don't need to align
        writeASCII("data");
        writeUInt32(numBytes);
        
        out.write(bytes, 0, numBytes);
        
        /**
         * WAV blocks must be short aligned.
         * XXX: but we're writing shorts so this should always be true!
         */
        if( numBytes % 2 != 0 ) {
            out.write(0);
        }
    }
    
    private static void convertToBytes(
    		short[] srcShorts, int srcShortOffset, int numShortsToCopy,
    		byte[]  dstBytes,  int dstByteOffset ) {
    	
    	Validate.notNull(srcShorts);
    	Validate.isTrue(srcShortOffset >= 0);
    	Validate.isTrue(numShortsToCopy >= 0);
    	Validate.isTrue(srcShortOffset + numShortsToCopy <= srcShorts.length);
    	
    	Validate.notNull(dstBytes);
    	Validate.isTrue(dstByteOffset >= 0);
    	Validate.isTrue(dstByteOffset + 2*numShortsToCopy <= dstBytes.length);
    	
        for( int i = 0; i < numShortsToCopy; i++) {
            dstBytes[dstByteOffset + 2*i + 0] = (byte)((srcShorts[dstByteOffset + i]>>0)&0xFF);
            dstBytes[dstByteOffset + 2*i + 1] = (byte)((srcShorts[dstByteOffset + i]>>8)&0xFF);
        }
    }
    
    private static byte[] convertToBytes(short[] shorts, int numShorts) {        
        byte[] bytes = new byte[numShorts*2];
        
        convertToBytes(shorts, 0, numShorts, bytes, 0);
        
        return bytes;
    }

    private void writeASCII(String s) throws Exception {
        out.write(s.getBytes("ASCII"));        
    }

    private void writeUInt16(int n) throws Exception {
        out.write((n>>0) & 0xFF);
        out.write((n>>8) & 0xFF);
    }

    private void writeUInt32(int n) throws Exception {
        out.write((n>> 0) & 0xFF);
        out.write((n>> 8) & 0xFF);
        out.write((n>>16) & 0xFF);
        out.write((n>>24) & 0xFF);
    }
}
