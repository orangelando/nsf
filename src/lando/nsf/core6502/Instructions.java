package lando.nsf.core6502;

import static lando.nsf.core6502.AddrMode.ABSOLUTE;
import static lando.nsf.core6502.AddrMode.ABSOLUTE_X;
import static lando.nsf.core6502.AddrMode.ABSOLUTE_Y;
import static lando.nsf.core6502.AddrMode.ACCUMULATOR;
import static lando.nsf.core6502.AddrMode.IMMEDIATE;
import static lando.nsf.core6502.AddrMode.IMPLIED;
import static lando.nsf.core6502.AddrMode.INDIRECT;
import static lando.nsf.core6502.AddrMode.INDIRECT_X;
import static lando.nsf.core6502.AddrMode.INDIRECT_Y;
import static lando.nsf.core6502.AddrMode.RELATIVE;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE_X;
import static lando.nsf.core6502.AddrMode.ZERO_PAGE_Y;
import static lando.nsf.core6502.OpCodeName.ADC;
import static lando.nsf.core6502.OpCodeName.AND;
import static lando.nsf.core6502.OpCodeName.ASL;
import static lando.nsf.core6502.OpCodeName.BCC;
import static lando.nsf.core6502.OpCodeName.BCS;
import static lando.nsf.core6502.OpCodeName.BEQ;
import static lando.nsf.core6502.OpCodeName.BIT;
import static lando.nsf.core6502.OpCodeName.BMI;
import static lando.nsf.core6502.OpCodeName.BNE;
import static lando.nsf.core6502.OpCodeName.BPL;
import static lando.nsf.core6502.OpCodeName.BRK;
import static lando.nsf.core6502.OpCodeName.BVC;
import static lando.nsf.core6502.OpCodeName.BVS;
import static lando.nsf.core6502.OpCodeName.CLC;
import static lando.nsf.core6502.OpCodeName.CLD;
import static lando.nsf.core6502.OpCodeName.CLI;
import static lando.nsf.core6502.OpCodeName.CLV;
import static lando.nsf.core6502.OpCodeName.CMP;
import static lando.nsf.core6502.OpCodeName.CPX;
import static lando.nsf.core6502.OpCodeName.CPY;
import static lando.nsf.core6502.OpCodeName.DEC;
import static lando.nsf.core6502.OpCodeName.DEX;
import static lando.nsf.core6502.OpCodeName.DEY;
import static lando.nsf.core6502.OpCodeName.EOR;
import static lando.nsf.core6502.OpCodeName.INC;
import static lando.nsf.core6502.OpCodeName.INX;
import static lando.nsf.core6502.OpCodeName.INY;
import static lando.nsf.core6502.OpCodeName.JMP;
import static lando.nsf.core6502.OpCodeName.JSR;
import static lando.nsf.core6502.OpCodeName.LDA;
import static lando.nsf.core6502.OpCodeName.LDX;
import static lando.nsf.core6502.OpCodeName.LDY;
import static lando.nsf.core6502.OpCodeName.LSR;
import static lando.nsf.core6502.OpCodeName.NOP;
import static lando.nsf.core6502.OpCodeName.ORA;
import static lando.nsf.core6502.OpCodeName.PHA;
import static lando.nsf.core6502.OpCodeName.PHP;
import static lando.nsf.core6502.OpCodeName.PLA;
import static lando.nsf.core6502.OpCodeName.PLP;
import static lando.nsf.core6502.OpCodeName.ROL;
import static lando.nsf.core6502.OpCodeName.ROR;
import static lando.nsf.core6502.OpCodeName.RTI;
import static lando.nsf.core6502.OpCodeName.RTS;
import static lando.nsf.core6502.OpCodeName.SBC;
import static lando.nsf.core6502.OpCodeName.SEC;
import static lando.nsf.core6502.OpCodeName.SED;
import static lando.nsf.core6502.OpCodeName.SEI;
import static lando.nsf.core6502.OpCodeName.STA;
import static lando.nsf.core6502.OpCodeName.STX;
import static lando.nsf.core6502.OpCodeName.STY;
import static lando.nsf.core6502.OpCodeName.TAX;
import static lando.nsf.core6502.OpCodeName.TAY;
import static lando.nsf.core6502.OpCodeName.TSX;
import static lando.nsf.core6502.OpCodeName.TXA;
import static lando.nsf.core6502.OpCodeName.TXS;
import static lando.nsf.core6502.OpCodeName.TYA;

import java.util.Arrays;
import java.util.EnumMap;
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

	public static Instruction[] BY_OP_CODE = 
	        new Instruction[256];
	
	public static Map<OpCodeName, Map<AddrMode, Instruction>> BY_NAME_AND_ADDR_MODE = 
	        new EnumMap<>(OpCodeName.class);
		
	public static List<Instruction> ALL_INSTRS = Arrays.asList(
	        
	        new Instruction(0x69, ADC, 2, 2, IMMEDIATE),
	        new Instruction(0x65, ADC, 2, 3, ZERO_PAGE),
	        new Instruction(0x75, ADC, 2, 4, ZERO_PAGE_X),
	        new Instruction(0x6D, ADC, 3, 4, ABSOLUTE),
	        new Instruction(0x7D, ADC, 3, 4, ABSOLUTE_X),
	        new Instruction(0x79, ADC, 3, 4, ABSOLUTE_Y),
	        new Instruction(0x61, ADC, 2, 6, INDIRECT_X),
	        new Instruction(0x71, ADC, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0x29, AND, 2, 2, IMMEDIATE),
	        new Instruction(0x25, AND, 2, 2, ZERO_PAGE),
	        new Instruction(0x35, AND, 2, 3, ZERO_PAGE_X),
	        new Instruction(0x2D, AND, 3, 4, ABSOLUTE),
	        new Instruction(0x3D, AND, 3, 4, ABSOLUTE_X),
	        new Instruction(0x39, AND, 3, 4, ABSOLUTE_Y),
	        new Instruction(0x21, AND, 2, 6, INDIRECT_X),
	        new Instruction(0x31, AND, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0x0A, ASL, 1, 2, ACCUMULATOR),
	        new Instruction(0x06, ASL, 2, 5, ZERO_PAGE),
	        new Instruction(0x16, ASL, 2, 6, ZERO_PAGE_X),
	        new Instruction(0x0E, ASL, 3, 6, ABSOLUTE),
	        new Instruction(0x1E, ASL, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x90, BCC, 2, 2, RELATIVE),
	                       
	        new Instruction(0xB0, BCS, 2, 2, RELATIVE),
	                       
	        new Instruction(0xF0, BEQ, 2, 2, RELATIVE),
	                       
	        new Instruction(0x24, BIT, 2, 3, ZERO_PAGE),
	        new Instruction(0x2C, BIT, 3, 4, ABSOLUTE),
	                       
	        new Instruction(0x30, BMI, 2, 2, RELATIVE),
	                       
	        new Instruction(0xD0, BNE, 2, 2, RELATIVE),
	                       
	        new Instruction(0x10, BPL, 2, 2, RELATIVE),
	                       
	        new Instruction(0x00, BRK, 1, 7, IMPLIED),
	                       
	        new Instruction(0x50, BVC, 2, 2, RELATIVE),
	                       
	        new Instruction(0x70, BVS, 2, 2, RELATIVE),
	                       
	        new Instruction(0x18, CLC, 1, 2, IMPLIED),
	                       
	        new Instruction(0xD8, CLD, 1, 2, IMPLIED),
	                       
	        new Instruction(0x58, CLI, 1, 2, IMPLIED),
	                       
	        new Instruction(0xB8, CLV, 1, 2, IMPLIED),
	                       
	        new Instruction(0xC9, CMP, 2, 2, IMMEDIATE),
	        new Instruction(0xC5, CMP, 2, 3, ZERO_PAGE),
	        new Instruction(0xD5, CMP, 2, 4, ZERO_PAGE_X),
	        new Instruction(0xCD, CMP, 3, 4, ABSOLUTE),
	        new Instruction(0xDD, CMP, 3, 4, ABSOLUTE_X),
	        new Instruction(0xD9, CMP, 3, 4, ABSOLUTE_Y),
	        new Instruction(0xC1, CMP, 2, 6, INDIRECT_X),
	        new Instruction(0xD1, CMP, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0xE0, CPX, 2, 2, IMMEDIATE),
	        new Instruction(0xE4, CPX, 2, 3, ZERO_PAGE),
	        new Instruction(0xEC, CPX, 3, 4, ABSOLUTE),
	                       
	        new Instruction(0xC0, CPY, 2, 2, IMMEDIATE),
	        new Instruction(0xC4, CPY, 2, 3, ZERO_PAGE),
	        new Instruction(0xCC, CPY, 3, 4, ABSOLUTE),
	                       
	        new Instruction(0xC6, DEC, 2, 5, ZERO_PAGE),
	        new Instruction(0xD6, DEC, 2, 6, ZERO_PAGE_X),
	        new Instruction(0xCE, DEC, 3, 6, ABSOLUTE),
	        new Instruction(0xDE, DEC, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xCA, DEX, 1, 2, IMPLIED),
	                       
	        new Instruction(0x88, DEY, 1, 2, IMPLIED),
	                       
	        new Instruction(0x49, EOR, 2, 2, IMMEDIATE),
	        new Instruction(0x45, EOR, 2, 3, ZERO_PAGE),
	        new Instruction(0x55, EOR, 2, 4, ZERO_PAGE_X),
	        new Instruction(0x4D, EOR, 3, 4, ABSOLUTE),
	        new Instruction(0x5D, EOR, 3, 4, ABSOLUTE_X),
	        new Instruction(0x59, EOR, 3, 4, ABSOLUTE_Y),
	        new Instruction(0x41, EOR, 2, 6, INDIRECT_X),
	        new Instruction(0x51, EOR, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0xE6, INC, 2, 5, ZERO_PAGE),
	        new Instruction(0xF6, INC, 2, 6, ZERO_PAGE_X),
	        new Instruction(0xEE, INC, 3, 6, ABSOLUTE),
	        new Instruction(0xFE, INC, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xE8, INX, 1, 2, IMPLIED),
	                       
	        new Instruction(0xC8, INY, 1, 2, IMPLIED),
	                       
	        new Instruction(0x4C, JMP, 3, 3, ABSOLUTE),
	        new Instruction(0x6C, JMP, 3, 5, INDIRECT),
	                       
	        new Instruction(0x20, JSR, 3, 6, ABSOLUTE),
	                       
	        new Instruction(0xA9, LDA, 2, 2, IMMEDIATE),
	        new Instruction(0xA5, LDA, 2, 3, ZERO_PAGE),
	        new Instruction(0xB5, LDA, 2, 4, ZERO_PAGE_X),
	        new Instruction(0xAD, LDA, 3, 4, ABSOLUTE),
	        new Instruction(0xBD, LDA, 3, 4, ABSOLUTE_X),
	        new Instruction(0xB9, LDA, 3, 4, ABSOLUTE_Y),
	        new Instruction(0xA1, LDA, 2, 6, INDIRECT_X),
	        new Instruction(0xB1, LDA, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0xA2, LDX, 2, 2, IMMEDIATE),
	        new Instruction(0xA6, LDX, 2, 3, ZERO_PAGE),
	        new Instruction(0xB6, LDX, 2, 4, ZERO_PAGE_Y),
	        new Instruction(0xAE, LDX, 3, 4, ABSOLUTE),
	        new Instruction(0xBE, LDX, 3, 4, ABSOLUTE_Y),
	                       
	        new Instruction(0xA0, LDY, 2, 2, IMMEDIATE),
	        new Instruction(0xA4, LDY, 2, 3, ZERO_PAGE),
	        new Instruction(0xB4, LDY, 2, 4, ZERO_PAGE_Y),
	        new Instruction(0xAC, LDY, 3, 4, ABSOLUTE),
	        new Instruction(0xBC, LDY, 3, 4, ABSOLUTE_Y),
	                       
	        new Instruction(0x4A, LSR, 1, 2, ACCUMULATOR),
	        new Instruction(0x46, LSR, 2, 5, ZERO_PAGE),
	        new Instruction(0x56, LSR, 2, 6, ZERO_PAGE_X),
	        new Instruction(0x4E, LSR, 3, 6, ABSOLUTE),
	        new Instruction(0x5E, LSR, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0xEA, NOP, 1, 2, IMPLIED),
	                       
	        new Instruction(0x09, ORA, 2, 2, IMMEDIATE),
	        new Instruction(0x05, ORA, 2, 3, ZERO_PAGE),
	        new Instruction(0x15, ORA, 2, 4, ZERO_PAGE_X),
	        new Instruction(0x0D, ORA, 3, 4, ABSOLUTE),
	        new Instruction(0x1D, ORA, 3, 4, ABSOLUTE_X),
	        new Instruction(0x19, ORA, 3, 4, ABSOLUTE_Y),
	        new Instruction(0x01, ORA, 2, 6, INDIRECT_X),
	        new Instruction(0x11, ORA, 2, 5, INDIRECT_Y),
	                       
	        new Instruction(0x48, PHA, 1, 3, IMPLIED),
	                       
	        new Instruction(0x08, PHP, 1, 3, IMPLIED),
	                       
	        new Instruction(0x68, PLA, 1, 4, IMPLIED),
	                       
	        new Instruction(0x28, PLP, 1, 4, IMPLIED),
	                       
	        new Instruction(0x2A, ROL, 1, 2, ACCUMULATOR),
	        new Instruction(0x26, ROL, 2, 5, ZERO_PAGE),
	        new Instruction(0x36, ROL, 2, 6, ZERO_PAGE_X),
	        new Instruction(0x2E, ROL, 3, 6, ABSOLUTE),
	        new Instruction(0x3E, ROL, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x6A, ROR, 1, 2, ACCUMULATOR),
	        new Instruction(0x66, ROR, 2, 5, ZERO_PAGE),
	        new Instruction(0x76, ROR, 2, 6, ZERO_PAGE_X),
	        new Instruction(0x6E, ROR, 3, 6, ABSOLUTE),
	        new Instruction(0x7E, ROR, 3, 7, ABSOLUTE_X),
	                       
	        new Instruction(0x40, RTI, 1, 6, IMPLIED),
	                       
	        new Instruction(0x60, RTS, 1, 6, IMPLIED),
	                       
	        new Instruction(0xE9, SBC, 2, 2, IMMEDIATE),
	        new Instruction(0xE5, SBC, 2, 2, ZERO_PAGE),
	        new Instruction(0xF5, SBC, 2, 2, ZERO_PAGE_X),
	        new Instruction(0xED, SBC, 3, 3, ABSOLUTE),
	        new Instruction(0xFD, SBC, 3, 3, ABSOLUTE_X),
	        new Instruction(0xF9, SBC, 3, 3, ABSOLUTE_Y),
	        new Instruction(0xE1, SBC, 2, 2, INDIRECT_X),
	        new Instruction(0xF1, SBC, 2, 2, INDIRECT_Y),
	                       
	        new Instruction(0x38, SEC, 1, 2, IMPLIED),
	                       
	        new Instruction(0xF8, SED, 1, 2, IMPLIED),
	                       
	        new Instruction(0x78, SEI, 1, 2, IMPLIED),
	                       
	        new Instruction(0x85, STA, 2, 3, ZERO_PAGE),
	        new Instruction(0x95, STA, 2, 4, ZERO_PAGE_X),
	        new Instruction(0x8D, STA, 3, 4, ABSOLUTE),
	        new Instruction(0x9D, STA, 3, 5, ABSOLUTE_X),
	        new Instruction(0x99, STA, 3, 5, ABSOLUTE_Y),
	        new Instruction(0x81, STA, 2, 6, INDIRECT_X),
	        new Instruction(0x91, STA, 2, 6, INDIRECT_Y),
	                       
	        new Instruction(0x86, STX, 2, 3, ZERO_PAGE),
	        new Instruction(0x96, STX, 2, 4, ZERO_PAGE_Y),
	        new Instruction(0x8E, STX, 3, 4, ABSOLUTE),
	                       
	        new Instruction(0x84, STY, 2, 3, ZERO_PAGE),
	        new Instruction(0x94, STY, 2, 4, ZERO_PAGE_X),
	        new Instruction(0x8C, STY, 3, 4, ABSOLUTE),
	                       
	        new Instruction(0xAA, TAX, 1, 2, IMPLIED),
	        new Instruction(0xA8, TAY, 1, 2, IMPLIED),
	                       
	        new Instruction(0xBA, TSX, 1, 2, IMPLIED),
	                       
	        new Instruction(0x8A, TXA, 1, 2, IMPLIED),
	                       
	        new Instruction(0x9A, TXS, 1, 2, IMPLIED),
	                       
	        new Instruction(0x98, TYA, 1, 2, IMPLIED)
	        );
	
	static {
	
	    for(Instruction instr: ALL_INSTRS) {
	        Validate.isTrue( BY_OP_CODE[instr.opCode] == null );
	        
	        BY_OP_CODE[instr.opCode] = instr;
	        
	        Map<AddrMode, Instruction> addrMap = BY_NAME_AND_ADDR_MODE.get(instr.name);
	        
	        if( addrMap == null ) {
	            addrMap = new EnumMap<>(AddrMode.class);
	            BY_NAME_AND_ADDR_MODE.put(instr.name, addrMap);
	        }
	        
	        Validate.isTrue( ! addrMap.containsKey(instr.addrMode), 
	                "Dupe entry for " + instr.name + ", " + instr.addrMode);
	        
	        addrMap.put(instr.addrMode, instr);
	    }
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
