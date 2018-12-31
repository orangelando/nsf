package lando.nsf.app;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import lando.nsf.*;
import lando.nsf.core6502.CPU;
import lando.nsf.core6502.Instruction;
import lando.nsf.core6502.Instructions;

import org.apache.commons.lang3.Validate;

public final class CpuTestApp {
	
	public static void main(String [] args) throws Exception {
		final File    file = new File("/Users/oroman/Downloads/super-mario-bros-2-nes-[NSF-ID1934].nsf");
		final NESMem mem = new NESMem();
		final NSF nsf = NSFReader.readNSF(file);
		final CPU cpu = new CPU(mem);
		
		NSFReader.load(nsf, mem);
		
		final int[] songNum = {0};
		final boolean done[] = {false};
		final Thread playThread[] = {null};
		
		final JFrame mainFrame = new JFrame();
		final JTextPane txtPane = new JTextPane();
		
		txtPane.setEditable(false);
		txtPane.setContentType("text/html");
		
		update(nsf, cpu, mem, txtPane, songNum[0]);
		
		txtPane.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
				switch(e.getKeyChar()) {
				case 'q': 
					songNum[0] = Math.max(songNum[0] - 1, 0); 
					break;
					
				case 'w': 
					songNum[0] = Math.min(songNum[0] + 1, nsf.header.totalSongs); 
					break;
					
				case 'e': 
					initTune(nsf, cpu, mem, songNum[0]);
					update(nsf, cpu, mem, txtPane, songNum[0]);
					break;
					
				case 'z':
					stopPlaying(playThread, done);
					startPlaying(playThread, done, cpu, mem, nsf);
					break;
					
				case 'x':
					stopPlaying(playThread, done);
					break;
				}
				
				System.err.println("song-num: " + songNum[0]);
			}

		});
		
		JScrollPane scrollPane = new JScrollPane(txtPane);
		
		mainFrame.getContentPane().add(scrollPane);
		mainFrame.setTitle("M6502");
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.setSize(500, 500);
		mainFrame.setVisible(true);
	}
	
	private static void startPlaying(
			final Thread[] playThread, 
			final boolean[] done,
			final CPU cpu, 
			final NESMem mem,
			final NSF nsf
			) {
		Validate.isTrue(playThread[0] == null);
		
		playThread[0] = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					go();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			private void go() throws Exception {
				System.err.println("Starting playing thread");
				
				long millionths = nsf.isNSTC() ? nsf.header.ntscSpeed : nsf.header.palSpeed;
				Validate.isTrue(millionths > 0);
				
				long nanos = millionths*1000;
				System.err.println(
					"Play interval: " + 
					(nanos/1e9) + "s " +
					(1e9/nanos) + "hz " + 
					"");
				
				
				long updateTicks = 5000000000L;
				long prevTime = System.nanoTime();
				
				long cycles = 0;
				long calls = 0;
				
				done[0] = false;
				
				while( ! done[0] ) {
					long time = System.nanoTime();
					
					if ( (prevTime/nanos) != (time/nanos) ) {
						cpu.PC = nsf.header.playDataAddr;
						cycles += runUntilRTS(cpu, mem);
						calls++;
					}
					
					if( (prevTime/updateTicks) != (time/updateTicks) ) {
						System.err.println("cycles: " + cycles);
						System.err.println("call speed : " + ((double)calls/updateTicks*1e9) + "hz");
						
						cycles = 0;
						calls = 0;
					}
					
					prevTime = time;
				}
				
				System.err.println("Exiting playing thread");
			}
			
		});
		
		playThread[0].start();
	}
	

	private static void stopPlaying(Thread[] playThread, boolean[] done) {
		done[0] = true;
		
		if( playThread[0] != null ) {
			
			System.err.println("Waiting for " + playThread[0].getName() + " to finish");
			
			try {
				if( playThread[0].isAlive() ) {
					playThread[0].join();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		playThread[0] = null;
	}
	
	private static void initTune(NSF nsf, CPU cpu, NESMem mem, int songNum) {
		System.err.println("Initializing...");
		
		Arrays.fill(mem.bytes, 0x0000, 0x0800, (byte)0);
		Arrays.fill(mem.bytes, 0x6000, 0x8000, (byte)0);
		
		if( nsf.isBanked() ) {
			for(int i = 0; i < 8; i++) {
				mem.write(0x5FF8 + i, nsf.header.bankswitchInitValues[i]);
			}
		}
		
		cpu.A = songNum;
		cpu.X = nsf.isNSTC() ? 0 : 1;
		cpu.PC = nsf.header.initDataAddr;
		
		long start = System.nanoTime();
		int cycles = runUntilRTS(cpu, mem);
		long end = System.nanoTime();
		
		System.err.println("init routine et: " + (end - start) + "ns");
		System.err.println("cycles: " + cycles);
		System.err.println("cycle-et: " + (double)(end - start)/cycles + "ns");
	}
	
	private static int runUntilRTS(CPU cpu, NESMem mem) {
		mem.reads = 0;
		mem.writes = 0;
		int cycles = 0;
		
		while( mem.read(cpu.PC) != 0x60 ) {
			cycles += cpu.step();
		}
		
		return cycles;
	}

	private static void step(NSF nsf, CPU cpu, NESMem mem, int steps) {
		int cycles = 0;
		mem.reads = 0;
		mem.writes = 0;
		
		long start = System.nanoTime();
		for(int i = 0; i < steps; i++) {
			cycles += cpu.step();
			
			if( mem.read(cpu.PC) == 0x60 ) {
				System.err.println("DONE!");
				cpu.PC = nsf.header.playDataAddr;
				break;
			}
		}
		long end = System.nanoTime();
		
		System.err.println();
		System.err.println("steps    : " + steps);
		System.err.println("et       : " + (end - start)/1e6 + "ms");
		System.err.println("cycles   : " + cycles);
		System.err.println("cycle-et : " + (double)(end - start)/cycles + "ns");
	}
	
	private static void update(NSF nsf, CPU cpu, NESMem mem, JTextPane txtPane, int songNum) {
		Set<Integer> addressBlocks = new TreeSet<Integer>();
		
		addressBlocks.add( nsf.header.initDataAddr & 0xFF00 );
		addressBlocks.add( nsf.header.loadDataAddr & 0xFF00 );
		addressBlocks.add( nsf.header.playDataAddr & 0xFF00 );
		addressBlocks.add( 0x0000 );
		addressBlocks.add( 0x0100 );
		
		addressBlocks.add( cpu.PC & 0xFF00 );
		
		for(int i = 0; i < mem.reads; i++) {
			addressBlocks.add(mem.readAddrs[i] & 0xFF00);
		}
		
		for(int i = 0; i < mem.writes; i++) {
			addressBlocks.add(mem.writeAddrs[i] & 0xFF00);
		}
		
		long start, end;
		
		start = System.currentTimeMillis();
		String txt = render(addressBlocks, mem, cpu, nsf, songNum);
		end = System.currentTimeMillis();
		
		System.err.println("render-et: " + (end - start));
		
		start = System.currentTimeMillis();
		txtPane.setText(txt);
		end = System.currentTimeMillis();
		
		System.err.println("set-et: " + (end - start));
	}
	
	private static String flag(int status, int flag) {
		return (status & flag) == 0 ? "0" : "1";
	}
	
	public static String render(final Set<Integer> addressBlocks, final NESMem mem, final CPU cpu, NSF nsf, int songNum) {
		Validate.notNull(addressBlocks);
		Validate.notNull(mem);
		Validate.notNull(cpu);
		
		final Set<Integer> reads  = new HashSet<Integer>();
		final Set<Integer> writes = new HashSet<Integer>();
		
		for(int i = 0; i < mem.reads; i++) {
			reads.add(mem.readAddrs[i]);
		}
		
		for(int i = 0; i < mem.writes; i++) {
			writes.add(mem.writeAddrs[i]);
		}
		
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		
		out.println("<html>");
		
		out.println("<head>");
		out.println("<style> " +
				"body {" +
				"    font-size:10; " +
				"    font-family:monospace; " +
				"    background-color:#444444; " +
				"    color:#FFFFFF;" +
				"} " +
				".ad { color:#AAAAAA; } " +
				".pc { color:#0088FF; } " +
				".rd { color:#00FF00; } " +
				".wr { color:#FF0000; } " +
				".bt { color:#FFFF00; } " +
				"</style>");
		out.println("</head>");
		
		out.println("<body>");
		
		renderCPURegisters(out, cpu, mem);
		
		out.println("<hr/>");
		
		for(int startAddr: addressBlocks) {
			renderAddressBlock(out, reads, writes, startAddr, cpu.PC, mem);
		}
		
		out.println("</body>");
		out.println("</html>");
		
		return writer.toString();
	}
	
	private static void renderAddressBlock(
			PrintWriter out, 
			Set<Integer> reads,
			Set<Integer> writes,
			int startAddr, 
			int pc, 
			NESMem mem
			) {
		
		int numCols = 32;
		int numRows = 256/32;
		
		out.printf("<h4>%s</h4>%n", HexUtils.toHex16(startAddr));
		out.println("<table>");
		
		//header row
		{
			out.printf("<tr>");
			out.printf("<td>&nbsp;</td>");
			
			for(int c = 0; c < numCols; c++) {
				
				if( c != 0 && c % 4 == 0 ) {
					out.printf("<td>&nbsp;</td>");
				}
				
				out.printf("<td class='ad'>%s</td>", HexUtils.toHex8(c));
			}
			out.println("</tr>");
		}
		
		for(int r = 0; r < numRows; r++) {
			int rowStartAddr = startAddr + r*numCols;
			
			out.println("<tr>");
			out.printf("<td class='ad'>%s:</td>%n", HexUtils.toHex16(rowStartAddr));
			
			for(int c = 0; c < numCols; c++) {
				int addr = (rowStartAddr + c) & 0xFFFF;
				
				final String clz;
				
				if( addr == pc ) {
					clz = "pc";
				} else if( reads.contains(addr) && writes.contains(addr)) {
					clz = "bt";
				} else if( reads.contains(addr) ) {
					clz = "rd";
				} else if( writes.contains(addr) ) {
					clz = "wr";
				} else {
					clz = "";
				}
				
				if( c != 0 && c % 4 == 0 ) {
					out.printf("<td>&nbsp;</td>");
				}
				
				if( addr >= 0 && addr < mem.bytes.length ) {
					out.printf("<td class='%s'>%s</td>", clz, HexUtils.toHex8(mem.bytes[addr] & 0xFF));
				} else {
					out.printf("<td>??</td>");
				}
			}
			
			out.println("</tr>");
		}
		
		out.println("</table>");
	}
	
	private static void renderCPURegisters(PrintWriter out, CPU cpu, NESMem mem) {
		//
		out.println("<table>");
		
		out.println("<tr>");
		out.println("<td>PC</td>");
		out.println("<td>A</td>");
		out.println("<td>X</td>");
		out.println("<td>Y</td>");
		out.println("<td>S</td>");
		out.println("<td>cycles</td>");
		out.println("</tr>");
		
		out.printf("<tr>");
		out.printf("<td>%s</td>", HexUtils.toHex16(cpu.PC));
		out.printf("<td>%s</td>", HexUtils.toHex8(cpu.A));
		out.printf("<td>%s</td>", HexUtils.toHex8(cpu.X));
		out.printf("<td>%s</td>", HexUtils.toHex8(cpu.Y));
		out.printf("<td>%s</td>", HexUtils.toHex8(cpu.S));
		out.printf("<td>%d</td>", cpu.cycles);
		out.printf("</tr>");
		
		out.println("</table>");
		out.println("<br/>");
		
		//
		out.println("<table>");
		
		out.println("<tr>");
		out.println("<td>C</td>");
		out.println("<td>Z</td>");
		out.println("<td>I</td>");
		out.println("<td>D</td>");
		out.println("<td>B</td>");
		out.println("<td>V</td>");
		out.println("<td>N</td>");
		out.println("</tr>");
		
		out.printf("<tr>");
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_C));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_Z));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_I));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_D));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_B));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_O));
		out.printf("<td>%s</td>", flag(cpu.P, CPU.STATUS_N));
		out.printf("</tr>");
		
		out.println("</table>");
		out.println("<br/>");
		
		//render opcode info
		{
			int b1 = mem.read(cpu.PC + 0);
			int b2 = mem.read(cpu.PC + 1);
			int b3 = mem.read(cpu.PC + 2);
			
			Instruction opInfo = null;
			
			if( b1 >= 0 && b1 < Instructions.BY_OP_CODE.length) {
				opInfo = Instructions.BY_OP_CODE[b1];
			}
			
			int len;
			
			if( opInfo != null ) {
				len = opInfo.addrMode.instrLen;
			} else {
				len = 1;
			}
			
			Object[] args = {"&nbsp;","&nbsp;","&nbsp;"};
			
			for(int i = 0; i < len; i++) {
				args[i] = HexUtils.toHex8(mem.read(cpu.PC + i));
			}
			
			out.printf("<table>%n");
			out.printf("<tr>");
			out.printf("<td>%s</td> <td>%s</td> <td>%s</td> <td>&nbsp;</td>",args);
			out.printf("<td>%s</td>", DisassemblerUtils.opCodeText(b1, b2, b3));
			out.printf("</tr>%n");
			out.printf("</table>%n");
		}
	}
}
