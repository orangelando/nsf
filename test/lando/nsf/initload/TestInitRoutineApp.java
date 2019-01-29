package lando.nsf.initload;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JFrame;

import lando.nsf.NESMem;
import lando.nsf.app.towav.NES;
import lando.nsf.cpu.CPU;
import lando.nsf.gui.Dissassembler;
import lando.nsf.gui.MemoryMonitor;
import lando.nsf.gui.MonitoringMem;
import lando.nsf.gui.TextLines;

public final class TestInitRoutineApp {

    public static void main(String [] args) throws Exception {
        
        Path path = Paths.get(
                "/Users/oroman/Desktop/stuff2/NSF-06-01-2011/d/Donkey Kong (1983)(Ikegami Tsushinki)(Nintendo R&D1)(Nintendo).nsf");
        
        MonitoringMem monitoringMem = new MonitoringMem();
        NES nes = NES.buildForPath(path, (mem) -> {
            monitoringMem.setMemory(mem);
            
            return monitoringMem;
        });
        
        int songIndex = 1;
        
        nes.initTune(songIndex);
        
         
        nes.startInit();
        RunState runState = RunState.INIT;
        
        AtomicInteger numInstrs = new AtomicInteger(0);

        Dissassembler dissassembler = new Dissassembler(
                nes.cpu, 
                () -> nes.mem.bytes, 
                new ArrayList<String>(), 
                numInstrs,
                new TreeMap<Integer, Integer>(),
                new HashMap<String, Integer>());

        MemoryMonitor zeroPage     = new MemoryMonitor(0, 256, 16, nes.cpu, monitoringMem);
        MemoryMonitor stackPage    = new MemoryMonitor(CPU.STACK_START, 256, 16, nes.cpu, monitoringMem);
        
        MemoryMonitor bankRegsPage = new MemoryMonitor(
                NESMem.FIRST_BANK_SWITCH_REGISTER, 16, 16, nes.cpu, monitoringMem);
        
        MemoryMonitor apuRegsPage = new MemoryMonitor(
                0x4000, 32, 16, nes.cpu, monitoringMem);

        TextLines dissassemblerTxt = new TextLines(dissassembler::currStatus);
        
        TextLines zeroPageTxt      = new TextLines(zeroPage::memData);
        TextLines stackPageTxt     = new TextLines(stackPage::memData);
        TextLines bankRegsTxt      = new TextLines(bankRegsPage::memData);
        TextLines apuRegsTxt       = new TextLines(apuRegsPage::memData);

        JFrame dissassemblerFrame = createWindow("Dissassembler",      dissassemblerTxt);
        JFrame zeroPageFrame      = createWindow("Zero Page - $0000",  zeroPageTxt);
        JFrame stackPageFrame     = createWindow("Stack Page - $0100", stackPageTxt);
        JFrame bankRegsFrame      = createWindow("Bank Regs", bankRegsTxt);
        JFrame apuRegsFrame       = createWindow("APU Regs", apuRegsTxt);
        
        KeyCollector kb = new KeyCollector();
        
        dissassemblerTxt.addKeyListener(kb);
        
        dissassemblerFrame.setLocation( 510,   0);
        zeroPageFrame     .setLocation(1020,   0);
        stackPageFrame    .setLocation(   0, 550);
        bankRegsFrame     .setLocation(1020, 550);
        apuRegsFrame      .setLocation(   0,   0);
        
        zeroPageFrame.setSize(385, 385);
        stackPageFrame.setSize(385, 385);
        bankRegsFrame.setSize(385, 385);
        apuRegsFrame.setSize(385, 385);
        
        while(dissassemblerFrame.isVisible()) {
            
            List<Character> keysPressed = kb.drainKeyQueue();
            
            if( keysPressed.contains('q') || keysPressed.contains('Q') || 
                keysPressed.contains('z') || keysPressed.contains('Z')) {
                 
                boolean step;
                
                switch(runState) {
                case INIT:
                    if( nes.cpu.PC == CPU.RESET_VECTOR_ADDR ) {
                        nes.startPlay();
                        runState = RunState.PLAY;
                        step = false;
                    } else {
                        step = true;
                    }
                    break;
                    
                case PLAY:
                    if( nes.cpu.PC == CPU.RESET_VECTOR_ADDR ) {
                        runState = RunState.WAITING;
                        step = false;
                    } else {
                        step = true;
                    }
                    break;
                    
                default: 
                    step = false;
                }
                
                if( step ) {
                    monitoringMem.clearReadsAndWrites();
                    nes.cpu.step();
                    numInstrs.incrementAndGet();
                }
                
                dissassemblerFrame.setTitle("Dissassembler - " + runState);
                dissassemblerTxt.updateTxt();
                zeroPageTxt.updateTxt();
                stackPageTxt.updateTxt();
                bankRegsTxt.updateTxt();
                apuRegsTxt.updateTxt();
            }
            
            Thread.sleep(10);
        }
    }
    
    private static JFrame createWindow(String title, JComponent panel) {
        JFrame mainFrame = new JFrame();
        
        mainFrame.setTitle(title);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(500, 500);
        mainFrame.setResizable(false);
        mainFrame.getContentPane().add(panel);
        mainFrame.setVisible(true);
        
        return mainFrame;
    }
}
