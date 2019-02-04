package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

public class AndTests {

    @Test
    public void and_immediate() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$fe",
                    "AND #$8a",
                    "BRK"),
                ExpectedState.onlyA(0xfe & 0x8a));
    }

}
