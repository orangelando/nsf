package lando.nsf.cpu.instructions;

import static lando.nsf.cpu.TestRunner.runTest;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

public class TransferTests {

    @Test
    public void tax() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99",
                    "TAX",
                    "BRK"),
                ExpectedState.onlyX(0x99));
    }
    
    @Test
    public void tay() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99",
                    "TAY",
                    "BRK"),
                ExpectedState.onlyY(0x99));
    }
    
    @Test
    public void tsx() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDA #$99",
                    "PHA",
                    "TSX",
                    "BRK"),
                ExpectedState.onlyX(0xFE));
    }
    
    @Test
    public void txa() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "TSX",
                    "TXA",
                    "BRK"),
                ExpectedState.onlyA(0xFF));
    }
    
    @Test
    public void txs() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDX #$99",
                    "TXS",
                    "BRK"),
                ExpectedState.onlyRegisters(
                    Optional.empty(), //accumulator, 
                    Optional.empty(), //xIndex, 
                    Optional.empty(), //yIndex, 
                    Optional.of(0x99-3), //stackPointer, BRK pushes 3 bytes (PC + status)
                    Optional.empty(), //programCounter, 
                    Optional.empty() //processorFlags
                    ));
    }
    
    @Test
    public void tya() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "LDY #$99",
                    "TYA",
                    "BRK"),
                ExpectedState.onlyA(0x99));
    }
}
