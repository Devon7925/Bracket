package app.bcrt.compile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppTool {
    public static boolean isList(String s) {
        return baseIndex(s, ',') >= 0;
    }

    public static int baseIndex(String s, char match) {
        int bracketlevel = 0;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(c == '{') bracketlevel++;
            if(c == '}') bracketlevel--;
            if(bracketlevel == 0 && c == match) return i;
        }
        return -1;
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
                if(String.valueOf(c).matches("\\{|\\[")) bracketlevel++;
                if(String.valueOf(c).matches("\\}|\\]")) bracketlevel--;
                if(c == '\'') invar = !invar;
                if(c == '\\' && bracketlevel == 0 && !invar) commenting = true;
                else ret += c;
            }
        }
        return ret;
    }

    public static String fileToString(String path) {
        BufferedReader reader = null;
        var stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        try {
            String line = null;
            reader = new BufferedReader(new FileReader(path));
            while((line = reader.readLine()) != null)
                stringBuilder.append(line).append(ls);
            stringBuilder.delete(stringBuilder.lastIndexOf(ls), stringBuilder.length());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String getCode(String path) {
        return removeComments(fileToString(path));
    }

    public static String litToVal(String literal) {
        return new Val(null, literal).toString();
    }

    public static double log(double base, double val) {
        return Math.log(val) / Math.log(base);
    }

    public static boolean isNumeric(String s) {
        return s.matches("\\d+");
    }

    public static Val valRebase(Val holder, Val v) {
        Val ret = v.clone();
        ret.holder = holder;
        return ret;
    }
}