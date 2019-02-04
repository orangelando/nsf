package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

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
