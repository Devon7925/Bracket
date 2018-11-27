package app;

import java.util.ArrayList;

class StringTool {
    static boolean isList(String s){
        int level = 0;
        for(char c : s.toCharArray()){
            if(c == '{') level++;
            if(c == '}') level--;
            if(level == 0 && c == ',') return true;
        }
        return false;
    }
    static ArrayList<String> splitList(String s){
        int level = 0;
        ArrayList<String> ret = new ArrayList<>();
        String temp = "";
        for(char c : s.toCharArray()){
            if(c == '{') level++;
            if(c == '}') level--;
            if(level == 0 && c == ','){
                ret.add(temp);
                temp = "";
            }else temp += c;
        }
        ret.add(temp);
        return ret;
    }
    static String toString(Val val){
        String ret = "";
        for (int i = 0; i < val.vals.length; i+=8) {
            int ch = 0;
            ch += val.vals[i+0].toInt() << 0;
            ch += val.vals[i+1].toInt() << 1;
            ch += val.vals[i+2].toInt() << 2;
            ch += val.vals[i+3].toInt() << 3;
            ch += val.vals[i+4].toInt() << 4;
            ch += val.vals[i+5].toInt() << 5;
            ch += val.vals[i+6].toInt() << 6;
            ch += val.vals[i+7].toInt() << 7;
            ret += (char) ch;
        }
        return ret;
    }
}