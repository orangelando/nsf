package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;

public class DeyTests {

    @Test
    public void dey() {
        runTest(0x0600, 100, Arrays.asList(
                "    LDY #$01",
                "    DEY",
                "    DEY",
                "    BRK"),
            ExpectedState.onlyY(0xFF));
    }

}
