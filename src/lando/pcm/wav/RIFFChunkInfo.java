package lando.pcm.wav;

import org.apache.commons.lang3.Validate;

public final class RIFFChunkInfo {
	public final int chunkId;
	public final int chunkDataSize;
	public final int chunkDataBytesOffset;
	
	public RIFFChunkInfo(int chunkId, int chunkDataSize, int chunkDataBytesOffset) {
		Validate.isTrue(chunkDataSize > 0);
		Validate.isTrue(chunkDataBytesOffset > 0);
		
		this.chunkId = chunkId;
		this.chunkDataSize = chunkDataSize;
		this.chunkDataBytesOffset = chunkDataBytesOffset;
	}
	
	@Override
	public String toString() {
		return getIdString(chunkId) + "," + chunkDataSize + "," + chunkDataBytesOffset;
	}
	
	public static String getIdString(int id) {
		try {
			return new String(new byte[] {
					(byte)((id >>  0) & 0xFF),
					(byte)((id >>  8) & 0xFF),
					(byte)((id >> 16) & 0xFF),
					(byte)((id >> 24) & 0xFF),
			}, "ASCII");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}