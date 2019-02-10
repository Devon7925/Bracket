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

    static String commentFilter(String code){
        int vallevel = 0,
            indexlevel = 0;
        boolean invar = false;
        String ret = "";
        boolean commenting = false;
        for(char c : code.toCharArray()){
            if(commenting){
                if(c == '\\') commenting = false;
            }else {
                if(c == '{')  vallevel++;
                if(c == '}')  vallevel--;
                if(c == '\'') invar = !invar;
                if(c == '[')  indexlevel++;
                if(c == ']')  indexlevel--;
                if(vallevel == 0 && !invar && indexlevel == 0 && c == '\\') commenting = true;
                else ret += c;
            }
        }
        return ret;
    }
}