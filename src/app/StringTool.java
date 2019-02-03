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
        for (int i = 0; i < val.vals.size(); i+=8) {
            int ch = 0;
            for(int j = 0; j < 8; j++)
                ch += val.vals.get(i+j).toInt() << j;
            ret += (char) ch;
        }
        return ret;
    }
}