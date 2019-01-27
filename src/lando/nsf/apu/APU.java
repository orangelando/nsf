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
		
	public APU(CPU cpu) {
	    this.frameSequencer = new FrameSequencer(
	            cpu, pulse1, pulse2, triangle, noise, dmc);
	}
	
	public void clockFrameSequencer() {
	    frameSequencer.clockDivider();
	}
	
	public void clockChannelTimers() {
	    pulse1.clockTimer();
	    pulse2.clockTimer();
	    triangle.clockTimer();
	    //noise.timer.clock();
	    //dmc.timer.clock();
	}
	
	/**
	 * returns a value in the range [0, 1]
	 */
	public double getOutput() {
	    
	    int pulse1Val   = pulse1  .getOutput(); //[0 15]
        int pulse2Val   = pulse2  .getOutput(); //[0 15]
		int triangleVal = triangle.getOutput(); //[0 15]
		int noiseVal    = noise   .getOutput(); //[0 15]
		int dmcVal      = dmc     .getOutput(); //[0 127]
		
		double pulseOut =  95.88/( 8128.0/(pulse1Val + pulse2Val) + 100.0 );
		double tndOut   = 159.79/( 1f/(triangleVal/8227.0 + noiseVal/12241.0 + dmcVal/22638.0) + 100.0 );
		double out      = pulseOut + tndOut;
				
		if( out < 0.0 ) {
		    return 0.0;
		}
		
		if( out > 1.0 ) {
		    return 1.0;
		}
		
		return out;
	}
}
