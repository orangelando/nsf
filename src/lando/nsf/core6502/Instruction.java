package lando.nsf.core6502;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class Instruction {
    public final int opCode;
	public final OpCodeName name;
	public final int cycles;
	public final AddrMode addrMode;
	
	public Instruction(int opCode, OpCodeName name, int cycles, AddrMode addrMode) {
	    Validate.isTrue(opCode >= 0 && opCode <= 255);
		Validate.notNull(name);
		Validate.notNull(addrMode);
		
		Validate.isTrue(cycles >= 1);
		
		this.opCode = opCode;
		this.name = name;
		this.cycles = cycles;
		this.addrMode = addrMode;
	}
	
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}
}