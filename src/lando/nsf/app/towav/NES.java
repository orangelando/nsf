package lando.nsf.app.towav;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import lando.nsf.NESMem;
import lando.nsf.NSF;
import lando.nsf.NSFLoader;
import lando.nsf.NSFReader;
import lando.nsf.apu.APU;
import lando.nsf.cpu.CPU;
import lando.nsf.cpu.Memory;

public final class NES {

    public static NES buildForPath(
            Path path,
            Function<Memory, Memory> memDecorator
            ) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        
        NSF nsf = NSFReader.readNSF(bytes);
        NESMem mem = new NESMem();
        Memory wrappedMem = memDecorator.apply(mem);
        CPU cpu = new CPU(wrappedMem);
        APU apu = new APU(cpu);
        
        mem.setAPU(apu);
        
        NSFLoader loader = new NSFLoader(mem, nsf);

        //load nsf
        mem.clearMem();
        loader.loadNSF();

        return new NES(nsf, apu, mem, loader, cpu);
    }
    
    public static NES buildForPathNoMemMonitor(Path path) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        
        NSF nsf = NSFReader.readNSF(bytes);
        NESMem mem = new NESMem();
        NSFLoader loader = new NSFLoader(mem, nsf);
        CPU cpu = new CPU(mem);
        APU apu = new APU(cpu);
        
        mem.setAPU(apu);

        //load nsf
        mem.clearMem();
        loader.loadNSF();

        return new NES(nsf, apu, mem, loader, cpu);
    }

    
    public final int stopAddr = CPU.RESET_VECTOR_ADDR;
    public final AtomicInteger numInstrs = new AtomicInteger(0);
    public final AtomicInteger numCycles = new AtomicInteger(0);
    
    public final NSF nsf;
    public final APU apu;
    public final NESMem mem;
    public final NSFLoader loader;
    public final CPU cpu;
    
    NES(NSF nsf, APU apu, NESMem mem, NSFLoader loader, CPU cpu) {
        this.nsf    = Objects.requireNonNull(nsf);
        this.apu    = Objects.requireNonNull(apu);
        this.mem    = Objects.requireNonNull(mem);
        this.loader = Objects.requireNonNull(loader);
        this.cpu    = Objects.requireNonNull(cpu);
    }
    
    public void initTune(int songIndex) {
        mem.clearMem(0x0000, 0x07ff);
        mem.clearMem(0x6000, 0x7fff);
        mem.initAPU();
        
        loader.initTune(cpu, songIndex);
    }
    
    public void startInit() {
        loader.startInit(cpu, stopAddr);
    }
    
    public void startPlay() {
        loader.startPlay(cpu, stopAddr);
    }
    
    public void runRoutine() {
        numInstrs.set(0);
        numCycles.set(0);

        while( cpu.PC != stopAddr) {
            numCycles.addAndGet(cpu.step());
            numInstrs.incrementAndGet();
        }
    }
}
