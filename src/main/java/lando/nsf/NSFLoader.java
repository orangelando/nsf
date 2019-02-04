package lando.nsf;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import lando.nsf.cpu.CPU;

public final class NSFLoader {
    
    private static final int DATA_START_OFFSET = 0x80;
    
    private final PrintStream out = System.err;
    private final NESMem mem;
    private final NSF nsf;

    public NSFLoader(NESMem mem, NSF nsf) {
        this.mem = Objects.requireNonNull(mem);
        this.nsf = Objects.requireNonNull(nsf);
    }

    public void loadNSF() {
        
        Validate.isTrue(
                nsf.header.loadDataAddr == (nsf.header.loadDataAddr&0xFFFF));
        
        if( nsf.isBanked() ) {
            if( ! nsf.isFDS() ) {
                loadBankedROM();
            } else {
                throw new UnsupportedOperationException("FDS bank switching not supported.");
            }
        } else {
            loadNonBankedROM();
        }
    }
    
    public void initTune(CPU cpu, int songIndex) {
        Validate.notNull(cpu);
        
        Validate.isTrue(songIndex >= 0 && songIndex < nsf.header.totalSongs, 
                "Song index out of range");
        
        if( nsf.isBanked() ) {
            setInitialBanks();
        }
        
        cpu.A = songIndex;
        cpu.X = nsf.header.getNtscPalMode() == NtscPalMode.PAL ? 1 : 0;
        cpu.Y = 0;
        
        cpu.P = CPU.START_STATUS;
        cpu.S = 0xFF;
        cpu.PC = nsf.header.initDataAddr;
    }
    
    public void startInit(CPU cpu, int returnAddr) {
        startSeg(cpu, nsf.header.initDataAddr, returnAddr);
    }
    
    public void startPlay(CPU cpu, int returnAddr) {
        startSeg(cpu, nsf.header.playDataAddr, returnAddr);
    }
    
    private void startSeg(CPU cpu, int startAddr, int returnAddr) {
        cpu.P  = CPU.START_STATUS;
        cpu.PC = startAddr;
        cpu.S  = 0xFF;
        
        //push our "return" address back the way JSR would do it.
        cpu.pushAddr(returnAddr - 1);
    }

    
    private void loadNonBankedROM() {
        
        for(int i = DATA_START_OFFSET; i < nsf.data.length; i++) {
            int j = nsf.header.loadDataAddr + i - DATA_START_OFFSET;
            
            if( j < mem.bytes.length ) {
                mem.bytes[j] = nsf.data[i];
            } else {
                out.printf("    %4x: %6d%n", nsf.header.loadDataAddr, nsf.data.length - 0x80);
                out.println("    TOO BIG!");
                break;
            }
        }
    }
    
    private void loadBankedROM() {
        
        byte[][] banks = readBanks();
        
        mem.enableBankSwitching(banks);
        
        setInitialBanks(); //don't need to do this
    }
    
    public void setInitialBanks() {
        
        int[] indexes = nsf.header.bankswitchInitValues;
        
        for(int i = 0; i < indexes.length; i++) {
            int bankIndex = indexes[i];
            int bankRegister = 0x5FF8 + i;
            
            mem.write(bankRegister, bankIndex);
        }
    }
    
    private byte[][] readBanks() {
        
        List<byte[]> bankList = new ArrayList<>();
        int bankSize = 4096;
        int padding = nsf.header.loadDataAddr & 0x0FFF;
        
        byte[] bank = new byte[bankSize];
        int bankIndex = padding;
        
        for(int i = DATA_START_OFFSET; i < nsf.data.length; i++) {
            
            if( bankIndex >= bank.length) {
                bankList.add(bank);
                bank = new byte[bankSize];
                bankIndex = 0;
            }
            
            bank[bankIndex++] = nsf.data[i];
        }
        
        bankList.add(bank);
        
        out.println("    banks: " + bankList.size());
        
        Validate.isTrue(nsf.header.bankswitchInitValues.length == 8);
        
        return bankList.toArray(new byte[][]{});
    }

}
