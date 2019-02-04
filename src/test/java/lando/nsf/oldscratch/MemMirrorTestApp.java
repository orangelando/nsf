package lando.nsf.oldscratch;

import lando.nsf.HexUtils;
import lando.nsf.NESMem;

public final class MemMirrorTestApp {

	public static void main(String [] args) throws Exception {
	    //APU apu = new APU();
		NESMem mem = new NESMem();
		
		//mirrored working RAM
		for(int i = 0; i < 20; i++) {
			mem.write(i, i);
		}
		
		for(int j = 0; j < 4; j++) {
		
			int startAddr = 0x0800*j;
			
			System.err.print(HexUtils.toHex16(startAddr) + ": ");
			
			for(int i = 0; i < 20; i++) {
				System.err.print( HexUtils.toHex8(mem.read(startAddr + i)) + " ");
			}
			
			System.err.println();
		}
		
		//mirroed PPU registers
		mem.write(0x2000, 1);
		mem.write(0x2001, 2);
		mem.write(0x2002, 3);
		
		for(int j = 0; j < 4; j++) {
			
			int startAddr = 0x2000 + 0x8*j;
			
			System.err.print(HexUtils.toHex16(startAddr) + ": ");
			
			for(int i = 0; i < 4; i++) {
				System.err.print( HexUtils.toHex8(mem.read(startAddr + i)) + " ");
			}
			
			System.err.println();
		}
	}
}
