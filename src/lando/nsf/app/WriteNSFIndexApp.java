package lando.nsf.app;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lando.nsf.NSF;
import lando.nsf.NSFHeader;
import lando.nsf.NSFReader;

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
        public final NSFHeader header;
        
        public NSFFileInfo(String path, String fileName, String parentDir, String md5, NSFHeader header) {
            this.path      = Objects.requireNonNull(path);
            this.fileName  = Objects.requireNonNull(fileName);
            this.parentDir = Objects.requireNonNull(parentDir);
            this.md5       = Objects.requireNonNull(md5);
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
        
        List<NSFFileInfo> infos = fetchFiles();
        out.println("found " + infos.size() + " infos.");
        
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        Path outFile = Paths.get(indexDir).resolve("nsf-index.json");
        out.println("writing to " + outFile);
        
        byte[] jsonBytes = jsonMapper.writeValueAsBytes(infos);
        Files.write(outFile, jsonBytes);
        
        out.println("done");
    }
    
    private List<NSFFileInfo> fetchFiles() throws Exception {
        
        MessageDigest digest = MessageDigest.getInstance("MD5");
        
        return Files.walk(Paths.get(nsfDir))
            .filter(p -> p.getFileName().toString().endsWith(".nsf"))
            .map(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String parentDir = path.getParent().getFileName().toString();
                    
                    long fileSize = Files.size(path);
                    Validate.isTrue( fileSize <  Integer.MAX_VALUE );
                    
                    byte[] bytes = Files.readAllBytes(path);
                    Validate.isTrue( fileSize == bytes.length );
        
                    String md5 = calcDigestHex(digest, bytes);
                    
                    NSF nsf = NSFReader.readNSF(bytes);
                    
                    return new NSFFileInfo(
                            path.toAbsolutePath().toString(),
                            fileName,
                            parentDir,
                            md5,
                            nsf.header);
                    
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
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
