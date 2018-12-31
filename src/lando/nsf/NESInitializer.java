package lando.nsf;

import java.util.Arrays;

import lando.nsf.core6502.CPU;
import lando.nsf.core6502.Memory;

/**
 * Taken from: 
 *    http://wiki.nesdev.com/w/index.php/CPU_power_up_state
 * 
 *
 */
public class NESInitializer {

    public void initPowerUpState(CPU cpu, NESMem mem) {
        
        cpu.P = 0x34;
        cpu.A = 0;
        cpu.X = 0;
        cpu.Y = 0;
        cpu.S = 0xFD;
        
        Arrays.fill(mem.bytes, 0x0000, 0x0800, (byte)0xFF);
        
        mem.bytes[0x0008] = (byte)0xF7;
        mem.bytes[0x0009] = (byte)0xEF;
        mem.bytes[0x000A] = (byte)0xDF;
        mem.bytes[0x000F] = (byte)0xBF;
    }
}
