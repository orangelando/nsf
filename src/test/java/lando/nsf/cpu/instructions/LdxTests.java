package lando.nsf.cpu.instructions;

import static java.util.Arrays.asList;
import static lando.nsf.cpu.TestRunner.runTest;

import org.junit.Test;

import lando.nsf.cpu.ExpectedState;

public class LdxTests {

    @Test
    public void ldx_immediate() {
        runTest(0x0600, 100, asList(
                    "LDX #$10",
                    "BRK"),
                ExpectedState.onlyX(0x10));
    }
    
    @Test
    public void ldx_zero_page() {
        runTest(0x0600, 100, asList(
                    "LDA #$10",
                    "STA $08",
                    "LDX $08",
                    "BRK"),
                ExpectedState.onlyX(0x10));
    }
    
    @Test
    public void ldx_zero_page_y() {
        runTest(0x0600, 100, asList(
                    "LDA #$10",
                    "STA $08",
                    "LDY #$ff",
                    "LDX $09,Y",
                    "BRK"),
                ExpectedState.onlyX(0x10));
    }

    @Test
    public void ldx_absolute() {
        runTest(0x0600, 100, asList(
                    "LDA #$10",
                    "STA $0345",
                    "LDX $0345",
                    "BRK"),
                ExpectedState.onlyX(0x10));
    }
    
    @Test
    public void ldx_absolute_y() {
        runTest(0x0600, 100, asList(
                    "LDA #$10",
                    "STA $3102",
                    "LDY #$03",
                    "LDX $30ff,Y",
                    "BRK"),
                ExpectedState.onlyX(0x10));
    }
}
