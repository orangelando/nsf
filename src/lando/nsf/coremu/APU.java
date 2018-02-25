package lando.nsf.coremu;

/**
 * http://wiki.nesdev.com/w/index.php/APU_Mixer
 * 
 * @author oroman
 *
 */
public final class APU {

	float output;
	private int apuFlags;
	
	boolean square1Enabled = false;
	boolean square2Enabled = false;
	boolean triangleEnabled = false;
	boolean noiseEnabled = false;
	boolean dmcEnabled = false;
	
	public void step() {
		
		computeOutput();
	}
	
	public void computeOutput() {
		int triangle = 0; //[0 15]
		int noise    = 0; //[0 15]
		int pulse1   = 0; //[0 15]
		int pulse2   = 0; //[0 15]
		int dmc      = 0; //[0 127]
		
		float pulseOut =  95.88f/( 8128f/(pulse1 + pulse2) + 100f );
		float tndOut   = 159.79f/( 1f/(triangle/8227f + noise/12241f + dmc/22638f) + 100f );
		
		this.output = pulseOut + tndOut;
	}
	
	public void setApuFlags(int apuFlags) {
		//do not set top 2 bits of first byte
		this.apuFlags =
				(this.apuFlags & 0xC0) | 
				(     apuFlags & 0x1F); 
		
		square1Enabled  = (apuFlags & 0x01) != 0;
		square2Enabled  = (apuFlags & 0x02) != 0;
		triangleEnabled = (apuFlags & 0x04) != 0;
		noiseEnabled    = (apuFlags & 0x08) != 0;
		dmcEnabled      = (apuFlags & 0x10) != 0;
	}

	public int getApuFlags() {
		return apuFlags;
	}
}
