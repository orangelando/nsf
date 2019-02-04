package lando.nsf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.Validate;

final class DataReader {
	final byte[] bytes;
	
	DataReader(byte[] bytes) {
		Validate.notNull(bytes);
		
		this.bytes = bytes;
	}
	
	int readByte(int offset) {
		int b = bytes[offset];
		
		return b & 0xFF; 
	}
	
	int readWord(int offset) {
		return readByte(offset) | (readByte(offset+1)<<8);
	}
	
	void copy(int srcIndex, byte[] dst, int dstIndex, int len) {
		System.arraycopy(bytes, srcIndex, dst, dstIndex, len);
	}
	
	String readString(int offset, int maxLen) {
	    int len;
		
		for(len = 0; len < maxLen; len++) {
			byte b = bytes[offset + len];
			
			if( b == 0 ) {
				break;
			}
		}
		
		Charset cs = StandardCharsets.US_ASCII;
		
		return new String(bytes, offset, len, cs);
	}
}
