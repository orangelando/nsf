package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

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
