package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

public class NopTests {

    @Test
    public void nop_accumulator() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "NOP",
                    "BRK"),
                ExpectedState.empty());
    }

}
