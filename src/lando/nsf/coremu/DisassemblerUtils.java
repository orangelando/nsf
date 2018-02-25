package lando.nsf.coremu;

public final class DisassemblerUtils {

	public static String opCodeText(int b1, int b2, int b3) {
		
		if( b1 < 0 || b1 >= OpCodes.OP_CODES.length) {
			return "???";
		}
		
		OpCode opInfo = OpCodes.OP_CODES[b1];
		
		if( opInfo == null ) {
			return "???";
		}
		
		String arg = "";
		
		switch(opInfo.len) {
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
