package lando.nsf.initload;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import lando.nsf.APU;
import lando.nsf.NESMem;
import lando.nsf.NSF;
import lando.nsf.NSFLoader;
import lando.nsf.NSFReader;
import lando.nsf.core6502.CPU;
import lando.nsf.gui.MonitoringMem;

final class NES {

    static NES buildForPath(Path path) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        
        NSF nsf = NSFReader.readNSF(bytes);
        APU apu = new APU();
        NESMem mem = new NESMem(apu);
        MonitoringMem monitoringMem = new MonitoringMem(mem);
        NSFLoader loader = new NSFLoader(mem, nsf);
        CPU cpu = new CPU(monitoringMem);

        //load nsf
        mem.clearMem();
        loader.loadNSF();

        return new NES(nsf, apu, monitoringMem, mem, loader, cpu);
    }
    
    static NES buildForPathNoMemMonitor(Path path) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        
        NSF nsf = NSFReader.readNSF(bytes);
        APU apu = new APU();
        NESMem mem = new NESMem(apu);
        MonitoringMem monitoringMem = null;
        NSFLoader loader = new NSFLoader(mem, nsf);
        CPU cpu = new CPU(mem);

        //load nsf
        mem.clearMem();
        loader.loadNSF();

        return new NES(nsf, apu, monitoringMem, mem, loader, cpu);
    }

    
    final int stopAddr = CPU.RESET_VECTOR_ADDR;
    final AtomicInteger numInstrs = new AtomicInteger(0);
    
    final NSF nsf;
    final APU apu;
    final MonitoringMem monitoringMem;
    final NESMem mem;
    final NSFLoader loader;
    final CPU cpu;
    
    NES(NSF nsf, APU apu, MonitoringMem monitoringMem, NESMem mem, NSFLoader loader, CPU cpu) {
        this.nsf    = Objects.requireNonNull(nsf);
        this.apu    = Objects.requireNonNull(apu);
        this.monitoringMem = monitoringMem; //can be null
        this.mem    = Objects.requireNonNull(mem);
        this.loader = Objects.requireNonNull(loader);
        this.cpu    = Objects.requireNonNull(cpu);
    }
    
    void initTune(int songIndex) {
        mem.clearMem(0x0000, 0x07ff);
        mem.clearMem(0x6000, 0x7fff);
        mem.initAPU();
        
        loader.initTune(cpu, songIndex);
    }
    
    void startInit() {
        loader.startInit(cpu, stopAddr);
    }
    
    void startPlay() {
        loader.startPlay(cpu, stopAddr);
    }
    
    void runRoutine() {
        numInstrs.set(0);

        while( cpu.PC != stopAddr) {
            
            if( monitoringMem != null ) {
                monitoringMem.clearReadsAndWrites();
            }
            
            cpu.step();
            numInstrs.incrementAndGet();
        }
    }
}
