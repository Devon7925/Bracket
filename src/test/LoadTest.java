package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import app.bcrt.compile.App;
import app.bcrt.compile.AppTool;
import app.bcrt.compile.Loader;
import app.bcrt.compile.Val;

/**
 * LoadTest
 */
public class LoadTest {

    @Test
    public void testLoad() {
        Val root = new Val(null, Arrays.asList(Loader.loadBcrtMethod("src/test/LoadTest.bcrt")));
        App app = new App(root);
        assertEquals(app.root.get(AppTool.litToVal("remove")).getClass(), 0);
        assertEquals(app.root.get(AppTool.litToVal("a")).asInt(), 0);
    }
}