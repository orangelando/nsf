package lando.nsf.cpu;

import static lando.nsf.cpu.AddrMode.ABSOLUTE;
import static lando.nsf.cpu.AddrMode.ABSOLUTE_X;
import static lando.nsf.cpu.AddrMode.ABSOLUTE_Y;
import static lando.nsf.cpu.AddrMode.ACCUMULATOR;
import static lando.nsf.cpu.AddrMode.IMMEDIATE;
import static lando.nsf.cpu.AddrMode.IMPLIED;
import static lando.nsf.cpu.AddrMode.INDIRECT;
import static lando.nsf.cpu.AddrMode.INDIRECT_X;
import static lando.nsf.cpu.AddrMode.INDIRECT_Y;
import static lando.nsf.cpu.AddrMode.RELATIVE;
import static lando.nsf.cpu.AddrMode.ZERO_PAGE;
import static lando.nsf.cpu.AddrMode.ZERO_PAGE_X;
import static lando.nsf.cpu.AddrMode.ZERO_PAGE_Y;
import static lando.nsf.cpu.OpCodeName.ADC;
import static lando.nsf.cpu.OpCodeName.AND;
import static lando.nsf.cpu.OpCodeName.ASL;
import static lando.nsf.cpu.OpCodeName.BCC;
import static lando.nsf.cpu.OpCodeName.BCS;
import static lando.nsf.cpu.OpCodeName.BEQ;
import static lando.nsf.cpu.OpCodeName.BIT;
import static lando.nsf.cpu.OpCodeName.BMI;
import static lando.nsf.cpu.OpCodeName.BNE;
import static lando.nsf.cpu.OpCodeName.BPL;
import static lando.nsf.cpu.OpCodeName.BRK;
import static lando.nsf.cpu.OpCodeName.BVC;
import static lando.nsf.cpu.OpCodeName.BVS;
import static lando.nsf.cpu.OpCodeName.CLC;
import static lando.nsf.cpu.OpCodeName.CLD;
import static lando.nsf.cpu.OpCodeName.CLI;
import static lando.nsf.cpu.OpCodeName.CLV;
import static lando.nsf.cpu.OpCodeName.CMP;
import static lando.nsf.cpu.OpCodeName.CPX;
import static lando.nsf.cpu.OpCodeName.CPY;
import static lando.nsf.cpu.OpCodeName.DEC;
import static lando.nsf.cpu.OpCodeName.DEX;
import static lando.nsf.cpu.OpCodeName.DEY;
import static lando.nsf.cpu.OpCodeName.EOR;
import static lando.nsf.cpu.OpCodeName.INC;
import static lando.nsf.cpu.OpCodeName.INX;
import static lando.nsf.cpu.OpCodeName.INY;
import static lando.nsf.cpu.OpCodeName.JMP;
import static lando.nsf.cpu.OpCodeName.JSR;
import static lando.nsf.cpu.OpCodeName.LDA;
import static lando.nsf.cpu.OpCodeName.LDX;
import static lando.nsf.cpu.OpCodeName.LDY;
import static lando.nsf.cpu.OpCodeName.LSR;
import static lando.nsf.cpu.OpCodeName.NOP;
import static lando.nsf.cpu.OpCodeName.ORA;
import static lando.nsf.cpu.OpCodeName.PHA;
import static lando.nsf.cpu.OpCodeName.PHP;
import static lando.nsf.cpu.OpCodeName.PLA;
import static lando.nsf.cpu.OpCodeName.PLP;
import static lando.nsf.cpu.OpCodeName.ROL;
import static lando.nsf.cpu.OpCodeName.ROR;
import static lando.nsf.cpu.OpCodeName.RTI;
import static lando.nsf.cpu.OpCodeName.RTS;
import static lando.nsf.cpu.OpCodeName.SBC;
import static lando.nsf.cpu.OpCodeName.SEC;
import static lando.nsf.cpu.OpCodeName.SED;
import static lando.nsf.cpu.OpCodeName.SEI;
import static lando.nsf.cpu.OpCodeName.STA;
import static lando.nsf.cpu.OpCodeName.STX;
import static lando.nsf.cpu.OpCodeName.STY;
import static lando.nsf.cpu.OpCodeName.TAX;
import static lando.nsf.cpu.OpCodeName.TAY;
import static lando.nsf.cpu.OpCodeName.TSX;
import static lando.nsf.cpu.OpCodeName.TXA;
import static lando.nsf.cpu.OpCodeName.TXS;
import static lando.nsf.cpu.OpCodeName.TYA;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

/**
 *2018-12-29 moved to: 
 *   http://www.obelisk.me.uk/6502/reference.html
 * originally at:
 *   http://www.obelisk.demon.co.uk/6502/reference.html
 * 
 *
 */
public final class Instructions {

	public static Instruction[] BY_OP_CODE;
	public static Map<OpCodeName, Map<AddrMode, Instruction>> BY_NAME_AND_ADDR_MODE;
		
	public static List<Instruction> ALL_INSTRS = Arrays.asList(
	        
	        new Instruction(0x69, ADC, 2, IMMEDIATE),
	        new Instruction(0x65, ADC, 3, ZERO_PAGE),
	        new Instruction(0x75, ADC, 4, ZERO_PAGE_X),
	        new Instruction(0x6D, ADC, 4, ABSOLUTE),
	        new Instruction(0x7D, ADC, 4, ABSOLUTE_X),
	        new Instruction(0x79, ADC, 4, ABSOLUTE_Y),
	        new Instruction(0x61, ADC, 6, INDIRECT_X),
	        new Instruction(0x71, ADC, 5, INDIRECT_Y),
	                       
	        new Instruction(0x29, AND, 2, IMMEDIATE),
	        new Instruction(0x25, AND, 3, ZERO_PAGE),
	        new Instruction(0x35, AND, 4, ZERO_PAGE_X),
	        new Instruction(0x2D, AND, 4, ABSOLUTE),
	        new Instruction(0x3D, AND, 4, ABSOLUTE_X),
	        new Instruction(0x39, AND, 4, ABSOLUTE_Y),
	        new Instruction(0x21, AND, 6, INDIRECT_X),
	        new Instruction(0x31, AND, 5, INDIRECT_Y),
	                       
	        new Instruction(0x0A, ASL, 2, ACCUMULATOR),
	        new Instruction(0x06, ASL, 5, ZERO_PAGE),
	        new Instruction(0x16, ASL, 6, ZERO_PAGE_X),
	        new Instruction(0x0E, ASL, 6, ABSOLUTE),
	        new Instruction(0x1E, ASL, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x90, BCC, 2, RELATIVE),
	                       
	        new Instruction(0xB0, BCS, 2, RELATIVE),
	                       
	        new Instruction(0xF0, BEQ, 2, RELATIVE),
	                       
	        new Instruction(0x24, BIT, 3, ZERO_PAGE),
	        new Instruction(0x2C, BIT, 4, ABSOLUTE),
	                       
	        new Instruction(0x30, BMI, 2, RELATIVE),
	                       
	        new Instruction(0xD0, BNE, 2, RELATIVE),
	                       
	        new Instruction(0x10, BPL, 2, RELATIVE),
	                       
	        new Instruction(0x00, BRK, 7, IMPLIED),
	                       
	        new Instruction(0x50, BVC, 2, RELATIVE),
	                       
	        new Instruction(0x70, BVS, 2, RELATIVE),
	                       
	        new Instruction(0x18, CLC, 2, IMPLIED),
	                       
	        new Instruction(0xD8, CLD, 2, IMPLIED),
	                       
	        new Instruction(0x58, CLI, 2, IMPLIED),
	                       
	        new Instruction(0xB8, CLV, 2, IMPLIED),
	                       
	        new Instruction(0xC9, CMP, 2, IMMEDIATE),
	        new Instruction(0xC5, CMP, 3, ZERO_PAGE),
	        new Instruction(0xD5, CMP, 4, ZERO_PAGE_X),
	        new Instruction(0xCD, CMP, 4, ABSOLUTE),
	        new Instruction(0xDD, CMP, 4, ABSOLUTE_X),
	        new Instruction(0xD9, CMP, 4, ABSOLUTE_Y),
	        new Instruction(0xC1, CMP, 6, INDIRECT_X),
	        new Instruction(0xD1, CMP, 5, INDIRECT_Y),
	                       
	        new Instruction(0xE0, CPX, 2, IMMEDIATE),
	        new Instruction(0xE4, CPX, 3, ZERO_PAGE),
	        new Instruction(0xEC, CPX, 4, ABSOLUTE),
	                       
	        new Instruction(0xC0, CPY, 2, IMMEDIATE),
	        new Instruction(0xC4, CPY, 3, ZERO_PAGE),
	        new Instruction(0xCC, CPY, 4, ABSOLUTE),
	                       
	        new Instruction(0xC6, DEC, 5, ZERO_PAGE),
	        new Instruction(0xD6, DEC, 6, ZERO_PAGE_X),
	        new Instruction(0xCE, DEC, 6, ABSOLUTE),
	        new Instruction(0xDE, DEC, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xCA, DEX, 2, IMPLIED),
	                       
	        new Instruction(0x88, DEY, 2, IMPLIED),
	                       
	        new Instruction(0x49, EOR, 2, IMMEDIATE),
	        new Instruction(0x45, EOR, 3, ZERO_PAGE),
	        new Instruction(0x55, EOR, 4, ZERO_PAGE_X),
	        new Instruction(0x4D, EOR, 4, ABSOLUTE),
	        new Instruction(0x5D, EOR, 4, ABSOLUTE_X),
	        new Instruction(0x59, EOR, 4, ABSOLUTE_Y),
	        new Instruction(0x41, EOR, 6, INDIRECT_X),
	        new Instruction(0x51, EOR, 5, INDIRECT_Y),
	                       
	        new Instruction(0xE6, INC, 5, ZERO_PAGE),
	        new Instruction(0xF6, INC, 6, ZERO_PAGE_X),
	        new Instruction(0xEE, INC, 6, ABSOLUTE),
	        new Instruction(0xFE, INC, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xE8, INX, 2, IMPLIED),
	                       
	        new Instruction(0xC8, INY, 2, IMPLIED),
	                       
	        new Instruction(0x4C, JMP, 3, ABSOLUTE),
	        new Instruction(0x6C, JMP, 5, INDIRECT),
	                       
	        new Instruction(0x20, JSR, 6, ABSOLUTE),
	                       
	        new Instruction(0xA9, LDA, 2, IMMEDIATE),
	        new Instruction(0xA5, LDA, 3, ZERO_PAGE),
	        new Instruction(0xB5, LDA, 4, ZERO_PAGE_X),
	        new Instruction(0xAD, LDA, 4, ABSOLUTE),
	        new Instruction(0xBD, LDA, 4, ABSOLUTE_X),
	        new Instruction(0xB9, LDA, 4, ABSOLUTE_Y),
	        new Instruction(0xA1, LDA, 6, INDIRECT_X),
	        new Instruction(0xB1, LDA, 5, INDIRECT_Y),
	                       
	        new Instruction(0xA2, LDX, 2, IMMEDIATE),
	        new Instruction(0xA6, LDX, 3, ZERO_PAGE),
	        new Instruction(0xB6, LDX, 4, ZERO_PAGE_Y),
	        new Instruction(0xAE, LDX, 4, ABSOLUTE),
	        new Instruction(0xBE, LDX, 4, ABSOLUTE_Y),
	                       
	        new Instruction(0xA0, LDY, 2, IMMEDIATE),
	        new Instruction(0xA4, LDY, 3, ZERO_PAGE),
	        new Instruction(0xB4, LDY, 4, ZERO_PAGE_Y),
	        new Instruction(0xAC, LDY, 4, ABSOLUTE),
	        new Instruction(0xBC, LDY, 4, ABSOLUTE_Y),
	                       
	        new Instruction(0x4A, LSR, 2, ACCUMULATOR),
	        new Instruction(0x46, LSR, 5, ZERO_PAGE),
	        new Instruction(0x56, LSR, 6, ZERO_PAGE_X),
	        new Instruction(0x4E, LSR, 6, ABSOLUTE),
	        new Instruction(0x5E, LSR, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xEA, NOP, 2, IMPLIED),
	                       
	        new Instruction(0x09, ORA, 2, IMMEDIATE),
	        new Instruction(0x05, ORA, 3, ZERO_PAGE),
	        new Instruction(0x15, ORA, 4, ZERO_PAGE_X),
	        new Instruction(0x0D, ORA, 4, ABSOLUTE),
	        new Instruction(0x1D, ORA, 4, ABSOLUTE_X),
	        new Instruction(0x19, ORA, 4, ABSOLUTE_Y),
	        new Instruction(0x01, ORA, 6, INDIRECT_X),
	        new Instruction(0x11, ORA, 5, INDIRECT_Y),
	                       
	        new Instruction(0x48, PHA, 3, IMPLIED),
	                       
	        new Instruction(0x08, PHP, 3, IMPLIED),
	                       
	        new Instruction(0x68, PLA, 4, IMPLIED),
	                       
	        new Instruction(0x28, PLP, 4, IMPLIED),
	                       
	        new Instruction(0x2A, ROL, 2, ACCUMULATOR),
	        new Instruction(0x26, ROL, 5, ZERO_PAGE),
	        new Instruction(0x36, ROL, 6, ZERO_PAGE_X),
	        new Instruction(0x2E, ROL, 6, ABSOLUTE),
	        new Instruction(0x3E, ROL, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x6A, ROR, 2, ACCUMULATOR),
	        new Instruction(0x66, ROR, 5, ZERO_PAGE),
	        new Instruction(0x76, ROR, 6, ZERO_PAGE_X),
	        new Instruction(0x6E, ROR, 6, ABSOLUTE),
	        new Instruction(0x7E, ROR, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x40, RTI, 6, IMPLIED),
	                       
	        new Instruction(0x60, RTS, 6, IMPLIED),
	                       
	        new Instruction(0xE9, SBC, 2, IMMEDIATE),
	        new Instruction(0xE5, SBC, 3, ZERO_PAGE),
	        new Instruction(0xF5, SBC, 4, ZERO_PAGE_X),
	        new Instruction(0xED, SBC, 4, ABSOLUTE),
	        new Instruction(0xFD, SBC, 4, ABSOLUTE_X),
	        new Instruction(0xF9, SBC, 4, ABSOLUTE_Y),
	        new Instruction(0xE1, SBC, 6, INDIRECT_X),
	        new Instruction(0xF1, SBC, 5, INDIRECT_Y),
	                       
	        new Instruction(0x38, SEC, 2, IMPLIED),
	                       
	        new Instruction(0xF8, SED, 2, IMPLIED),
	                       
	        new Instruction(0x78, SEI, 2, IMPLIED),
	                       
	        new Instruction(0x85, STA, 3, ZERO_PAGE),
	        new Instruction(0x95, STA, 4, ZERO_PAGE_X),
	        new Instruction(0x8D, STA, 4, ABSOLUTE),
	        new Instruction(0x9D, STA, 5, ABSOLUTE_X),
	        new Instruction(0x99, STA, 5, ABSOLUTE_Y),
	        new Instruction(0x81, STA, 6, INDIRECT_X),
	        new Instruction(0x91, STA, 6, INDIRECT_Y),
	                       
	        new Instruction(0x86, STX, 3, ZERO_PAGE),
	        new Instruction(0x96, STX, 4, ZERO_PAGE_Y),
	        new Instruction(0x8E, STX, 4, ABSOLUTE),
	                       
	        new Instruction(0x84, STY, 3, ZERO_PAGE),
	        new Instruction(0x94, STY, 4, ZERO_PAGE_X),
	        new Instruction(0x8C, STY, 4, ABSOLUTE),
	                       
	        new Instruction(0xAA, TAX, 2, IMPLIED),
	        new Instruction(0xA8, TAY, 2, IMPLIED),
	                       
	        new Instruction(0xBA, TSX, 2, IMPLIED),
	                       
	        new Instruction(0x8A, TXA, 2, IMPLIED),
	                       
	        new Instruction(0x9A, TXS, 2, IMPLIED),
	                       
	        new Instruction(0x98, TYA, 2, IMPLIED)
	        );
	
	static {
	    InstructionsMap map = new InstructionsMap();
	    
	    BY_OP_CODE = map.buildOpCodeArray(ALL_INSTRS);
	    BY_NAME_AND_ADDR_MODE = map.buildNameMap(ALL_INSTRS);
    }
	
	public static Optional<Instruction> byOpCode(int code) {
	    return Optional.ofNullable(BY_OP_CODE[code]);
	}
	
	public static Optional<Instruction> byNameAndAddrMode(OpCodeName name, AddrMode addrMode) {
	    Validate.notNull(name);
	    Validate.notNull(addrMode);
	    
	    return Optional.ofNullable(BY_NAME_AND_ADDR_MODE.get(name))
	        .flatMap(addrMap -> Optional.ofNullable(addrMap.get(addrMode)));
	}
}
