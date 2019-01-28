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
	
	public void setPulse2Enabled(boolean flag) {
        pulse2Enabled = flag;
    }
	
	public void setTriangleEnabled(boolean flag) {
        triangleEnabled = flag;
    }
	
	public void setNoiseEnabled(boolean flag) {
        noiseEnabled = flag;
    }
	
	public void setDmcEnabled(boolean flag) {
	    dmcEnabled = flag;
	}
	
	public void clockFrameSequencer() {
	    frameSequencer.clockDivider();
	}
	
	public void clockChannelTimers() {
	    pulse1.clockTimer();
	    pulse2.clockTimer();
	    triangle.clockTimer();
	    noise.clockTimer();
	}
	
	/**
	 * returns a value in the range [0, 1]
	 */
	public float getOutput() {
	    
	    int pulse1Val   = pulse1Enabled   ? pulse1  .getOutput() : 0; //[0 15]
        int pulse2Val   = pulse2Enabled   ? pulse2  .getOutput() : 0; //[0 15]
		int triangleVal = triangleEnabled ? triangle.getOutput() : 0; //[0 15]
		int noiseVal    = noiseEnabled    ? noise   .getOutput() : 0; //[0 15]
		int dmcVal      = dmcEnabled      ? dmc     .getOutput() : 0; //[0 127]
		
		float pulseOut =  95.88f/( 8128f/(pulse1Val + pulse2Val) + 100f );
		float tndOut   = 159.79f/( 1f/(triangleVal/8227f + noiseVal/12241f + dmcVal/22638f) + 100f );
		float out      = pulseOut + tndOut;
				
		if( out < 0.0f ) {
		    return 0.0f;
		}
		
		if( out > 1.0f ) {
		    return 1.0f;
		}
		
		return out;
	}
}
