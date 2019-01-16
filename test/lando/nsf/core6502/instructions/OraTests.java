package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

public class OraTests {

    @Test
    public void ora_immediate() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$fe",
                    "ORA #$8a",
                    "BRK"),
                ExpectedState.onlyA(0xfe | 0x8a));
    }

}
