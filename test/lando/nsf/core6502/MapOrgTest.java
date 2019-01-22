package lando.nsf.core6502;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import lando.nsf.cpu.AddrMode;
import lando.nsf.cpu.Instruction;
import lando.nsf.cpu.Instructions;
import lando.nsf.cpu.OpCodeName;

public class MapOrgTest {

    @Test
    public void test_named_map() {
        
        Instruction i1 = Instructions.byNameAndAddrMode(OpCodeName.AND, AddrMode.ABSOLUTE).get();
        assertTrue( i1.name == OpCodeName.AND && i1.addrMode == AddrMode.ABSOLUTE);
        
        assertTrue( ! Instructions.byNameAndAddrMode(OpCodeName.AND, AddrMode.ACCUMULATOR).isPresent() );
    }
}
