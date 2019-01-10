package lando.nsf.core6502;

import static lando.nsf.core6502.StringUtils.toBin8;
import static lando.nsf.core6502.StringUtils.toHex2;
import static lando.nsf.core6502.StringUtils.toHex4;

import java.io.PrintStream;

final class StatusPrinter {
    
    private static final PrintStream out = System.err;

    static void printRegisters(CPU cpu, Memory mem) {
        out.println();
        out.println("A=" + toHex2(cpu.A));
        out.println("X=" + toHex2(cpu.X));
        out.println("Y=" + toHex2(cpu.Y));
        out.println("S=" + toHex2(cpu.S));
        out.println("PC=" + toHex4(cpu.PC));
        out.println("NV-BDIZC");
        out.println(toBin8(cpu.P));
        out.println();
        
        int opCode = mem.read(cpu.PC);
        Instruction instr = Instructions.BY_OP_CODE[opCode];
        
        if( instr == null ) {
            out.println("next: ??");
        } else {
            out.print("next: " + instr.name + " ");
            
            switch(instr.addrMode.instrLen) {
            case 1:
                out.println("A");
                break;
                
            case 2:
                out.println( toHex2(mem.read(cpu.PC + 1)) );
                break;
                
            case 3:
                out.println( toHex2(mem.read(cpu.PC + 1)) + " " + toHex2(mem.read(cpu.PC + 2)));
                break;
                
            default:
                out.println("wth?!");
            }
        }
        
        //print memory
        {
            int start = 0x1f0;
            int len = 0x30;
            
            for(int i = 0; i < len; i += 16) {
                out.print( toHex4(start + i).substring(1) + ":");
                
                for(int j = 0; j < 16; j++) {
                    out.print( " " + toHex2(mem.read(start + i + j)).substring(1) );
                }
                
                out.println();
            }
        }
         
    }

}
