package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class RorTests {

    @Test
    public void ror_accumulator() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "SEC",
                    "LDA #$08", //0000 1000
                    "ROR A",    //
                    "BRK"),
                ExpectedState.onlyA(0x84));
    }
    
    @Test
    public void ror_zero_page() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "CLC",
                    "LDA #$11", //0001 0001
                    "STA $04",
                    "ROR $04",
                    "BRK"),
                ExpectedState.statusAndMem(
                    CPU.START_STATUS | CPU.STATUS_C,
                    0x04, 0x08));
    }
}
