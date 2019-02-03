package lando.nsf.apu;

import lando.nsf.apu.dmc.DeltaModulationChannel;
import lando.nsf.apu.noise.NoiseChannel;
import lando.nsf.apu.pulse.PulseChannel;
import lando.nsf.apu.triangle.TriangleChannel;
import lando.nsf.cpu.CPU;

public final class APU {
	
	final PulseChannel pulse1 = new PulseChannel(false);
	final PulseChannel pulse2 = new PulseChannel(true);
	final TriangleChannel triangle = new TriangleChannel();
	final NoiseChannel noise = new NoiseChannel();
	final DeltaModulationChannel dmc = new DeltaModulationChannel();
	final FrameSequencer frameSequencer;
	
	private boolean pulse1Enabled = true;
	private boolean pulse2Enabled = true;
	private boolean triangleEnabled = true;
	private boolean noiseEnabled = true;
	private boolean dmcEnabled = true;
			
	public APU(CPU cpu) {
	    this.frameSequencer = new FrameSequencer(
	            cpu, pulse1, pulse2, triangle, noise, dmc);	    
	}
	
	public void setPulse1Enabled(boolean flag) {
	    pulse1Enabled = flag;
	}
	
	public boolean isPulse1Enabled() {
	    return pulse1Enabled;
	}
	
	public void setPulse2Enabled(boolean flag) {
        pulse2Enabled = flag;
    }
	
	public boolean isPulse2Enabled() {
        return pulse2Enabled;
    }
	
	public void setTriangleEnabled(boolean flag) {
        triangleEnabled = flag;
    }
	
	public boolean isTriangleEnabled() {
	    return triangleEnabled;
	}
	
	public void setNoiseEnabled(boolean flag) {
        noiseEnabled = flag;
    }
	
	public boolean isNoiseEnabled() {
	    return noiseEnabled;
	}
	
	public void setDmcEnabled(boolean flag) {
	    dmcEnabled = flag;
	}
	
	public boolean isDmcEnabled() {
	    return dmcEnabled;
	}
	
	public void clockFrameSequencer() {
	    frameSequencer.clockSequencer();
	}
	
	public void clockChannelTimers() {
	    pulse1.clockTimer();
	    pulse2.clockTimer();
	    triangle.clockTimer();
	    noise.clockTimer();
	}
	
	public float pulse1Output() {
	    return clamped(pulse1.getOutput()/16f);
	}
	
    public float pulse2Output() {
        return clamped(pulse2.getOutput()/16f);
    }
    
    public float triangleOutput() {
        return clamped(triangle.getOutput()/16f);
    }
    
    public float noiseOutput() {
        return clamped(noise.getOutput()/16f);
    }
    
    public float dmcOutput() {
        return clamped(dmc.getOutput()/128f);
    }
    	
	/**
	 * returns a value in the range [0, 1]
	 */
	public float mixerOutput() {
	    
	    int pulse1Val   = pulse1Enabled   ? pulse1  .getOutput() : 0; //[0 15]
        int pulse2Val   = pulse2Enabled   ? pulse2  .getOutput() : 0; //[0 15]
		int triangleVal = triangleEnabled ? triangle.getOutput() : 0; //[0 15]
		int noiseVal    = noiseEnabled    ? noise   .getOutput() : 0; //[0 15]
		int dmcVal      = dmcEnabled      ? dmc     .getOutput() : 0; //[0 127]
		
		float pulseOut =  95.88f/( 8128f/(pulse1Val + pulse2Val) + 100f );
		float tndOut   = 159.79f/( 1f/(triangleVal/8227f + noiseVal/12241f + dmcVal/22638f) + 100f );
		
		return clamped(pulseOut + tndOut);
	}
	
	private float clamped(float output) {
	    if( output < 0f ) return 0f;
	    if( output > 1f ) return 1f;
	    
	    return output;
	}
}
