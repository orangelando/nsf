package lando.nsf.core6502;

import org.apache.commons.lang3.Validate;

final class DataDiff {

    final int addr;
    final int expected;
    final int actual;
    
    DataDiff(int addr, int expected, int actual) {
        Validate.isTrue(expected != actual);
        
        this.addr = addr;
        this.expected = expected;
        this.actual = actual;
    }
    
    @Override
    public String toString() {
        return "{" + 
                "addr: " + toHex(addr) + "," + 
                "expected: " + toHex(expected) + "," + 
                "actual: " + toHex(actual) +
                "}";
    }
    
    private String toHex(int a) {
        String s = "0000" + Integer.toHexString(a);
        
        return "$" + s.substring(s.length() - 4);
    }

}
