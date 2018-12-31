package lando.nsf.core6502;

import org.apache.commons.lang3.Validate;

public final class CPU {
	public static final int STATUS_C = 0x01;
	public static final int STATUS_Z = 0x02;
	public static final int STATUS_I = 0x04;
	public static final int STATUS_D = 0x08;
	public static final int STATUS_B = 0x10;
	public static final int STATUS_E = 0x20;
	public static final int STATUS_O = 0x40;
	public static final int STATUS_N = 0x80;
	public static final int STACK_START = 0x0100;
	
	private final Memory mem;
	
	public int P  = 0;
	public int PC = 0;
	public int A  = 0;
	public int X  = 0;
	public int Y  = 0;
	public int S  = 0;
	
	public int cycles = 0;
	
	public CPU(Memory mem) {
		Validate.notNull(mem);
		this.mem = mem;
	}
	
	public int step() {
		cycles = 0;
		
		int addr;
		int opCode = mem.read(PC++);
		
		/**
		 * I find having the constants in the case statements easier to search and debug
		 * than creating constant fields.
		 */
		switch(opCode) {
		
			//ADC
			case 0x69: adc(readImmediate());		 break;
			case 0x65: adc(readZeroPage());			 break;
			case 0x75: adc(readZeroPageX()); 		 break;
			case 0x6D: adc(readAbsolute());			 break;
			case 0x7D: adc(readAbsoluteX());		 break;
			case 0x79: adc(readAbsoluteY());		 break;
			case 0x61: adc(readIndexedIndirectX());	 break;
			case 0x71: adc(readIndirectIndexedY());	 break;
			
			//AND
			case 0x29: A &= readImmediate();       setZN(A); break;
			case 0x25: A &= readZeroPage();        setZN(A); break;
			case 0x35: A &= readZeroPageX();       setZN(A); break;
			case 0x2D: A &= readAbsolute();        setZN(A); break;
			case 0x3D: A &= readAbsolute();        setZN(A); break;
			case 0x39: A &= readAbsoluteY();       setZN(A); break;
			case 0x21: A &= readIndexedIndirectX(); setZN(A); break;
			case 0x31: A &= readIndirectIndexedY(); setZN(A); break;
			
			//ASL
			case 0x0A: aslAcc();                     cycles = 2; break;
			case 0x06: aslMem(readZeroPageAddr ());  cycles = 5; break;
			case 0x16: aslMem(readZeroPageXAddr());  cycles = 6; break;
			case 0x0E: aslMem(readAbsoluteAddr ());  cycles = 6; break;
			case 0x1E: aslMem(readAbsoluteXAddr());  cycles = 7; break;
			
			//BCC
			case 0x90: branchIfClear(STATUS_C); break;
				
			//BCS
			case 0xB0: branchIfSet(STATUS_C); break;
				
			//BEQ
			case 0xF0: branchIfSet(STATUS_Z); break;
				
			//BIT
			case 0x24: bit(readZeroPage()); break;
			case 0x2C: bit(readAbsolute()); break;
			
			//BMI
			case 0x30: branchIfSet(STATUS_N);
			
			//BNE
			case 0xD0: branchIfClear(STATUS_Z); break;
			
			//BPL
			case 0x10: branchIfClear(STATUS_N); break;
			
			//BRK:
			case 0x00:
				pushAddr(PC);
				push(P);
				PC = (mem.read(0xFFFF) << 8) | mem.read(0xFFFE);
				setStatus(true, STATUS_B);
				cycles = 7;
				break;
			
			//BVC
			case 0x50: branchIfClear(STATUS_O); break;
			
			//BVS
			case 0x70: branchIfSet(STATUS_O); break;
			
			//CLC
			case 0x18: setStatus(false, STATUS_C); cycles = 2; break;
			
			//CLD
			case 0xD8: setStatus(false, STATUS_D); cycles = 2; break;
			
			//CLI
			case 0x58: setStatus(false, STATUS_I); cycles = 2; break;
			
			//CLV
			case 0xB8: setStatus(false, STATUS_O); cycles = 2; break;
			
			//CMP
			case 0xC9: cmp(A, readImmediate()); break;
			case 0xC5: cmp(A, readZeroPage()); break;
			case 0xD5: cmp(A, readZeroPageX()); break;
			case 0xCD: cmp(A, readAbsolute()); break;
			case 0xDD: cmp(A, readAbsoluteX()); break;
			case 0xD9: cmp(A, readAbsoluteY()); break;
			case 0xC1: cmp(A, readIndexedIndirectX()); break;
			case 0xD1: cmp(A, readIndirectIndexedY()); break;
			
			//CPX
			case 0xE0: cmp(X, readImmediate()); break;
			case 0xE4: cmp(X, readZeroPage()); break;
			case 0xEC: cmp(X, readAbsolute()); break;
			
			//CPY
			case 0xC0: cmp(Y, readImmediate()); break;
			case 0xC4: cmp(Y, readZeroPage()); break;
			case 0xCC: cmp(Y, readAbsolute()); break;
			
			//DEC
			case 0xC6: addr = readZeroPageAddr();  mem.write(addr, mem.read(addr) - 1); setZN(mem.read(addr)); cycles = 5; break;
			case 0xD6: addr = readZeroPageXAddr(); mem.write(addr, mem.read(addr) - 1); setZN(mem.read(addr)); cycles = 6; break;
			case 0xCE: addr = readAbsoluteAddr();  mem.write(addr, mem.read(addr) - 1); setZN(mem.read(addr)); cycles = 6; break;
			case 0xDE: addr = readAbsoluteX();     mem.write(addr, mem.read(addr) - 1); setZN(mem.read(addr)); cycles = 7; break;
			
			//DEX
			case 0xCA: X = (X - 1) & 0xFF; setZN(X); cycles = 2; break;
			
			//DEY
			case 0x88: Y = (Y - 1) & 0xFF; setZN(Y); cycles = 2; break;
			
			//EOR
			case 0x49: A ^= readImmediate();        setZN(A); break;
			case 0x45: A ^= readZeroPage();         setZN(A); break;
			case 0x55: A ^= readZeroPageX();        setZN(A); break;
			case 0x4D: A ^= readAbsolute();         setZN(A); break;
			case 0x5D: A ^= readAbsoluteX();        setZN(A); break;
			case 0x59: A ^= readAbsoluteY();        setZN(A); break;
			case 0x41: A ^= readIndexedIndirectX(); setZN(A); break;
			case 0x51: A ^= readIndirectIndexedY(); setZN(A); break;
			
			//INC
			case 0xE6: addr = readZeroPageAddr();  mem.write(addr, mem.read(addr) + 1); setZN(mem.read(addr)); cycles = 5; break;
			case 0xF6: addr = readZeroPageXAddr(); mem.write(addr, mem.read(addr) + 1); setZN(mem.read(addr)); cycles = 6; break;
			case 0xEE: addr = readAbsoluteAddr();  mem.write(addr, mem.read(addr) + 1); setZN(mem.read(addr)); cycles = 6; break;
			case 0xFE: addr = readAbsoluteX();     mem.write(addr, mem.read(addr) + 1); setZN(mem.read(addr)); cycles = 7; break;
			
			//INX
			case 0xE8: X = (X + 1) & 0xFF; setZN(X); cycles = 2; break;
			
			//INY
			case 0xC8: Y = (Y + 1) & 0xFF; setZN(Y); cycles = 2; break;
			
			//JMP
			case 0x4C: PC = readAbsoluteAddr(); break;
			case 0x6C: PC = readIndirectAddr(); break;
			
			//JSR
			case 0x20:
					pushAddr(PC - 1);
					PC = readAbsoluteAddr(); 
					cycles = 6;
					break;
			
			//LDA
			case 0xA9: A = readImmediate();       setZN(A); break;
			case 0xA5: A = readZeroPage();        setZN(A); break;
			case 0xB5: A = readZeroPageX();       setZN(A); break;
			case 0xAD: A = readAbsolute();        setZN(A); break;
			case 0xBD: A = readAbsoluteX();       setZN(A); break;
			case 0xB9: A = readAbsoluteY();       setZN(A); break;
			case 0xA1: A = readIndexedIndirectX(); setZN(A); break;
			case 0xB1: A = readIndirectIndexedY(); setZN(A); break;
				
			//LDX
			case 0xA2: X = readImmediate(); setZN(X); break;
			case 0xA6: X = readZeroPage();  setZN(X); break;
			case 0xB6: X = readZeroPageY(); setZN(X); break;
			case 0xAE: X = readAbsolute();  setZN(X); break;
			case 0xBE: X = readAbsoluteY(); setZN(X); break;
			
			//LDY
			case 0xA0: Y = readImmediate(); setZN(Y); break;
			case 0xA4: Y = readZeroPage();  setZN(Y); break;
			case 0xB4: Y = readZeroPageX(); setZN(Y); break;
			case 0xAC: Y = readAbsolute();  setZN(Y); break;
			case 0xBC: Y = readAbsoluteX(); setZN(Y); break;
			
			//LSR
			case 0x4A: lsrAcc();                    cycles = 2; break;
			case 0x46: lsrMem(readZeroPageAddr());  cycles = 5; break;
			case 0x56: lsrMem(readZeroPageXAddr()); cycles = 6; break;
			case 0x4E: lsrMem(readAbsoluteAddr());  cycles = 6; break;
			case 0x5E: lsrMem(readAbsoluteXAddr()); cycles = 7; break;
		
			//NOP
			case 0xEA: cycles = 2; break;
			
			//ORA
			case 0x09: A |= readImmediate();       setZN(A); break;
			case 0x05: A |= readZeroPage();        setZN(A); break;
			case 0x15: A |= readZeroPageX();       setZN(A); break;
			case 0x0D: A |= readAbsolute();        setZN(A); break;
			case 0x1D: A |= readAbsoluteX();       setZN(A); break;
			case 0x19: A |= readAbsoluteY();       setZN(A); break;
			case 0x01: A |= readIndexedIndirectX(); setZN(A); break;
			case 0x11: A |= readIndirectIndexedY(); setZN(A); break;
			
			//PHA
			case 0x48: push(A); cycles = 3; break;
			
			//PHP
			case 0x08: push(P); cycles = 3; break;
			
			//PLA
			case 0x68: A = pull(); setZN(A); cycles = 4; break;
			
			//PLP
			case 0x28: P = pull(); cycles = 4; break;
			
			//ROL
			case 0x2A: rolAcc();                    cycles = 2; break;
			case 0x26: rolMem(readZeroPageAddr());  cycles = 5; break;
			case 0x36: rolMem(readZeroPageXAddr()); cycles = 6; break;
			case 0x2E: rolMem(readAbsoluteAddr());  cycles = 6; break;
			case 0x3E: rolMem(readAbsoluteXAddr()); cycles = 7; break;
			
			//ROR
			case 0x6A: rorAcc();                    cycles = 2; break;
			case 0x66: rorMem(readZeroPageAddr());  cycles = 5; break;
			case 0x76: rorMem(readZeroPageXAddr()); cycles = 6; break;
			case 0x6E: rorMem(readAbsoluteAddr());  cycles = 6; break;
			case 0x7E: rorMem(readAbsoluteXAddr()); cycles = 7; break;
			
			//RTI
			case 0x40:
				P = pull();
				PC = pullAddr();
				cycles = 6; 
				break;
				
			//RTS
			case 0x60: 
				PC = pullAddr();
				cycles = 6;
				break;
				
			//SBC
			case 0xE9: sbc(readImmediate());        break;
			case 0xE5: sbc(readZeroPage());         break;
			case 0xF5: sbc(readZeroPageX());        break;
			case 0xED: sbc(readAbsolute());         break;
			case 0xFD: sbc(readAbsoluteX());        break;
			case 0xF9: sbc(readAbsoluteY());        break;
			case 0xE1: sbc(readIndexedIndirectX()); break;
			case 0xF1: sbc(readIndirectIndexedY()); break;
			
			//SEC
			case 0x38: setStatus(true, STATUS_C); cycles = 2; break;
			
			//SED
			case 0xF8: setStatus(true, STATUS_D); cycles = 2; break;
			
			//SEI
			case 0x78: setStatus(true, STATUS_I); cycles = 2; break;
			
			//STA
			case 0x85: mem.write( readZeroPageAddr(),     A); cycles = 3; break;
			case 0x95: mem.write( readZeroPageXAddr(),    A); cycles = 4; break;
			case 0x8D: mem.write( readAbsoluteAddr(),     A); cycles = 4; break;
			case 0x9D: mem.write( readAbsoluteXAddr(),    A); cycles = 5; break;
			case 0x99: mem.write( readAbsoluteYAddr(),    A); cycles = 5; break;
			case 0x81: mem.write( readIndexedIndirectX(), A); cycles = 6; break;
			case 0x91: mem.write( readIndirectIndexedY(), A); cycles = 6; break;
			
			//STX
			case 0x86: mem.write( readZeroPageAddr(),     X); cycles = 3; break;
			case 0x96: mem.write( readZeroPageYAddr(),    X); cycles = 4; break;
			case 0x8E: mem.write( readAbsoluteAddr(),     X); cycles = 4; break;
			
			//STY
			case 0x84: mem.write( readZeroPageAddr(),     Y); cycles = 3; break;
			case 0x94: mem.write( readZeroPageXAddr(),    Y); cycles = 4; break;
			case 0x8C: mem.write( readAbsoluteAddr(),     Y); cycles = 4; break;
			
			//TAX
			case 0xAA: X = A; cycles = 2; setZN(X); break;
			
			//TAY
			case 0xA8: Y = A; cycles = 2; setZN(Y); break;
			
			//TSX
			case 0xBA: X = S; cycles = 2; setZN(X); break;
			
			//TXA
			case 0x8A: A = X; cycles = 2; setZN(A); break;
			
			//TXS
			case 0x9A: S = X; cycles = 2; break;
			
			//TYA
			case 0x98: A = Y; setZN(A); cycles = 2; break;
				
			//
			default: 
				throw new IllegalArgumentException("Unknown opCode: " + Integer.toHexString(opCode));
		}
		
		return cycles;
	}
	
	private void push(int b) {
		mem.write(STACK_START + ((S--) & 0xFF), b);
	}
	
	private int pull() {
		return mem.read(STACK_START + ((S++) & 0xFF));
	}
	
	private void pushAddr(int addr) {
		int adh = (addr >> 8) & 0xFF;
		int adl = addr & 0xFF;
		push(adh);
		push(adl);
	}
	
	private int pullAddr() {
		int adl = pull();
		int adh = pull();
		
		return fullAddr(adh, adl);
	}
	
	//
	
	private int readImmediateAddr() {
		cycles=2;
		return PC++;
	}
	
	private int readZeroPageAddr() {
		int adl = mem.read(PC++);
		cycles=3;
		return adl;
	}
	
	private int readZeroPageXAddr() {
		int adl = mem.read(PC++);
		int ad = (adl + X) & 0xFF;
		cycles = 4;
		return ad;
	}
	
	private int readZeroPageYAddr() {
		int adl = mem.read(PC++);
		int ad = (adl + Y) & 0xFF;
		cycles = 4;
		return ad;
	}
	
	private int readAbsoluteAddr() {
		int adl = mem.read(PC++);
		int adh = mem.read(PC++);
		int ad  = fullAddr(adh, adl);
		cycles = 4;
		return ad;
	}
	
	private int readAbsoluteXAddr() {
		int bal = mem.read(PC++);
		int bah = mem.read(PC++);
		int ba  = fullAddr(bah, bal);
		int ad   = ba + X;
		cycles = 4 + pageCross(ba, ad);
		return ad;
	}
	
	private int readAbsoluteYAddr() {
		int bal = mem.read(PC++);
		int bah = mem.read(PC++);
		int ba  = fullAddr(bah, bal);
		int ad  = ba + Y;
		cycles = 4 + pageCross(ba, ad);
		return ad;
	}
	
	private int readIndexedIndirectXAddr() {
		int ial = mem.read(PC++); //zero page index
		int adl = mem.read((ial + X + 0) & 0xFF);
		int adh = mem.read((ial + X + 1) & 0xFF);
		int ad  = fullAddr(adh, adl);
		cycles = 6;
		return ad;
	}
	
	private int readIndirectIndexedYAddr() {
		int ial = mem.read(PC++);
		int bal = mem.read((ial + 0) & 0xFF);
		int bah = mem.read((ial + 1) & 0xFF);
		int ba  = fullAddr(bah, bal);
		int ad  = ba + Y;
		cycles = 5 + pageCross(ba, ad);
		return ad;
	}
	
	private int readIndirectAddr() {
		int ial = mem.read(PC++);
		int adl = mem.read((ial + 0) & 0xFF);
		int adh = mem.read((ial + 1) & 0xFF);
		int ad  = fullAddr(adh, adl);
		cycles = 5;
		return ad;
	}
	
	//
	
	private int readImmediate() {
		return mem.read(readImmediateAddr());
	}
	
	private int readZeroPage() {
		return mem.read(readZeroPageAddr());
	}
	
	private int readZeroPageX() {
		return mem.read(readZeroPageXAddr());
	}
	
	private int readZeroPageY() {
		return mem.read(readZeroPageYAddr());
	}
	
	private int readAbsolute() {
		return mem.read(readAbsoluteAddr());		
	}
	
	private int readAbsoluteX() {
		return mem.read(readAbsoluteXAddr());
	}
	
	private int readAbsoluteY() {
		return mem.read(readAbsoluteYAddr());
	}
	
	private int readIndexedIndirectX() {
		return mem.read(readIndexedIndirectXAddr());
	}
	
	private int readIndirectIndexedY() {
		return mem.read(readIndirectIndexedYAddr());
	}
	
	private int readIndirect() {
		return mem.read(readIndirectAddr());
	}
	
	//
	
	private void setZN(int n) {
		setStatus( n == 0, STATUS_Z);
		setStatus( (n & 0x80) != 0, STATUS_N);
	}
	
	private void setStatus(boolean condition, int flag) {
		if( condition ) {
			P |= flag;
		} else {
			P &= ~flag;
		}
	}
	
	private void adc(int M) {
		if( (P & STATUS_D) != 0 ) {
			throw new IllegalStateException("Decimal flag not supported");
		}
		int oldSign = A & 0x80;
		int C = (P & STATUS_C) != 0 ? 1 : 0;
		A = A + M + C;
		int newSign = A & 0x80;
		setZN(A);
		setStatus( (A & 0x100) != 0, STATUS_C);
		setStatus( oldSign != newSign, STATUS_O);
		A &= 0xFF;
	}
	
	private void sbc(int M) {
		if( (P & STATUS_D) != 0 ) {
			throw new IllegalStateException("Decimal flag not supported");
		}
		int oldSign = A & 0x80;
		int C = (P & STATUS_C) != 0 ? 1 : 0;
		A = A - M - (1 - C);
		int newSign = A & 0x80;
		setZN(A);
		setStatus( (A & 0x100) != 0, STATUS_C);
		setStatus( oldSign != newSign, STATUS_O);
		A &= 0xFF;
	}
	
	private void aslAcc() {
		setStatus( (A & 0x80) != 0, STATUS_C );
		A = (A<<1) & 0xFF;
		setZN(A);
	}
	
	private void aslMem(int addr) {
		int M = mem.read(addr);
		setStatus( (M & 0x80) != 0, STATUS_C );
		M = (M << 1) & 0xFF;
		mem.write(addr, M);
		setZN(M);
	}
	
	private void lsrAcc() {
		setStatus( (A & 1) != 0, STATUS_C );
		A = (A >> 1) & 0xFF;
		setZN(A);
	}
	
	private void lsrMem(int addr) {
		int M = mem.read(addr);
		setStatus( (M & 1) != 0, STATUS_C );
		M = (M >> 1) & 0xFF;
		mem.write(addr, M);
		setZN(M);
	}
	
	private void rolAcc() {
		int oldBit7 = A & 0x80;
		A = (A << 1) & 0xFF;
		if( (P & STATUS_C) != 0 ) {
			A |= 1;
		}
		setStatus( oldBit7 != 0, STATUS_C);
		setZN(A);
	}
	
	private void rolMem(int addr) {
		int M = mem.read(addr);
		int oldBit7 = M & 0x80;
		M = (M << 1) & 0xFF;
		if( (P & STATUS_C) != 0 ) {
			M |= 1;
		}
		mem.write(addr, M);
		setStatus( oldBit7 != 0, STATUS_C);
		setZN(M);
	}
	
	private void rorAcc() {
		int oldBit0 = A & 0x1;
		A = (A >> 1) & 0xFF;
		if( (P & STATUS_C) != 0 ) {
			A |= 0x80;
		}
		setStatus( oldBit0 != 0, STATUS_C);
		setZN(A);
	}
	
	private void rorMem(int addr) {
		int M = mem.read(addr);
		int oldBit0 = M & 0x1;
		M = (M >> 1) & 0xFF;
		if( (P & STATUS_C) != 0 ) {
			M |= 0x80;
		}
		mem.write(addr, M);
		setStatus( oldBit0 != 0, STATUS_C);
		setZN(M);
	}
	
	private int pageCross(int ad1, int ad2) {
		 return (ad1 & 0xFF00) != (ad2 & 0xFF00) ? 1 : 0;
	}
	
	private int fullAddr(int adh, int adl) {
		return (adh << 8) | adl;
	}
	
	private void branchIfClear(int flag) {
		cycles = 2;
		int offset = (byte)readImmediate();
		
		if( (P & flag) == 0 ) {
			int oldPC = PC;
			PC += offset;
			cycles += 1 + pageCross(oldPC, PC);
		}
	}
	
	private void branchIfSet(int flag) {
		cycles = 2;
		int offset = (byte)readImmediate();
		
		if( (P & flag) != 0 ) {
			int oldPC = PC;
			PC += offset;
			cycles += 1 + pageCross(oldPC, PC);
		}
	}
	
	private void bit(int M) {
		setStatus( (A & M) == 0, STATUS_Z);
		setStatus( (M & STATUS_O) != 0, STATUS_O);
		setStatus( (M & STATUS_N) != 0, STATUS_N);
	}
	
	private void cmp(int V, int M) {
		int D = V - M;
		setStatus( V >= M, STATUS_C);
		setStatus( V == M, STATUS_Z);
		setStatus( (D & 0x80) != 0, STATUS_N);
	}
}
