package lando.nsf.app.info;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lando.nsf.HexUtils;
import lando.nsf.NSF;
import lando.nsf.NSFReader;

public final class LandoNSFInfoApp {

    @Option(name="-json", required=false)
    private boolean json = false;
        
    @Argument
    private List<String> arguments = new ArrayList<>();
    
    public static void main(String [] args) throws Exception {
        PrintStream out = System.out;
        LandoNSFInfoApp app = new LandoNSFInfoApp();
        CmdLineParser cmdParser = new CmdLineParser(app);
        
        try {
            cmdParser.parseArgument(args);
        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            cmdParser.printUsage(System.err);
            return;
        }
        
        app.exec(out);
    }
    
    private void exec(PrintStream out) throws Exception {
        Validate.notNull(out);
        
        if( arguments.size() == 0 ) {
            System.err.println("Expecting path to .nsf file as argument.");
            return;
        }
        
        if( arguments.size() != 1 ) {
            System.err.println("Only expecting 1 argument and not " + arguments.size());
            return;
        }
        
        Path path = Paths.get(arguments.get(0));
        
        if( ! Files.exists(path) ) {
            System.err.printf("%s does not exist.", path);
        }
        
        NSF nsf;
        
        try {
            nsf = NSFReader.readNSF(path.toFile());
        } catch(Exception e) {
            System.err.println("Unable to read nsf file: " + e.getMessage());
            return;
        }
        
        NSFInfo info = extractInfo(nsf);
        
        if( json ) {
            printJson(out, info);
        } else {
            printPlainTxt(out, info);
        }
    }

    private NSFInfo extractInfo(NSF nsf) {
        Validate.notNull(nsf);        
        
        return new NSFInfo(
                nsf.header.versionNumber,
                nsf.header.totalSongs,
                nsf.header.startingSong,
                nsf.header.loadDataAddr,
                nsf.header.initDataAddr,
                nsf.header.playDataAddr,
                nsf.header.songName,
                nsf.header.artistName,
                nsf.header.copyrightHolder,
                nsf.header.ntscSpeed,
                nsf.header.bankswitchInitValues,
                nsf.header.palSpeed,
                nsf.header.getNtscPalMode(),
                nsf.header.getSupportedExtraSoundChips()
                );
    }
    
    private void printJson(PrintStream out, NSFInfo info) throws Exception {
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        out.println(jsonMapper.writeValueAsString(info));
    }
    
    private void printPlainTxt(PrintStream out, NSFInfo info) throws Exception {
        
        out.printf("Version Number    : %d%n", info.versionNumber);
        out.printf("Total Songs       : %d%n", info.totalSongs);
        out.printf("Starting Song     : %d%n", info.startingSong);
        
        out.println();
        
        out.printf("Song Name         : %s%n", info.songName);
        out.printf("Artist Name       : %s%n", info.artistName);
        out.printf("Copyright Holder  : %s%n", info.copyrightHolder);
        
        out.println();

        out.printf("NTSC/Pal Mode     : %s%n", info.ntscPalMode);
        out.printf("NTSC Speed        : %s%n", info.ntscSpeed);
        out.printf("PAL Speed         : %s%n", info.palSpeed);
        
        out.println();
        
        out.printf("Bankswitch Vals   : %s%n", Arrays.toString(info.bankswitchInitValues));
        out.printf("Extra Sound Chips : %s%n", info.supportedExtraSoundChips);
        
        out.println();
        
        out.printf("Load Data Addr    : %s%n", "$" + HexUtils.toHex16(info.loadDataAddr));
        out.printf("Init Data Addr    : %s%n", "$" + HexUtils.toHex16(info.initDataAddr));
        out.printf("Play Data Addr    : %s%n", "$" + HexUtils.toHex16(info.playDataAddr));
    }
}
