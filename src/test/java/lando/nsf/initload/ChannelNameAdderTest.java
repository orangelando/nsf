package lando.nsf.initload;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import lando.nsf.app.towav.ChannelNameAdder;

public class ChannelNameAdderTest {

    @Test
    public void insert_normal() {
        test("/Foo/bar/baz.raw", "/Foo/bar/baz-tri.raw");
    }
    
    @Test
    public void after_track_num() {
        test("/Foo/bar/baz-01.raw", "/Foo/bar/baz-01-tri.raw");
    }
        
    private void test(String inFile, String expected) {
        ChannelNameAdder adder = new ChannelNameAdder();
        Path inPath = Paths.get(inFile);
        Path outPath = adder.addChannelName(inPath, "tri");
        
        Assert.assertEquals(expected, outPath.toString());
    }

}
