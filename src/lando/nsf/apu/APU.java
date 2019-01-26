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
	
	public double getOutput() {
	    
	    int pulse1   = 0; //[0 15]
        int pulse2   = 0; //[0 15]
		int triangle = 0; //[0 15]
		int noise    = 0; //[0 15]
		int dmc      = 0; //[0 127]
		
		double pulseOut =  95.88/( 8128.0/(pulse1 + pulse2) + 100.0 );
		double tndOut   = 159.79/( 1f/(triangle/8227.0 + noise/12241.0 + dmc/22638.0) + 100f );
		
		return pulseOut + tndOut;
	}
}
