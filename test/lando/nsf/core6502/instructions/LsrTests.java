package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class LsrTests {

    @Test
    public void lsr_accumulator() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99", //1001 1001
                    "LSR A",    //0100 1100 1
                    "BRK"),
                ExpectedState.accumAndStatus(
                        0x99>>1,
                        CPU.START_STATUS | CPU.STATUS_C));
    }
    
    @Test
    public void lsr_zero_page() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99", //1001 1001
                    "STA $04",
                    "LSR $04",  
                    "BRK"),
                ExpectedState.statusAndMem(
                        CPU.START_STATUS | CPU.STATUS_C,
                        0x04, 0x99>>1));
    }
}
