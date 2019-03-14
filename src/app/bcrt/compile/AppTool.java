package app.bcrt.compile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppTool {
    public static boolean isList(String s) {
        int bracketlevel = 0;
        for(char c : s.toCharArray()) {
            if(c == '{') bracketlevel++;
            if(c == '}') bracketlevel--;
            if(bracketlevel == 0 && c == ',') return true;
        }
        return false;
    }

    public static List<String> stringToElems(String s) {
        int bracketlevel = 0;
        List<String> ret = new ArrayList<>();
        String temp = "";
        for(char c : s.toCharArray()) {
            if(c == '{') bracketlevel++;
            if(c == '}') bracketlevel--;
            if(bracketlevel == 0 && c == ',') {
                ret.add(temp);
                temp = "";
            } else temp += c;
        }
        ret.add(temp);
        return ret;
    }

    public static String removeComments(String code) {
        int bracketlevel = 0;
        boolean invar = false;
        String ret = "";
        boolean commenting = false;
        for(char c : code.toCharArray()) {
            if(commenting) {
                if(c == '\\') commenting = false;
            } else {
                if(c == '{' || c == '[') bracketlevel++;
                if(c == '}' || c == ']') bracketlevel--;
                if(c == '\'') invar = !invar;
                if(c == '\\' && bracketlevel == 0 && !invar) commenting = true;
                else ret += c;
            }
        }
        return ret;
    }

    public static String fileToString(String file) {
        BufferedReader reader = null;
        var stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        try {
            String line = null;
            reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.delete(stringBuilder.lastIndexOf(ls), stringBuilder.length());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    public static String getCode(String path) {
        return AppTool.removeComments(fileToString(path));
    }

    public static String litToVal(String literal) {
        return new Val(literal).toString();
    }
    
    public static double log(double base, double val){
        return Math.log(val) / Math.log(base);
    }
}