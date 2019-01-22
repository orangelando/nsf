package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class IncTests {

    @Test
    public void inc() {
        runTest(0x0600, 100, Arrays.asList(
                "    LDA #$fe",
                "    STA $00",
                "    INC $00",
                "    BRK"),
            ExpectedState.statusAndMem(
                CPU.START_STATUS | CPU.STATUS_N,
                0x00, 0xFF));
    }

}
