package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import lando.nsf.core6502.CPU;
import lando.nsf.core6502.ExpectedState;

public class StackTests {

    @Test
    public void pha() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$10",
                    "PHA",
                    "LDA #$20",
                    "PHA",
                    "BRK"),
                ExpectedState.of(
                        Optional.of(0x20), //accumulator, 
                        Optional.empty(), //xIndex, 
                        Optional.empty(), //yIndex, 
                        Optional.of(0xfd-3), //stackPointer, BRK pushes 3 bytes
                        Optional.empty(), //programCounter, 
                        Optional.empty(), //processorFlags, 
                        0x01ff, 0x10,
                        0x01fe, 0x20));
    }
    
    @Test
    public void php() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "PHP",
                    "BRK"),
                ExpectedState.onlyMem(
                        0x01ff, CPU.START_STATUS));
    }
    
    @Test
    public void pla() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$10",
                    "PHA",
                    "LDA #$20",
                    "PHA",
                    "LDA #$30",
                    "PLA",
                    "PLA",
                    "BRK"),
                ExpectedState.onlyA(0x10));
    }
    
    @Test
    public void plp() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "SEC",
                    "PHP",
                    "CLC",
                    "PLP",
                    "BRK"),
                ExpectedState.statusFlags(
                        CPU.START_STATUS | CPU.STATUS_C
                        ));
    }




}
