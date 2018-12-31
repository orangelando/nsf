package lando.nsf.core6502;

import org.apache.commons.lang3.Validate;

public enum AddrMode {
	IMMEDIATE  (2),
	ZERO_PAGE  (2),
	ZERO_PAGE_X(2),
	ZERO_PAGE_Y(2),
	ABSOLUTE   (3),
	ABSOLUTE_X (3),
	ABSOLUTE_Y (3),
	INDIRECT_X (2), //INDEXED_INDIRECT (INDIRECT,X)
	INDIRECT_Y (2), //INDIRECT_INDEXED (INDIRECT),Y
	ACCUMULATOR(1), 
	RELATIVE   (2), 
	IMPLIED    (1), 
	INDIRECT   (3); //(INDIRECT)
    
    public final int instrLen;
    
    private AddrMode(int instrLen) {
        Validate.isTrue(instrLen >= 1 && instrLen <= 3);

        this.instrLen = instrLen;
    }
}
