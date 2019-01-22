package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.cpu.CPU;
import lando.nsf.cpu.ExpectedState;

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
