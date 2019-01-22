package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

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
