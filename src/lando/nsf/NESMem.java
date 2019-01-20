package lando.nsf;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.core6502.Memory;

public final class NESMem implements Memory {
	
    public static final int BANK_SIZE = 4096;
    public static final int FIRST_BANK_SWITCH_REGISTER = 0x5FF8;
    public static final int LAST_BANK_SWITCH_REGISTER  = 0x5FFF;
	
	public final byte [] bytes = new byte[65536];
	
	private final APU apu;
	private byte[][] banks = null;
    private boolean isBankSwitching = false;
	
	
    public static final int NUM_ACCESSES = 4096;
    
	public final int[] readAddrs  = new int[NUM_ACCESSES];
	public final int[] writeAddrs = new int[NUM_ACCESSES];
	
	public int reads = 0;
	public int writes = 0;
	
	public NESMem(APU apu) {
	    this.apu = Objects.requireNonNull(apu);
	}
	
	public void clearMem() {
	    Arrays.fill(bytes, (byte)0);
	}
	
	public void clearMem(int startAddr, int endAddrInclusive) {
        Arrays.fill(bytes, startAddr, endAddrInclusive + 1, (byte)0);
    }
	
	public void disableBankSwitching() {
	    this.banks = null;
	    this.isBankSwitching = false;
	}
	
	public void enableBankSwitching(byte[][] banks) {
	    Validate.notNull(banks, "banks cannot be null");
	    Validate.isTrue(banks.length > 0, "banks cannot be empty");
	    
	    for(byte[] bank: banks) {
	        Validate.notNull(bank, "individual banks cannot be null");
	        Validate.isTrue(bank.length == BANK_SIZE, "banks must be 4KiB");
	    }
	    
	    this.banks = banks;
	}

	@Override
	public int read(int addr) {
		addr = trans(addr);
		
		readAddrs[(reads++)%NUM_ACCESSES] = addr;
		int M = bytes[addr] & 0xFF;
		
		if( apu != null ) { 
			switch(addr) {
			case 0x4015: M = apu.getApuFlags(); break;
			}
		}
		
		return M;
	}
	
	@Override
	public void write(int addr, int M) {
		addr = trans(addr);
		
		writeAddrs[(writes++)%NUM_ACCESSES] = addr;
		bytes[addr] = (byte)(M & 0xFF);
		
		if( isBankSwitching && addr >= FIRST_BANK_SWITCH_REGISTER && addr <= LAST_BANK_SWITCH_REGISTER ) {
		    int bankIndex = addr - FIRST_BANK_SWITCH_REGISTER;
            int startAddr = (8 + bankIndex)<<12;
            byte[] newBank = banks[M & 0xFF];
            
            System.arraycopy(newBank, 0, bytes, startAddr, newBank.length);
		}
		
		if( apu != null ) { 
			switch(addr) {
			
			//Pulse 1 channel
			case 0x4000: 				break;
			case 0x4001: 				break;
			case 0x4002: 				break;
			case 0x4003: 				break;
				
			//Pulse 2 channel
			case 0x4004: 				break;
			case 0x4005: 				break;
			case 0x4006: 				break;
			case 0x4007: 				break;
				
			//Triangle channel
			case 0x4008: 				break;
			case 0x400A: 				break;
			case 0x400B: 				break;
				
			//Noise channel
			case 0x400C: 				break;
			case 0x400E: 				break;
			case 0x400F: 				break;
				
			//DMC channel
			case 0x4010:				break;
			case 0x4011:				break;
			case 0x4012:				break;
			case 0x4013:				break;
				
			//misc
			case 0x4015: apu.setApuFlags(M & 0x1F); break;
			case 0x4017: break;
			}
		}
	}
	
	private int trans(int addr) {
		//http://wiki.nesdev.com/w/index.php/CPU_memory_map
		addr &= 0xFFFF;
		
		if( addr < 0x2000 ) { 
			//internal ram 0x0000 - 0x07FF and repeat 0x0800 - 0x1FFF
			addr &= 0x07FF;
		} else if( addr < 0x4000 ) { //PPU mirrors
			//PPU registers are 0x2000 - 0x2007 and repeat 0x02008 - 0x3FFF
			//TODO optimize this
			addr  = 0x2000 + ((addr - 0x2000)%8);
			addr &= 0xFFFF;
		} else if( addr < 0x4020 ) { //NES APU and I/O registers
			//APU and I/O registers are 0x4000 - 0x401F
		} else {
			//Cartridge space: PRG ROM, PRG RAM, and mapper registers (See Note)
		}
		
		return addr;
	}

    public void initAPU() {

        for(int addr = 0x4000; addr <= 0x400f; addr++) {
            write(addr, 0);
        }
        
        write(0x4010, 0x10);
        
        for(int addr = 0x4011; addr <= 0x4013; addr++) {
            write(addr, 0);
        }
        
        write(0x4015, 0x0f);
    }
}
