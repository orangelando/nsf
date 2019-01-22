package lando.nsf.core6502.instructions;

import static lando.nsf.core6502.TestRunner.runTest;

import java.util.Arrays;

import org.junit.Test;

import lando.nsf.core6502.ExpectedState;
import lando.nsf.cpu.CPU;

public class StatusFlagsTests {

    @Test
    public void clc() {
        runTest(0x0600, 100, Arrays.asList(
                "    CLC",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS));
    }
    
    @Test
    public void cld() {
        runTest(0x0600, 100, Arrays.asList(
                "    CLD",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS));
    }
    
    @Test
    public void cli() {
        runTest(0x0600, 100, Arrays.asList(
                "    CLI",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS));
    }
    
    @Test
    public void clv() {
        runTest(0x0600, 100, Arrays.asList(
                "    CLV",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS));
    }
    
    @Test
    public void sec() {
        runTest(0x0600, 100, Arrays.asList(
                "    SEC",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS | CPU.STATUS_C));
    }
    
    @Test
    public void sed() {
        runTest(0x0600, 100, Arrays.asList(
                "    SED",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS | CPU.STATUS_D));
    }
    
    @Test
    public void sei() {
        runTest(0x0600, 100, Arrays.asList(
                "    SEI",
                "    BRK"),
            ExpectedState.statusFlags(
                CPU.START_STATUS | CPU.STATUS_I));
    }
}
