package lando.nsf.app;

import static lando.nsf.HexUtils.*;

import java.io.File;
import java.util.Arrays;

import lando.nsf.*;
import lando.nsf.core6502.CPU;
import lando.nsf.core6502.Instruction;
import lando.nsf.core6502.Instructions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TestReadApp {

	 
	public static void main(String [] args) throws Exception {
		
		File file = new File(
				"/Users/oroman/Downloads/super-mario-bros-2-nes-[NSF-ID1934].nsf"
				//"/Users/oroman/Downloads/Metroid.nsf"
				);
		
		NESMem mem = new NESMem();
		CPU cpu = new CPU(mem);
		NSF nsf = NSFReader.readNSF(file);
		
		if( nsf.header.extraSoundChipSupport != 0 ) {
			throw new IllegalArgumentException("Extra sound chips not supported.");
		}
		
		byte[][] banks = new byte[8][];
		
		for(int i = 0; i < 8; i++) {
			banks[i] = new byte[4096];
			Arrays.fill(banks[i], (byte)0);
		}
		
		if( nsf.isBanked() ) {
			
			int bankAddr = 0x0FFF & nsf.header.loadDataAddr;
			int romAddr = 0x80;
			
			while(romAddr < nsf.data.length) {
				banks[bankAddr>>12][bankAddr&0xFFF] = nsf.data[romAddr++];
			}
			
			for(int i = 0; i < 8; i++) {
				mem.write(0x5FF8 + i, nsf.header.bankswitchInitValues[i]);
			}
			
			
		} else {
			System.err.println("Straight loading... " + 0x80 );
			
			if( nsf.header.loadDataAddr < 0x8000 ) {
				System.err.println("Warning: load data before $8000 vs " + toHex16(nsf.header.loadDataAddr));
			}
			
			for(int i = 0x80; i < nsf.data.length; i++) {
				mem.bytes[nsf.header.loadDataAddr + i - 0x80] = nsf.data[i];
			}
		}
		
		printDisassemble(mem.bytes, nsf.header.playDataAddr);
		
		System.err.println("Done");
	}
	
	
	

	
	 
	private static void printDisassemble(byte[] cpuMem, int startAdd) {
		
		System.err.println("startAdd: " + startAdd + " " + toHex16(startAdd));
		
		int pc = startAdd;
		
		while(pc < startAdd + 1000) {
			
			/*
			System.err.printf("%s:", toHex16(pc));
			
			for(int i = 0; i < 4; i++) {
				System.err.printf(" %s", toHex8(cpuMem[pc + i]));
			}
			
			System.err.printf("%n");
			
			
			pc += 4;
			*/
		
			
			int b = cpuMem[pc] & 0xFF;
			
			Instruction op = Instructions.BY_OP_CODE[b];
			
			if( op != null ) {
			
				System.err.printf("%s: %s", toHex16(pc), op.name );
				
				for(int i = 1; i < op.addrMode.instrLen; i++) {
					System.err.printf(" %s", toHex8(cpuMem[pc + i]));
				}
				
				System.err.printf("%n");
				
				pc += op.addrMode.instrLen;
				
			} else {
				
				System.err.printf("%s: %s%n", toHex16(pc), toHex16(b));
				pc += 1;
			}
		}		
	}

	
}
