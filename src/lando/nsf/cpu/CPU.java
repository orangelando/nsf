package lando.nsf.cpu;

import org.apache.commons.lang3.Validate;

public final class CPU {
    
    private static enum FlagStatus {
        SET, CLEAR
    }
    
    public static final int START_STATUS = 0b00_1_10000;

    public static final int NMI_VECTOR_ADDR   = 0xFF_FA;
    public static final int RESET_VECTOR_ADDR = 0xFF_FC;
    public static final int IRQ_VECTOR_ADDR   = 0xFF_FE;
    
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
	
	private int pageCrossed;
	private int branchTaken;
	private int branchedToNewPage;
	
	public int P  = START_STATUS; 
	public int PC = 0;
	public int A  = 0;
	public int X  = 0;
	public int Y  = 0;
	public int S  = 0xFF; 
		
	public CPU(Memory mem) {
		Validate.notNull(mem);
		this.mem = mem;
	}
	
	public int stackAddr() {
	    return STACK_START + (S&0xFF);
	}
	
	public int step() {
	    
		int cycles;
		int opCode = mem.read(PC++);
		
		/**
		 * I find having the constants in the case statements easier to search and debug
		 * than creating constant fields.
		 */
		switch(opCode) {
		
			//ADC
			case 0x69: adc(readImmediate());		 cycles = 2;               break;
			case 0x65: adc(readZeroPage ());		 cycles = 3;               break;
			case 0x75: adc(readZeroPageX()); 		 cycles = 4;               break;
			case 0x6D: adc(readAbsolute ()); 	     cycles = 4;               break;
			case 0x7D: adc(readAbsoluteX());		 cycles = 4 + pageCrossed; break;
			case 0x79: adc(readAbsoluteY());		 cycles = 4 + pageCrossed; break;
			case 0x61: adc(readIndexedIndirectX());	 cycles = 6;               break;
			case 0x71: adc(readIndirectIndexedY());	 cycles = 5 + pageCrossed; break;
			
			//AND
			case 0x29: and(readImmediate());         cycles = 2;               break;
			case 0x25: and(readZeroPage ());         cycles = 3;               break;
			case 0x35: and(readZeroPageX());         cycles = 4;               break;
			case 0x2D: and(readAbsolute ());         cycles = 4;               break;
			case 0x3D: and(readAbsoluteX());         cycles = 4 + pageCrossed; break;
			case 0x39: and(readAbsoluteY());         cycles = 4 + pageCrossed; break;
			case 0x21: and(readIndexedIndirectX());  cycles = 6;               break;
			case 0x31: and(readIndirectIndexedY());  cycles = 5 + pageCrossed; break;
			
			//ASL
			case 0x0A: aslAcc();                     cycles = 2; break;
			case 0x06: aslMem(readZeroPageAddr ());  cycles = 5; break;
			case 0x16: aslMem(readZeroPageXAddr());  cycles = 6; break;
			case 0x0E: aslMem(readAbsoluteAddr ());  cycles = 6; break;
			case 0x1E: aslMem(readAbsoluteXAddr());  cycles = 7; break;
			
			//BCC
			case 0x90: branchIfClear(STATUS_C); cycles = branchCycles(); break;
				
			//BCS
			case 0xB0: branchIfSet(STATUS_C);   cycles = branchCycles(); break;
				
			//BEQ
			case 0xF0: branchIfSet(STATUS_Z);   cycles = branchCycles(); break;
				
			//BIT
			case 0x24: bit(readZeroPage()); cycles = 3; break;
			case 0x2C: bit(readAbsolute()); cycles = 4; break;
			
			//BMI
			case 0x30: branchIfSet(STATUS_N);   cycles = branchCycles(); break;
			
			//BNE
			case 0xD0: branchIfClear(STATUS_Z); cycles = branchCycles(); break;
			
			//BPL
			case 0x10: branchIfClear(STATUS_N); cycles = branchCycles(); break;
			
			//BRK:
			case 0x00: brk(); cycles = 7; break;
			
			//BVC
			case 0x50: branchIfClear(STATUS_O); cycles = branchCycles(); break;
			
			//BVS
			case 0x70: branchIfSet(STATUS_O); cycles = branchCycles(); break;
			
			//CLC
			case 0x18: setStatus(false, STATUS_C); cycles = 2; break;
			
			//CLD
			case 0xD8: setStatus(false, STATUS_D); cycles = 2; break;
			
			//CLI
			case 0x58: setStatus(false, STATUS_I); cycles = 2; break;
			
			//CLV
			case 0xB8: setStatus(false, STATUS_O); cycles = 2; break;
			
			//CMP
			case 0xC9: cmp(A, readImmediate());        cycles = 2;               break;
			case 0xC5: cmp(A, readZeroPage ());        cycles = 3;               break;
			case 0xD5: cmp(A, readZeroPageX());        cycles = 4;               break;
			case 0xCD: cmp(A, readAbsolute ());        cycles = 4;               break;
			case 0xDD: cmp(A, readAbsoluteX());        cycles = 4 + pageCrossed; break;
			case 0xD9: cmp(A, readAbsoluteY());        cycles = 4 + pageCrossed; break;
			case 0xC1: cmp(A, readIndexedIndirectX()); cycles = 6;               break;
			case 0xD1: cmp(A, readIndirectIndexedY()); cycles = 5 + pageCrossed; break;
			
			//CPX
			case 0xE0: cmp(X, readImmediate()); cycles = 2; break;
			case 0xE4: cmp(X, readZeroPage ()); cycles = 3; break;
			case 0xEC: cmp(X, readAbsolute ()); cycles = 4; break;
			
			//CPY
			case 0xC0: cmp(Y, readImmediate()); cycles = 2; break;
			case 0xC4: cmp(Y, readZeroPage ()); cycles = 3; break;
			case 0xCC: cmp(Y, readAbsolute ()); cycles = 4; break;
			
			//DEC
			case 0xC6: dec(readZeroPageAddr());  cycles = 5; break;
			case 0xD6: dec(readZeroPageXAddr()); cycles = 6; break;
			case 0xCE: dec(readAbsoluteAddr());  cycles = 6; break;
			case 0xDE: dec(readAbsoluteXAddr());     cycles = 7; break;
			
			//DEX
			case 0xCA: dex(); cycles = 2; break;
			
			//DEY
			case 0x88: dey(); cycles = 2; break;
			
			//EOR
			case 0x49: eor(readImmediate());         cycles = 2;               break;
			case 0x45: eor(readZeroPage());          cycles = 3;               break;
			case 0x55: eor(readZeroPageX());         cycles = 4;               break;
			case 0x4D: eor(readAbsolute());          cycles = 4;               break;
			case 0x5D: eor(readAbsoluteX());         cycles = 4 + pageCrossed; break;
			case 0x59: eor(readAbsoluteY());         cycles = 4 + pageCrossed; break;
			case 0x41: eor(readIndexedIndirectX());  cycles = 6;               break;
			case 0x51: eor(readIndirectIndexedY());  cycles = 5 + pageCrossed; break;
			
			//INC
			case 0xE6: inc(readZeroPageAddr());  cycles = 5; break;
			case 0xF6: inc(readZeroPageXAddr()); cycles = 6; break;
			case 0xEE: inc(readAbsoluteAddr());  cycles = 6; break;
			case 0xFE: inc(readAbsoluteXAddr()); cycles = 7; break;
			
			//INX
			case 0xE8: inx(); cycles = 2; break;
			
			//INY
			case 0xC8: iny(); cycles = 2; break;
			
			//JMP
			case 0x4C: jmp(readAbsoluteAddr()); cycles = 3; break;
			case 0x6C: jmp(readIndirectAddr()); cycles = 5; break;
			
			//JSR
			case 0x20: jsr(); cycles = 6; break;
			
			//LDA
			case 0xA9: lda(readImmediate());        cycles = 2;               break;
			case 0xA5: lda(readZeroPage());         cycles = 3;               break;
			case 0xB5: lda(readZeroPageX());        cycles = 4;               break;
			case 0xAD: lda(readAbsolute());         cycles = 4;               break;
			case 0xBD: lda(readAbsoluteX());        cycles = 4 + pageCrossed; break;
			case 0xB9: lda(readAbsoluteY());        cycles = 4 + pageCrossed; break;
			case 0xA1: lda(readIndexedIndirectX()); cycles = 6;               break;
			case 0xB1: lda(readIndirectIndexedY()); cycles = 5 + pageCrossed; break;
				
			//LDX
			case 0xA2: ldx(readImmediate()); cycles = 2;               break;
			case 0xA6: ldx(readZeroPage());  cycles = 3;               break;
			case 0xB6: ldx(readZeroPageY()); cycles = 4;               break;
			case 0xAE: ldx(readAbsolute());  cycles = 4;               break;
			case 0xBE: ldx(readAbsoluteY()); cycles = 4 + pageCrossed; break;
			
			//LDY
			case 0xA0: ldy(readImmediate()); cycles = 2;               break;
			case 0xA4: ldy(readZeroPage());  cycles = 3;               break;
			case 0xB4: ldy(readZeroPageX()); cycles = 4;               break;
			case 0xAC: ldy(readAbsolute());  cycles = 4;               break;
			case 0xBC: ldy(readAbsoluteX()); cycles = 4 + pageCrossed; break;
			
			//LSR
			case 0x4A: lsrAcc();                    cycles = 2; break;
			case 0x46: lsrMem(readZeroPageAddr());  cycles = 5; break;
			case 0x56: lsrMem(readZeroPageXAddr()); cycles = 6; break;
			case 0x4E: lsrMem(readAbsoluteAddr());  cycles = 6; break;
			case 0x5E: lsrMem(readAbsoluteXAddr()); cycles = 7; break;
		
			//NOP
			case 0xEA: cycles = 2; break;
			
			//ORA
			case 0x09: ora(readImmediate());        cycles = 2;               break;
			case 0x05: ora(readZeroPage());         cycles = 3;               break;
			case 0x15: ora(readZeroPageX());        cycles = 4;               break;
			case 0x0D: ora(readAbsolute());         cycles = 4;               break;
			case 0x1D: ora(readAbsoluteX());        cycles = 4 + pageCrossed; break;
			case 0x19: ora(readAbsoluteY());        cycles = 4 + pageCrossed; break;
			case 0x01: ora(readIndexedIndirectX()); cycles = 6;               break;
			case 0x11: ora(readIndirectIndexedY()); cycles = 5 + pageCrossed; break;
			
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
			case 0x40: rti(); cycles = 6; break;
				
			//RTS
			case 0x60: rts(); cycles = 6; break;
				
			//SBC
			case 0xE9: sbc(readImmediate());        cycles = 2;               break;
			case 0xE5: sbc(readZeroPage());         cycles = 3;               break;
			case 0xF5: sbc(readZeroPageX());        cycles = 4;               break;
			case 0xED: sbc(readAbsolute());         cycles = 4;               break;
			case 0xFD: sbc(readAbsoluteX());        cycles = 4 + pageCrossed; break;
			case 0xF9: sbc(readAbsoluteY());        cycles = 4 + pageCrossed; break;
			case 0xE1: sbc(readIndexedIndirectX()); cycles = 6;               break;
			case 0xF1: sbc(readIndirectIndexedY()); cycles = 5 + pageCrossed; break;
			
			//SEC
			case 0x38: setStatus(true, STATUS_C); cycles = 2; break;
			
			//SED
			case 0xF8: setStatus(true, STATUS_D); cycles = 2; break;
			
			//SEI
			case 0x78: setStatus(true, STATUS_I); cycles = 2; break;
			
			//STA
			case 0x85: sta(readZeroPageAddr());     cycles = 3; break;
			case 0x95: sta(readZeroPageXAddr());    cycles = 4; break;
			case 0x8D: sta(readAbsoluteAddr());     cycles = 4; break;
			case 0x9D: sta(readAbsoluteXAddr());    cycles = 5; break;
			case 0x99: sta(readAbsoluteYAddr());    cycles = 5; break;
			case 0x81: sta(readIndexedIndirectXAddr()); cycles = 6; break;
			case 0x91: sta(readIndirectIndexedYAddr()); cycles = 6; break;
			
			//STX
			case 0x86: stx(readZeroPageAddr());      cycles = 3; break;
			case 0x96: stx(readZeroPageYAddr());     cycles = 4; break;
			case 0x8E: stx(readAbsoluteAddr());      cycles = 4; break;
			
			//STY
			case 0x84: sty(readZeroPageAddr());      cycles = 3; break;
			case 0x94: sty(readZeroPageXAddr());     cycles = 4; break;
			case 0x8C: sty(readAbsoluteAddr());      cycles = 4; break;
			
			//TAX
			case 0xAA: tax(); cycles = 2; break;
			
			//TAY
			case 0xA8: tay(); cycles = 2; break;
			
			//TSX
			case 0xBA: tsx(); cycles = 2; break;
			
			//TXA
			case 0x8A: txa(); cycles = 2; break;
			
			//TXS
			case 0x9A: txs(); cycles = 2; break;
			
			//TYA
			case 0x98: tya(); cycles = 2; break;
				
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
		return mem.read(STACK_START + ((++S) & 0xFF));
	}
	
	public void pushAddr(int addr) {
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
	
	private int branchCycles() {
	    return 2 + branchTaken + branchedToNewPage; 
	}
	
	//
	
	private int readImmediateAddr() {
		return PC++;
	}
	
	private int readZeroPageAddr() {
		int adl = mem.read(PC++);
		
		return adl;
	}
	
	private int readZeroPageXAddr() {
		int adl = mem.read(PC++);
		int ad = (adl + X) & 0xFF;
		
		return ad;
	}
	
	private int readZeroPageYAddr() {
		int adl = mem.read(PC++);
		int ad = (adl + Y) & 0xFF;
		
		return ad;
	}
	
	private int readAbsoluteAddr() {
		int adl = mem.read(PC++);
		int adh = mem.read(PC++);
		int ad  = fullAddr(adh, adl);
		
		return ad;
	}
	
	private int readAbsoluteXAddr() {
		int bal = mem.read(PC++);
		int bah = mem.read(PC++);
		int ba  = fullAddr(bah, bal);
		int ad  = ba + X;
		
		return ad;
	}
	
	private int readAbsoluteYAddr() {
		int bal = mem.read(PC++);
		int bah = mem.read(PC++);
		int ba  = fullAddr(bah, bal);
		int ad  = ba + Y;
		
		return ad;
	}
	
	private int readIndexedIndirectXAddr() {
		int ial = mem.read(PC++); //zero page index
		int adl = mem.read((ial + X + 0) & 0xFF);
		int adh = mem.read((ial + X + 1) & 0xFF);
		int ad  = fullAddr(adh, adl);
		
		return ad;
	}
	
	private int readIndirectIndexedYAddr() {
		int ial = mem.read(PC++);
		int bal = mem.read((ial + 0) & 0xFF);
		int bah = mem.read((ial + 1) & 0xFF);
		int ba  = fullAddr(bah, bal);
		int ad  = ba + Y;
		
		return ad;
	}
	
	private int readIndirectAddr() {
		int ial = mem.read(PC++);
		int adl = mem.read((ial + 0) & 0xFF);
		int adh = mem.read((ial + 1) & 0xFF);
		int ad  = fullAddr(adh, adl);
		
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
		
	//
	
	private void brk() {
        pushAddr(PC);
        push(P);
        PC = (mem.read(0xFFFF) << 8) | mem.read(0xFFFE);
        setStatus(true, STATUS_B);
	}
	
	private void setZN(int n) {
		setStatus( (n & 0xFF) == 0, STATUS_Z);
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
	
	private void and(int M) {
	    A = A & M;
	    setZN(A);
	}
	
	private void sbc(int M) {
	    
		if( (P & STATUS_D) != 0 ) {
			throw new IllegalStateException("Decimal flag not supported");
		}
		
        int oldSign = A & 0x80;
		int C = (P & STATUS_C) != 0 ? 1 : 0;
		A = A + ((~M + C) & 0xFF);
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
	
	private void dec(int addr) {
	    int M = mem.read(addr) - 1;
	    
	    mem.write(addr, M);
	    
	    setZN(M); 
	}
	
	private void dex() {
	    X = (X - 1) & 0xFF;
	    
	    setZN(X); 
	}
	
	private void dey() {
	    Y = (Y - 1) & 0xFF; 
	    
	    setZN(Y); 
	}
	
	private void eor(int M) {
	    A = A^M;
	    
	    setZN(A);
	}
	
   private void inc(int addr) {
        int M = mem.read(addr) + 1;
        
        mem.write(addr, M);
        
        setZN(M); 
    }
    
    private void inx() {
        X = (X + 1) & 0xFF;
        
        setZN(X); 
    }
    
    private void iny() {
        Y = (Y + 1) & 0xFF; 
        
        setZN(Y); 
    }
    
    private void jmp(int addr) {
        PC = addr;
    }
    
    private void jsr() {
        int addr = readAbsoluteAddr();
        pushAddr(PC - 1);
        PC = addr;
    }
    
    private void lda(int M) {
        A = M;
        setZN(A);
    }
    
    private void ldx(int M) {
        X = M;
        setZN(M);
    }
    
    private void ldy(int M) {
        Y = M;
        setZN(Y);
    }
    
    private void ora(int M) {
        A = A | M;
        setZN(A);
    }
    
    private void rti() {
        P = pull();
        PC = pullAddr();
    }
    
    private void rts() {
        PC = pullAddr();
        PC++; //"fix" PC (see page 108 of MOS 6502 programming manual)
    }

    private void sta(int addr) {
        mem.write(addr, A);
    }
    
    private void stx(int addr) {
        mem.write(addr, X);
    }
    
    private void sty(int addr) {
        mem.write(addr, Y);
    }
    
    private void tax() {
        X = A;
        
        setZN(X); 
    }
    
    private void tay() {
        Y = A; 
        
        setZN(Y); 
    }
    
    private void tsx() {
        X = S; 
        
        setZN(X); 
    }
    
    private void txa() {
        A = X; 
        
        setZN(A); 
    }
    
    private void txs() {
        S = X; 
    }
    
    private void tya() {
        A = Y; 
        
        setZN(A); 
    }
	
	private int pageCross(int ad1, int ad2) {
		 return (ad1 & 0xFF00) != (ad2 & 0xFF00) ? 1 : 0;
	}
	
	private int fullAddr(int adh, int adl) {
		return (adh << 8) | adl;
	}
	
	private void branchIfClear(int flag) {
	    branch(flag, FlagStatus.CLEAR);
	}
	
	private void branchIfSet(int flag) {
	    branch(flag, FlagStatus.SET);
	}
	
   private void branch(int flag, FlagStatus desiredStatus) {
        
        int offset = (byte)readImmediate();
        FlagStatus status = (P & flag) == 0 ? FlagStatus.CLEAR : FlagStatus.SET;
        
        if( status == desiredStatus ) {
            int oldPC = PC;
            
            PC += offset;
            
            branchTaken = 1;
            branchedToNewPage = pageCross(oldPC, PC);
        } else {
            branchTaken = 0;
            branchedToNewPage = 0;
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
