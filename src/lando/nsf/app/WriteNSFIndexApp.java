package lando.nsf.app;

import static lando.nsf.DisassemblerUtils.opCodeText;
import static lando.nsf.core6502.StringUtils.toHex2;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lando.nsf.HexUtils;
import lando.nsf.NSF;
import lando.nsf.NSFHeader;
import lando.nsf.NSFReader;
import lando.nsf.core6502.Instruction;
import lando.nsf.core6502.Instructions;

public final class WriteNSFIndexApp {
    
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    
    static {
        Validate.isTrue(HEX_CHARS.length == 16);
    }
    
    public static class NSFFileInfo {
        public final String path;
        public final String fileName;
        public final String parentDir;
        public final String md5;
        public final int dataSize;
        public final NSFHeader header;
        
        public NSFFileInfo(String path, String fileName, String parentDir, String md5, int dataSize, NSFHeader header) {
            Validate.isTrue(dataSize > 0);
            
            this.path      = Objects.requireNonNull(path);
            this.fileName  = Objects.requireNonNull(fileName);
            this.parentDir = Objects.requireNonNull(parentDir);
            this.md5       = Objects.requireNonNull(md5);
            this.dataSize  = dataSize;
            this.header    = Objects.requireNonNull(header);
        }
    }
    
    @Option(name="-indexDir", required=true)
    private String indexDir;
    
    @Option(name="-nsfDir", required=true)
    private String nsfDir;

    public static void main(String [] args) throws Exception {
        WriteNSFIndexApp app = new WriteNSFIndexApp();
        CmdLineParser cmdParser = new CmdLineParser(app);
        
        try {
            cmdParser.parseArgument(args);
        } catch(CmdLineException e) {
            throw e;
        }
        
        app.exec();
    }
    
    private WriteNSFIndexApp() {
        
    }
    
    private void exec() throws Exception {
                
        PrintStream out = System.err;
        
        Path listingsDir = Paths.get(indexDir).resolve("asm-listings");
        List<NSFFileInfo> infos = fetchFilesAndWriteASMListings(listingsDir);
        out.println("found " + infos.size() + " infos.");
        
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        Path outFile = Paths.get(indexDir).resolve("nsf-index.json");
        out.println("writing to " + outFile);
        
        byte[] jsonBytes = jsonMapper.writeValueAsBytes(infos);
        Files.write(outFile, jsonBytes);
        
        out.println("done");
    }
    
    private List<NSFFileInfo> fetchFilesAndWriteASMListings(Path listingsDir) throws Exception {
        
        MessageDigest digest = MessageDigest.getInstance("MD5");
        
        byte[] rom = new byte[1<<16];
        
        return Files.walk(Paths.get(nsfDir))
            .filter(p -> p.getFileName().toString().endsWith(".nsf"))
            //.filter(p -> p.getFileName().toString().toLowerCase().contains("metroid"))
            .map(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    
                    System.err.println();
                    System.err.println("________________________________________________________________________________");
                    System.err.println(fileName);
                    
                    String parentDir = path.getParent().getFileName().toString();
                    
                    long fileSize = Files.size(path);
                    Validate.isTrue( fileSize <  Integer.MAX_VALUE );
                    
                    byte[] bytes = Files.readAllBytes(path);
                    Validate.isTrue( fileSize == bytes.length );
        
                    String md5 = calcDigestHex(digest, bytes);
                    
                    NSF nsf = NSFReader.readNSF(bytes);
        
                    createROM(rom, nsf);
                    
                    writeASMListing(rom, 
                            nsf.header.initDataAddr, 
                            listingsDir.resolve("init." + md5 + ".txt"));
                    
                    writeASMListing(rom, 
                            nsf.header.playDataAddr, 
                            listingsDir.resolve("play." + md5 + ".txt"));
                    
                    return new NSFFileInfo(
                            path.toAbsolutePath().toString(),
                            fileName,
                            parentDir,
                            md5,
                            bytes.length - 0x80,
                            nsf.header);
                    
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }
    
    private void writeASMListing(byte[] rom, int addr, Path path) throws Exception {
        
        try(BufferedWriter bout = Files.newBufferedWriter(path);
            PrintWriter out = new PrintWriter(bout)) {

            int numInstrsToPrint = 1000;
            AtomicInteger pc = new AtomicInteger(addr);
            Supplier<Byte> nextByte = () -> rom[pc.getAndIncrement()];
            
            for(int numInstrs = 0; numInstrs < numInstrsToPrint && pc.get() < rom.length - 3; numInstrs++) {
                
                out.print(HexUtils.toHex16(pc.get()));
                out.print(": ");
                
                int opCode = nextByte.get();
                Instruction instr = Instructions.BY_OP_CODE[opCode & 0xFF];
                
                if( instr != null ) {
                    switch(instr.addrMode.instrLen) {
                    
                    case 1: out.print(opCodeText(
                                    instr,
                                    0,
                                    0));
                            break;
                            
                    case 2: out.print(opCodeText(
                                    instr, 
                                    nextByte.get(), 
                                    0)); 
                            break;
                            
                    case 3: out.print(opCodeText(
                                    instr, 
                                    nextByte.get(), 
                                    nextByte.get())); 
                            break;
                    
                    default: 
                        throw new RuntimeException("unknown instr " + instr);
                    }
                } else {
                    out.print(toHex2(opCode));
                    out.print(" ; unknown op-code");
                }
                
                out.println();
                
                //if( instr != null && (instr.name == OpCodeName.RTS || instr.name == OpCodeName.BRK) ) {
                //    break;
                //}
            }
        }
    }

    private static void createROM(byte[] rom, NSF nsf) {
        Validate.notNull(rom);
        Validate.notNull(nsf);
        
        System.err.println("    creating ROM for " + nsf.header.songName);
        
        Arrays.fill(rom, (byte)0);
        
        Validate.isTrue(
                nsf.header.loadDataAddr == (nsf.header.loadDataAddr&0xFFFF));
        
        if( nsf.isBanked() ) {
            System.err.println("    banked " + Arrays.toString(nsf.header.bankswitchInitValues));
            
            if( ! nsf.isFDS() ) {
                loadBankedInitialROM(rom, nsf);
            } else {
                System.err.println("FDS bank switching not supported.");
            }
        } else {
            System.err.println("    non-banked");
            loadNonBankedROM(rom, nsf);
        }
    }
    
    private static void loadNonBankedROM(byte[] rom, NSF nsf) {
        for(int i = 0x80; i < nsf.data.length; i++) {
            int j = nsf.header.loadDataAddr + i - 0x80;
            
            if( j < rom.length ) {
                rom[j] = nsf.data[i];
            } else {
                System.err.printf("    %4x: %6d%n", nsf.header.loadDataAddr, nsf.data.length - 0x80);
                System.err.println("    TOO BIG!");
                break;
            }
        }
    }
    
    private static void loadBankedInitialROM(byte[] rom, NSF nsf) {
        List<byte[]> banks = new ArrayList<>();
        int bankSize = 4096;
        int padding = nsf.header.loadDataAddr & 0x0FFF;
        
        byte[] bank = new byte[bankSize];
        int bankIndex = padding;
        
        for(int i = 0x80; i < nsf.data.length; i++) {
            
            if( bankIndex >= bank.length) {
                banks.add(bank);
                bank = new byte[bankSize];
                bankIndex = 0;
            }
            
            bank[bankIndex++] = nsf.data[i];
        }
        
        banks.add(bank);
        
        System.err.println("    banks: " + banks.size());
        
        Validate.isTrue(nsf.header.bankswitchInitValues.length == 8);
        
        for(int i = 0; i < nsf.header.bankswitchInitValues.length; i++) {
            int selectedBankIndex = nsf.header.bankswitchInitValues[i];
            int register = 0x5FF8 + i;
            int startAddr = (8 + i)<<12;
            
            if( selectedBankIndex >= banks.size() ) {
                System.err.println("BANK index out of range");
                continue;
            }
            
            byte[] selectedBank = banks.get(selectedBankIndex);
            
            rom[register] = (byte)selectedBankIndex;
            System.arraycopy(selectedBank, 0, rom, startAddr, selectedBank.length);
        }
    }
    
    private static String calcDigestHex(MessageDigest digest, byte[] bytes) {
        digest.reset();
        byte[] digestBytes = digest.digest(bytes);
        
        return toHex(digestBytes);
    }
    
    
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        
        for(int i = 0; i < bytes.length; i++) {
            int d = bytes[i];
            
            sb.append(HEX_CHARS[(d>>4) &  0xf]);
            sb.append(HEX_CHARS[(d>>0) &  0xf]);
        }
        
        return sb.toString();
    }
}
