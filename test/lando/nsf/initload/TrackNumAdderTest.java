package lando.nsf.initload;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.Assert;

public class TrackNumAdderTest {

    @Test
    public void insert_num() {
        test("/Foo/bar/baz.raw", "/Foo/bar/baz.001.raw");
    }
    
    @Test
    public void dot_at_end() {
        test("/Foo/bar/baz.", "/Foo/bar/baz.001");
    }
    
    @Test
    public void dot_at_start() {
        test("/Foo/bar/.baz", "/Foo/bar/.baz.001");
    }
    
    @Test
    public void not_dot_at_all() {
        test("/Foo/bar/baz", "/Foo/bar/baz.001");
    }
    
    private void test(String inFile, String expected) {
        TrackNumAdder adder = new TrackNumAdder();
        Path inPath = Paths.get(inFile);
        Path outPath = adder.addTrackNum(inPath, 1);
        
        Assert.assertEquals(expected, outPath.toString());
    }
}
