package lando.nsf;

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
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < maxLen; i++) {
			char c = (char)bytes[offset + i];
			
			if( c == '\0') {
				break;
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
}
