package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class CmpTests {

    @Test
    public void cmp() {
        runTest(0x0600, 100, Arrays.asList(
                "    LDA #$01",
                "    CMP #$02",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS | CPU.STATUS_N));
    }

}
