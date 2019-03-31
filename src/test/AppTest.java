package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import app.bcrt.compile.App;
import app.bcrt.compile.Load;
import app.bcrt.compile.Loader;
import app.bcrt.compile.Val;

/**
 * AppTest
 */
public class AppTest {
    @Test
    public void testInterpretArgs() {
        String[] args = {"-d", "0", "-d", "999", "path"};
        assertEquals(App.interpretArgs(args), "path");
        assertEquals(App.debugLevel, 999);
    }

    @Test
    public void testConstructor() {
        Val root = new Val(null);
        root.set(Arrays.asList(Loader.loadFile("src/test/AssignTest.bcrt", root)));
        App app = new App(root);
        assertEquals(app.root.get("load").getClass(), Load.class);
        assertEquals(app.root.get("{a},").toString(), "{6},");
    }
}