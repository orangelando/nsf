package lando.nsf;

import lando.nsf.core6502.Instruction;
import lando.nsf.core6502.Instructions;

public final class DisassemblerUtils {

	public static String opCodeText(int b1, int b2, int b3) {
		
		if( b1 < 0 || b1 >= Instructions.BY_OP_CODE.length) {
			return "???";
		}
		
		Instruction opInfo = Instructions.BY_OP_CODE[b1];
		
		if( opInfo == null ) {
			return "???";
		}
		
		String arg = "";
		
		switch(opInfo.addrMode.instrLen) {
			case 2: arg = HexUtils.toHex8(b2); break;
			case 3: arg = HexUtils.toHex16(b2 | (b3 << 8)); break;
		}
		
		switch(opInfo.addrMode) {
			case IMMEDIATE   : arg = "#" + arg; break;
			case ZERO_PAGE   : break;
			case ZERO_PAGE_X : arg = arg + ",X"; break;
			case ZERO_PAGE_Y : arg = arg + ",Y"; break;
			case ABSOLUTE    : break;
			case ABSOLUTE_X  : arg = arg + ",X"; break;
			case ABSOLUTE_Y  : arg = arg + ",Y"; break;
			case INDIRECT_X  : arg = "(" + arg + ",X)"; break;
			case INDIRECT_Y  : arg = "(" + arg + ",Y"; break; 
			case ACCUMULATOR : arg = "A" + arg; break; 
			case RELATIVE    : break; 
			case IMPLIED     : break; 
			case INDIRECT    : arg = "(" + arg + ")"; break;
		}
		
		return opInfo.name + " " + arg;
	}
}
