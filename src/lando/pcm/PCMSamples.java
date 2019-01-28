package lando.pcm;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PCMSamples {

	public static enum SampleType {
		BYTE(1, false), SHORT(2, true);
		
		public final int bytesPerSample;
		public final boolean signed;
		
		SampleType(int bytesPerSample, boolean signed) {
			this.bytesPerSample = bytesPerSample;
			this.signed = signed;
		}
	}
	
	public final int samplesPerSecond;
	public final int numChannels;
	public final int numSamples;
	
	transient public final byte[] bytes;
	transient public final short[] shorts;
	
	public final SampleType sampleType;
	public final int blockAlign;
	public final int numBytes;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false);
	}
	
	private PCMSamples(
			final int samplesPerSecond,
			final int numChannels,
			final int numSamples,
			final SampleType sampleType,
			final byte[] bytes,
			final short[] shorts)
	{
		Validate.isTrue(samplesPerSecond >= 1);
	    Validate.isTrue(numChannels == 1 || numChannels == 2); //true limit is 65535
	    Validate.isTrue(numSamples >= 1);
	    Validate.notNull(sampleType);
	    Validate.isTrue( (bytes != null || shorts != null) && !(bytes != null && shorts != null) );
	    
	    switch(sampleType) {
	    
	    case BYTE: 
	    	Validate.notNull(bytes); 
	    	Validate.isTrue(numSamples*numChannels <= bytes.length); 
	    	break;
	    	
	    case SHORT: 
	    	Validate.notNull(shorts); 
	    	Validate.isTrue(numSamples*numChannels <= shorts.length); 
	    	break;
	    }
	    
		this.samplesPerSecond = samplesPerSecond;		
		this.numChannels = numChannels;
		this.numSamples = numSamples;
		
		this.sampleType = sampleType;
		this.blockAlign = sampleType.bytesPerSample*numChannels;
		this.numBytes = numSamples*blockAlign;
		this.bytes = bytes;
		this.shorts = shorts;
		
		Validate.isTrue( numBytes % blockAlign == 0 );
	}
	
	public PCMSamples(
			final int samplesPerSecond,
			final int numChannels,
			final int numSamples,
			final byte[] samples) 
	{
		this(samplesPerSecond, numChannels, numSamples, SampleType.BYTE, samples, null);		
	}
	
	public PCMSamples(
			final int samplesPerSecond,
			final int numChannels,
			final int numSamples,
			final short[] samples) 
	{
		this(samplesPerSecond, numChannels, numSamples, SampleType.SHORT, null, samples);
	}
}
