package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.CPU;
import lando.nsf.core6502.ExpectedState;

public class BitTests {

    @Test
    public void bit_zero_page() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$04",
                    "STA $00",
                    "LDA #$40",
                    "BIT $00",
                    "BRK"),
                ExpectedState.statusFlags(CPU.START_STATUS | CPU.STATUS_Z));
    }
    
    @Test
    public void bit_absolute() {
        runTest(
                0x0600,
                100,
                Arrays.asList(
                    "LDA #$04",
                    "STA $1012",
                    "LDA #$40",
                    "BIT $1012",
                    "BRK"),
                ExpectedState.statusFlags(CPU.START_STATUS | CPU.STATUS_Z));
    }
}
