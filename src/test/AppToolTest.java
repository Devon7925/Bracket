package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import app.bcrt.compile.AppTool;

class AppToolTest {
    String test1 = "{1}, {2}, {3}";
    String test2 = "{1,,} {2,{3}} {3}";

    @Test
    public void testIsList() {
        assertEquals(AppTool.isList(test1), true);
        assertEquals(AppTool.isList(test2), false);
    }

    @Test
    public void testBaseIndex() {
        assertEquals(AppTool.baseIndex("{,,}fef{}aesf", ','), -1);
        assertEquals(AppTool.baseIndex("{}fef{}aesf", ','), -1);
        assertEquals(AppTool.baseIndex("{,,}fef{},aesf", ','), 9);
        assertEquals(AppTool.baseIndex("{,,}fef{}paesf", 'p'), 9);
        assertEquals(AppTool.baseIndex(",{,,}fef{}aesf", ','), 0);
    }

    @Test
    public void testStringToElems() {
        assertIterableEquals(AppTool.stringToElems(test1), Arrays.asList("{1}", " {2}", " {3}"));
        assertIterableEquals(AppTool.stringToElems(test2), Arrays.asList(test2));
    }

    @Test
    public void testRemoveComments() {
        assertEquals(AppTool.removeComments("\\code\\"), "");
        assertEquals(AppTool.removeComments("{\\}code\\"), "{\\}code");
    }

    @Test
    public void testFileToString() {
        assertEquals(AppTool.fileToString("src/test/AssignTest.bcrt"), "{'a'@{5}},{'a'@{6}}\\comment\\");
    }

    @Test
    public void testGetCode() {
        assertEquals(AppTool.getCode("src/test/AssignTest.bcrt"), "{'a'@{5}},{'a'@{6}}");
    }

    @Test
    public void testLitToVal() {
        assertEquals(AppTool.litToVal("code"), "{code},");
        assertEquals(AppTool.litToVal("{}code"), "{{}code},");
        assertEquals(AppTool.litToVal("123"), "{123},");
        assertEquals(AppTool.litToVal("1"), "{T},");
    }

    @Test
    public void testLog() {
        assertEquals(AppTool.log(2, 4), 2);
        assertEquals(AppTool.log(10, 100), 2);
        assertEquals(Math.round(AppTool.log(10, 1000)), 3);
    }

    @Test
    public void testIsNumeric() {
        assertEquals(AppTool.isNumeric(""), false);
        assertEquals(AppTool.isNumeric("5"), true);
        assertEquals(AppTool.isNumeric("123"), true);
        assertEquals(AppTool.isNumeric("fr3d"), false);
    }
}