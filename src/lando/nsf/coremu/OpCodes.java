package lando.nsf.coremu;

import static lando.nsf.coremu.AddrMode.*;

public final class OpCodes {

	public static OpCode[] OP_CODES = new OpCode[256];
	
	static {
		//http://www.obelisk.demon.co.uk/6502/reference.html
		OP_CODES[0x69] = new OpCode("ADC", 2, 2, IMMEDIATE);
		OP_CODES[0x65] = new OpCode("ADC", 2, 3, ZERO_PAGE);
		OP_CODES[0x75] = new OpCode("ADC", 2, 4, ZERO_PAGE_X);
		OP_CODES[0x6D] = new OpCode("ADC", 3, 4, ABSOLUTE);
		OP_CODES[0x7D] = new OpCode("ADC", 3, 4, ABSOLUTE_X);
		OP_CODES[0x79] = new OpCode("ADC", 3, 4, ABSOLUTE_Y);
		OP_CODES[0x61] = new OpCode("ADC", 2, 6, INDIRECT_X);
		OP_CODES[0x71] = new OpCode("ADC", 2, 5, INDIRECT_Y);
		
		OP_CODES[0x29] = new OpCode("AND", 2, 2, IMMEDIATE);
		OP_CODES[0x25] = new OpCode("AND", 2, 2, ZERO_PAGE);
		OP_CODES[0x35] = new OpCode("AND", 2, 3, ZERO_PAGE_X);
		OP_CODES[0x2D] = new OpCode("AND", 3, 4, ABSOLUTE);
		OP_CODES[0x3D] = new OpCode("AND", 3, 4, ABSOLUTE_X);
		OP_CODES[0x39] = new OpCode("AND", 3, 4, ABSOLUTE_Y);
		OP_CODES[0x21] = new OpCode("AND", 2, 6, INDIRECT_X);
		OP_CODES[0x31] = new OpCode("AND", 2, 5, INDIRECT_Y);
		
		OP_CODES[0x0A] = new OpCode("ASL", 1, 2, ACCUMULATOR);
		OP_CODES[0x06] = new OpCode("ASL", 2, 5, ZERO_PAGE);
		OP_CODES[0x16] = new OpCode("ASL", 2, 6, ZERO_PAGE_X);
		OP_CODES[0x0E] = new OpCode("ASL", 3, 6, ABSOLUTE);
		OP_CODES[0x1E] = new OpCode("ASL", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0x90] = new OpCode("BCC", 2, 2, RELATIVE);
		
		OP_CODES[0xB0] = new OpCode("BCS", 2, 2, RELATIVE);
		
		OP_CODES[0xF0] = new OpCode("BEQ", 2, 2, RELATIVE);
		
		OP_CODES[0x24] = new OpCode("BIT", 2, 3, ZERO_PAGE);
		OP_CODES[0x2C] = new OpCode("BIT", 3, 4, ABSOLUTE);
		
		OP_CODES[0x30] = new OpCode("BMI", 2, 2, RELATIVE);
		
		OP_CODES[0xD0] = new OpCode("BNE", 2, 2, RELATIVE);
		
		OP_CODES[0x10] = new OpCode("BPL", 2, 2, RELATIVE);
		
		OP_CODES[0x00] = new OpCode("BRK", 1, 7, IMPLIED);
		
		OP_CODES[0x50] = new OpCode("BVC", 2, 2, RELATIVE);
		
		OP_CODES[0x70] = new OpCode("BVS", 2, 2, RELATIVE);
		
		OP_CODES[0x18] = new OpCode("CLC", 1, 2, IMPLIED);
		
		OP_CODES[0xD8] = new OpCode("CLD", 1, 2, IMPLIED);
		
		OP_CODES[0x58] = new OpCode("CLI", 1, 2, IMPLIED);
		
		OP_CODES[0xB8] = new OpCode("CLV", 1, 2, IMPLIED);
		
		OP_CODES[0xC9] = new OpCode("CMP", 2, 2, IMMEDIATE);
		OP_CODES[0xC5] = new OpCode("CMP", 2, 3, ZERO_PAGE);
		OP_CODES[0xD5] = new OpCode("CMP", 2, 4, ZERO_PAGE_X);
		OP_CODES[0xCD] = new OpCode("CMP", 3, 4, ABSOLUTE);
		OP_CODES[0xDD] = new OpCode("CMP", 3, 4, ABSOLUTE_X);
		OP_CODES[0xD9] = new OpCode("CMP", 3, 4, ABSOLUTE_Y);
		OP_CODES[0xC1] = new OpCode("CMP", 2, 6, INDIRECT_X);
		OP_CODES[0xD1] = new OpCode("CMP", 2, 5, INDIRECT_Y);
		
		OP_CODES[0xE0] = new OpCode("CPX", 2, 2, IMMEDIATE);
		OP_CODES[0xE4] = new OpCode("CPX", 2, 3, ZERO_PAGE);
		OP_CODES[0xEC] = new OpCode("CPX", 3, 4, ABSOLUTE);
		
		OP_CODES[0xC0] = new OpCode("CPY", 2, 2, IMMEDIATE);
		OP_CODES[0xC4] = new OpCode("CPY", 2, 3, ZERO_PAGE);
		OP_CODES[0xCC] = new OpCode("CPY", 3, 4, ABSOLUTE);
		
		OP_CODES[0xC6] = new OpCode("DEC", 2, 5, ABSOLUTE);
		OP_CODES[0xD6] = new OpCode("DEC", 2, 6, ZERO_PAGE_X);
		OP_CODES[0xCE] = new OpCode("DEC", 3, 6, ABSOLUTE);
		OP_CODES[0xDE] = new OpCode("DEC", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0xCA] = new OpCode("DEX", 1, 2, IMPLIED);
		
		OP_CODES[0x88] = new OpCode("DEY", 1, 2, IMPLIED);
		
		OP_CODES[0x49] = new OpCode("EOR", 2, 2, IMMEDIATE);
		OP_CODES[0x45] = new OpCode("EOR", 2, 3, ZERO_PAGE);
		OP_CODES[0x55] = new OpCode("EOR", 2, 4, ZERO_PAGE_X);
		OP_CODES[0x4D] = new OpCode("EOR", 3, 4, ABSOLUTE);
		OP_CODES[0x5D] = new OpCode("EOR", 3, 4, ABSOLUTE_X);
		OP_CODES[0x59] = new OpCode("EOR", 3, 4, ABSOLUTE_Y);
		OP_CODES[0x41] = new OpCode("EOR", 2, 6, INDIRECT_X);
		OP_CODES[0x51] = new OpCode("EOR", 2, 5, INDIRECT_Y);
		
		OP_CODES[0xE6] = new OpCode("INC", 2, 5, ZERO_PAGE);
		OP_CODES[0xF6] = new OpCode("INC", 2, 6, ZERO_PAGE_X);
		OP_CODES[0xEE] = new OpCode("INC", 3, 6, ABSOLUTE);
		OP_CODES[0xFE] = new OpCode("INC", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0xE8] = new OpCode("INX", 1, 2, IMPLIED);
		
		OP_CODES[0xC8] = new OpCode("INY", 1, 2, IMPLIED);
		
		OP_CODES[0x4C] = new OpCode("JMP", 3, 3, ABSOLUTE);
		OP_CODES[0x6C] = new OpCode("JMP", 3, 5, INDIRECT);
		
		OP_CODES[0x20] = new OpCode("JSR", 3, 6, ABSOLUTE);
		
		OP_CODES[0xA9] = new OpCode("LDA", 2, 2, IMMEDIATE);
		OP_CODES[0xA5] = new OpCode("LDA", 2, 3, ZERO_PAGE);
		OP_CODES[0xB5] = new OpCode("LDA", 2, 4, ZERO_PAGE_X);
		OP_CODES[0xAD] = new OpCode("LDA", 3, 4, ABSOLUTE);
		OP_CODES[0xBD] = new OpCode("LDA", 3, 4, ABSOLUTE_X);
		OP_CODES[0xB9] = new OpCode("LDA", 3, 4, ABSOLUTE_Y);
		OP_CODES[0xA1] = new OpCode("LDA", 2, 6, INDIRECT_X);
		OP_CODES[0xB1] = new OpCode("LDA", 2, 5, INDIRECT_Y);
		
		OP_CODES[0xA2] = new OpCode("LDX", 2, 2, IMMEDIATE);
		OP_CODES[0xA6] = new OpCode("LDX", 2, 3, ZERO_PAGE);
		OP_CODES[0xB6] = new OpCode("LDX", 2, 4, ZERO_PAGE_Y);
		OP_CODES[0xAE] = new OpCode("LDX", 3, 4, ABSOLUTE);
		OP_CODES[0xBE] = new OpCode("LDX", 3, 4, ABSOLUTE_Y);
		
		OP_CODES[0xA0] = new OpCode("LDY", 2, 2, IMMEDIATE);
		OP_CODES[0xA4] = new OpCode("LDY", 2, 3, ZERO_PAGE);
		OP_CODES[0xB4] = new OpCode("LDY", 2, 4, ZERO_PAGE_Y);
		OP_CODES[0xAC] = new OpCode("LDY", 3, 4, ABSOLUTE);
		OP_CODES[0xBC] = new OpCode("LDY", 3, 4, ABSOLUTE_Y);
		
		OP_CODES[0x4A] = new OpCode("LSR", 1, 2, ACCUMULATOR);
		OP_CODES[0x46] = new OpCode("LSR", 2, 5, ZERO_PAGE);
		OP_CODES[0x56] = new OpCode("LSR", 2, 6, ZERO_PAGE_X);
		OP_CODES[0x4E] = new OpCode("LSR", 3, 6, ABSOLUTE);
		OP_CODES[0x5E] = new OpCode("LSR", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0xEA] = new OpCode("NOP", 1, 2, IMPLIED);
		
		OP_CODES[0x09] = new OpCode("ORA", 2, 2, IMMEDIATE);
		OP_CODES[0x05] = new OpCode("ORA", 2, 3, ZERO_PAGE);
		OP_CODES[0x15] = new OpCode("ORA", 2, 4, ZERO_PAGE_X);
		OP_CODES[0x0D] = new OpCode("ORA", 3, 4, ABSOLUTE);
		OP_CODES[0x1D] = new OpCode("ORA", 3, 4, ABSOLUTE_X);
		OP_CODES[0x19] = new OpCode("ORA", 3, 4, ABSOLUTE_Y);
		OP_CODES[0x01] = new OpCode("ORA", 2, 6, INDIRECT_X);
		OP_CODES[0x11] = new OpCode("ORA", 2, 5, INDIRECT_Y);
		
		OP_CODES[0x48] = new OpCode("PHA", 1, 3, IMPLIED);
		
		OP_CODES[0x08] = new OpCode("PHP", 1, 3, IMPLIED);
		
		OP_CODES[0x68] = new OpCode("PLA", 1, 4, IMPLIED);
		
		OP_CODES[0x28] = new OpCode("PLP", 1, 4, IMPLIED);
		
		OP_CODES[0x2A] = new OpCode("ROL", 1, 2, ACCUMULATOR);
		OP_CODES[0x26] = new OpCode("ROL", 2, 5, ZERO_PAGE);
		OP_CODES[0x36] = new OpCode("ROL", 2, 6, ZERO_PAGE_X);
		OP_CODES[0x2E] = new OpCode("ROL", 3, 6, ABSOLUTE);
		OP_CODES[0x3E] = new OpCode("ROL", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0x6A] = new OpCode("ROR", 1, 2, ACCUMULATOR);
		OP_CODES[0x66] = new OpCode("ROR", 2, 5, ZERO_PAGE);
		OP_CODES[0x76] = new OpCode("ROR", 2, 6, ZERO_PAGE_X);
		OP_CODES[0x6E] = new OpCode("ROR", 3, 6, ABSOLUTE);
		OP_CODES[0x7E] = new OpCode("ROR", 3, 7, ABSOLUTE_X);
		
		OP_CODES[0x40] = new OpCode("RTI", 1, 6, IMPLIED);
		
		OP_CODES[0x60] = new OpCode("RTS", 1, 6, IMPLIED);
		
		OP_CODES[0xE9] = new OpCode("SBC", 2, 2, IMMEDIATE);
		OP_CODES[0xE5] = new OpCode("SBC", 2, 2, ZERO_PAGE);
		OP_CODES[0xF5] = new OpCode("SBC", 2, 2, ZERO_PAGE_X);
		OP_CODES[0xED] = new OpCode("SBC", 3, 3, ABSOLUTE);
		OP_CODES[0xFD] = new OpCode("SBC", 3, 3, ABSOLUTE_X);
		OP_CODES[0xF9] = new OpCode("SBC", 3, 3, ABSOLUTE_Y);
		OP_CODES[0xE1] = new OpCode("SBC", 2, 2, INDIRECT_X);
		OP_CODES[0xF1] = new OpCode("SBC", 2, 2, INDIRECT_Y);
		
		OP_CODES[0x38] = new OpCode("SEC", 1, 2, IMPLIED);
		
		OP_CODES[0xF8] = new OpCode("SED", 1, 2, IMPLIED);
		
		OP_CODES[0x78] = new OpCode("SEI", 1, 2, IMPLIED);
		
		OP_CODES[0x85] = new OpCode("STA", 2, 3, ZERO_PAGE);
		OP_CODES[0x95] = new OpCode("STA", 2, 4, ZERO_PAGE_X);
		OP_CODES[0x8D] = new OpCode("STA", 3, 4, ABSOLUTE);
		OP_CODES[0x9D] = new OpCode("STA", 3, 5, ABSOLUTE_X);
		OP_CODES[0x99] = new OpCode("STA", 3, 5, ABSOLUTE_Y);
		OP_CODES[0x81] = new OpCode("STA", 2, 6, INDIRECT_X);
		OP_CODES[0x91] = new OpCode("STA", 2, 6, INDIRECT_Y);
		
		OP_CODES[0x86] = new OpCode("STX", 2, 3, ZERO_PAGE);
		OP_CODES[0x96] = new OpCode("STX", 2, 4, ZERO_PAGE_Y);
		OP_CODES[0x8E] = new OpCode("STX", 3, 4, ABSOLUTE);
		
		OP_CODES[0x84] = new OpCode("STY", 2, 3, ZERO_PAGE);
		OP_CODES[0x94] = new OpCode("STY", 2, 4, ZERO_PAGE_X);
		OP_CODES[0x8C] = new OpCode("STY", 3, 4, ABSOLUTE);
		
		OP_CODES[0xAA] = new OpCode("TAX", 1, 2, IMPLIED);
		OP_CODES[0xA8] = new OpCode("TAY", 1, 2, IMPLIED);
		
		OP_CODES[0xBA] = new OpCode("TSX", 1, 2, IMPLIED);
		
		OP_CODES[0x8A] = new OpCode("TXA", 1, 2, IMPLIED);
		
		OP_CODES[0x9A] = new OpCode("TXS", 1, 2, IMPLIED);
		
		OP_CODES[0x98] = new OpCode("TYA", 1, 2, IMPLIED);
	}
}
