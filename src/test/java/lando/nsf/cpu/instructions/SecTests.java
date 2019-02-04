package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

public class SecTests {

    @Test
    public void sec() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "SEC",
                    "BRK"),
                ExpectedState.statusFlags(0b00_1_10001));
    }
}
