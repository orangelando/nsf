package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

public class EorTests {

    @Test
    public void eor_immediate() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$fe",
                    "EOR #$8a",
                    "BRK"),
                ExpectedState.onlyA(0xfe ^ 0x8a));
    }
}
