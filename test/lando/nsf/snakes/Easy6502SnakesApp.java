package lando.nsf.snakes;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JFrame;

import lando.nsf.ExecutableImage;
import lando.nsf.assembler.AssemblerResults;
import lando.nsf.assembler.Easy6502TestName;
import lando.nsf.assembler.SimpleAssembler;
import lando.nsf.core6502.ByteArrayMem;
import lando.nsf.core6502.CPU;
import lando.nsf.gui.Dissassembler;
import lando.nsf.gui.MemoryMonitor;
import lando.nsf.gui.MonitoringMem;
import lando.nsf.gui.TextLines;

public final class Easy6502SnakesApp {
    
    private static final int BOOT_ADDR = 0x0600;

    public static void main(String [] args) throws Exception {
        
        List<String> asm = Easy6502TestName.snakes.assemblyLines();
        SimpleAssembler assembler = new SimpleAssembler();
        AssemblerResults assr = assembler.build(BOOT_ADDR, asm);
        ExecutableImage img = assr.getExecImg();
        ByteArrayMem mem = new ByteArrayMem();
        MonitoringMem monitoringMem = new MonitoringMem(mem);
        CPU cpu = new CPU(monitoringMem);
        
        init(cpu);
        mem.load(img);
        
        AtomicInteger numInstrs = new AtomicInteger(0);

        Dissassembler dissassembler = new Dissassembler(
                cpu, 
                () -> mem.bytes, 
                asm, 
                numInstrs,
                assr.getAddrsToLineNums(),
                assr.getAddressLabels());
        
        MemoryMonitor zeroPage   = new MemoryMonitor(0, 256, 16, cpu, monitoringMem);
        MemoryMonitor stackPage  = new MemoryMonitor(CPU.STACK_START, 256, 16, cpu, monitoringMem);
        MemoryMonitor pixelsPage = new MemoryMonitor(0x0200, 32*32, 32, cpu, monitoringMem);
                
        TextLines dissassemblerTxt = new TextLines(dissassembler::currStatus);
        TextLines zeroPageTxt      = new TextLines(zeroPage::memData);
        TextLines stackPageTxt     = new TextLines(stackPage::memData);
        TextLines pixelsPageTxt    = new TextLines(pixelsPage::memData);
        
        JFrame dissassemblerFrame = createWindow("Dissassembler",      dissassemblerTxt);
        JFrame zeroPageFrame      = createWindow("Zero Page",          zeroPageTxt);
        JFrame stackPageFrame     = createWindow("Stack Page - $0100", stackPageTxt);
        JFrame pixelsPageFrame    = createWindow("Pixels - $0200",     pixelsPageTxt);
        
        WidgetRandom   rnd    = new WidgetRandom(mem);
        WidgetKeyboard kb     = new WidgetKeyboard(mem);
        WidgetPixels   pixels = new WidgetPixels(mem);
        
        JFrame mainFrame = createWindow("Easy6502 Snakes", pixels);
        
        mainFrame.addKeyListener(kb);
        
        mainFrame         .setLocation(   0,   0);
        dissassemblerFrame.setLocation( 510,   0);
        zeroPageFrame     .setLocation(1020,   0);
        stackPageFrame    .setLocation(   0, 550);
        pixelsPageFrame   .setLocation( 510, 550);
        
        zeroPageFrame.setSize(385, 385);
        stackPageFrame.setSize(385, 385);
        pixelsPageFrame.setSize(750, 750);
        
        //long prevTime = System.currentTimeMillis();
        
        while(mainFrame.isVisible()) {
            
            List<Character> keysPressed = kb.drainKeyQueue();
            
            //if( keysPressed.contains('q') || keysPressed.contains('Q') || 
            //    keysPressed.contains('z') || keysPressed.contains('Z')) {
                
                int numSteps = keysPressed.contains('z') || keysPressed.contains('Z') 
                        ? 100 : 1;
            
                for(int i = 0; i < 100; i++) {
                    rnd.setRandByte();
                    monitoringMem.clearReadsAndWrites();
                    cpu.step();
                    numInstrs.incrementAndGet();
                }
                
                //dissassemblerTxt.updateTxt();
                //zeroPageTxt.updateTxt();
                //stackPageTxt.updateTxt();
                //pixelsPageTxt.updateTxt();
                
                pixels.repaint();
            //}
            
            /*
            long currTime = System.currentTimeMillis();
            
            if( prevTime/1000 != currTime/1000 ) {
                System.err.println(numInstrs.get());
            }
            
            prevTime = currTime;
            */
            
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
    
    private static void init(CPU cpu) {
        cpu.A  = 0;
        cpu.X  = 0;
        cpu.Y  = 0;
        cpu.P  = 0b00_1_10000;
        cpu.S  = 0xFF;
        cpu.PC = BOOT_ADDR;
    }
}
