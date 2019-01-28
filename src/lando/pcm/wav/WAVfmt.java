package lando.pcm.wav;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class WAVfmt {
	public static final int COMPRESSION_UNKNOWN                  =   0    ;
	public static final int COMPRESSION_PCM_UNCOMPRESSED         =   1    ;
	public static final int COMPRESSION_MICROSOFT_ADPCM          =   2    ;
	public static final int COMPRESSION_ITU_G_711_A_LAW          =   6    ;
	public static final int COMPRESSION_ITU_G_711_B_LAW         =   7    ;
	public static final int COMPRESSION_IMA_ADPCM                =  17    ;
	public static final int COMPRESSION_ITU_G_723_ADPCM_YAMAHA   =  20    ;
	public static final int COMPRESSION_GSM_6_10                 =  49    ;
	public static final int COMPRESSION_ITU_G_721_ADPCM          =  64    ;
	public static final int COMPRESSION_MPEG                     =  80    ;
	public static final int COMPRESSION_EXPERIMENTAL             =  65536 ;
	
	public final int compressionCode;
	public final int numberOfChannels;
	public final int sampleRate;
	public final int avgBytesPerSec;
	public final int blockAlign;
	public final int significantBitsPerSample;
	public final int extraFormatBytes;
	
	public WAVfmt(
			int compressionCode, 
			int numberOfChannels, 
			int sampleRate,
			int avgBytesPerSec, 
			int blockAlign,
			int significantBitsPerSample, 
			int extraFormatBytes) {
		
		Validate.isTrue(compressionCode == COMPRESSION_PCM_UNCOMPRESSED);
		Validate.isTrue(numberOfChannels == 1 || numberOfChannels == 2);
		Validate.isTrue(sampleRate > 0);
		Validate.isTrue(significantBitsPerSample == 8 || significantBitsPerSample == 16);
		
		this.compressionCode = compressionCode;
		this.numberOfChannels = numberOfChannels;
		this.sampleRate = sampleRate;
		this.avgBytesPerSec = avgBytesPerSec;
		this.blockAlign = blockAlign;
		this.significantBitsPerSample = significantBitsPerSample;
		this.extraFormatBytes = extraFormatBytes;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}