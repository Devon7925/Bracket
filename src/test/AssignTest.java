package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import app.bcrt.compile.App;
import app.bcrt.compile.AppTool;
import app.bcrt.compile.Loader;
import app.bcrt.compile.Val;

/**
 * AssignTest
 */
public class AssignTest {

    @Test
    public void testAssign() {
        Val root = new Val(null, Arrays.asList(Loader.loadBcrtMethod("src/test/AssignTest.bcrt")));
        App app = new App(root);
        assertEquals(app.root.get(AppTool.litToVal("a")).asInt(), 6);
        assertEquals(app.root.get(AppTool.litToVal("b")).asInt(), 3);
    }
}