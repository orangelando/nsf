package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class RolTests {

    @Test
    public void rol_accumulator() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "SEC",
                    "LDA #$01", //0000 0001
                    "ROL A",    //
                    "BRK"),
                ExpectedState.onlyA(3));
    }
    
    @Test
    public void rol_zero_page() {
        runTest(0x0600, 100,
                Arrays.asList(
                    "CLC",
                    "LDA #$88", //1000 1000
                    "STA $04",
                    "ROL $04",
                    "BRK"),
                ExpectedState.statusAndMem(
                    CPU.START_STATUS | CPU.STATUS_C,
                    0x04, 0x10));
    }
}
