package lando.nsf.coremu;

import org.apache.commons.lang3.Validate;

public final class OpCode {
	public final String name;
	public final int len;
	public final int cycles;
	public final AddrMode addrMode;
	
	public OpCode(String name, int len, int cycles, AddrMode addrMode) {
		Validate.notNull(name);
		Validate.notEmpty(name);
		
		Validate.isTrue(len >= 1 && len <= 3);
		
		this.name = name;
		this.len = len;
		this.cycles = cycles;
		this.addrMode = addrMode;
	}
}