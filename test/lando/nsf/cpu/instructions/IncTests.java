package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.CPU;
import lando.nsf.cpu.ExpectedState;

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
