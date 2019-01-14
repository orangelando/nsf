package lando.nsf.core6502;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

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
