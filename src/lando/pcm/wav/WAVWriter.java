package lando.pcm.wav;

import java.io.RandomAccessFile;

import lando.pcm.PCMSamples;

import org.apache.commons.lang3.Validate;

public final class WAVWriter {
	
	public static void writePCM(
            String path,
            PCMSamples origSamples,
            long startSample,
            long numSamples,
            byte[] bytes)
	
    throws Exception 
    {
        Validate.notNull(path);
        Validate.notNull(origSamples);
        Validate.notNull(bytes);
        Validate.isTrue( origSamples.numBytes >= 0 && origSamples.numBytes <= bytes.length );
        
        RandomAccessFile fout = new RandomAccessFile(path, "rw");
        
        fout.seek(0);
        fout.setLength(0);
        
        int numBytes = (int)(numSamples*origSamples.numChannels*origSamples.sampleType.bytesPerSample);
        
        writeASCII(fout, "RIFF");
        writeUInt32(fout, 4+8+16+8+numBytes);
        writeASCII(fout, "WAVE");
        
        //don't need to align
        
        writeASCII (fout, "fmt "); //chunk id
        writeUInt32(fout, 16); //chunk data size
        writeUInt16(fout,  1); //compression code - 1 PCM
        writeUInt16(fout, origSamples.numChannels);
        writeUInt32(fout, origSamples.samplesPerSecond);
        writeUInt32(fout, origSamples.samplesPerSecond*origSamples.blockAlign);
        writeUInt16(fout, origSamples.blockAlign);
        writeUInt16(fout, origSamples.sampleType.bytesPerSample*8);
        
        //don't need to align
        writeASCII(fout, "data");
        writeUInt32(fout, numBytes);
        fout.write(bytes, 
        		(int)(startSample*origSamples.numChannels*origSamples.sampleType.bytesPerSample), 
        		numBytes);
        
        if( bytes.length % 2 != 0 ) {
            fout.writeByte(0);
        }
        
        fout.close();
    }
    
    public static void writePCM(
            String path,
            PCMSamples samples ) 
    throws Exception 
    {
        Validate.notNull(path);
        Validate.notNull(samples);
        
        byte[] bytes = null;
        
        switch(samples.sampleType) {
        case BYTE: bytes = samples.bytes; break;
        case SHORT: bytes = convertToBytes(samples.shorts, samples.shorts.length); break;
        }
        
        Validate.notNull(bytes);
        Validate.isTrue( samples.numBytes >= 0 && samples.numBytes <= bytes.length );
        
        RandomAccessFile fout = new RandomAccessFile(path, "rw");
        
        fout.seek(0);
        fout.setLength(0);
        
        writeASCII(fout, "RIFF");
        writeUInt32(fout, 4+8+16+8+samples.numBytes);
        writeASCII(fout, "WAVE");
        
        //don't need to align
        
        writeASCII (fout, "fmt "); //chunk id
        writeUInt32(fout, 16); //chunk data size
        writeUInt16(fout,  1); //compression code - 1 PCM
        writeUInt16(fout, samples.numChannels);
        writeUInt32(fout, samples.samplesPerSecond);
        writeUInt32(fout, samples.samplesPerSecond*samples.blockAlign);
        writeUInt16(fout, samples.blockAlign);
        writeUInt16(fout, samples.sampleType.bytesPerSample*8);
        
        //don't need to align
        writeASCII(fout, "data");
        writeUInt32(fout, samples.numBytes);
        fout.write(bytes, 0, samples.numBytes);
        
        if( bytes.length % 2 != 0 ) {
            fout.writeByte(0);
        }
        
        fout.close();
    }
    
    public static void convertToBytes(
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
    
    public static byte[] convertToBytes(short[] shorts, int numShorts) {        
        byte[] bytes = new byte[shorts.length*2];
        
        convertToBytes(shorts, 0, numShorts, bytes, 0);
        
        return bytes;
    }

    private static void writeASCII(RandomAccessFile fout, String s) throws Exception {
        fout.write(s.getBytes("ASCII"));        
    }

    private static void writeUInt16(RandomAccessFile fout, int n) throws Exception {
        fout.writeByte((byte)((n>>0) &0xFF));
        fout.writeByte((byte)((n>>8) &0xFF));
    }

    private static void writeUInt32(RandomAccessFile fout, int n) throws Exception {
        fout.writeByte((byte)((n>> 0) &0xFF));
        fout.writeByte((byte)((n>> 8) &0xFF));
        fout.writeByte((byte)((n>>16) &0xFF));
        fout.writeByte((byte)((n>>24) &0xFF));
    }
}
