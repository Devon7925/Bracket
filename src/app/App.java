package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class App {
    static ArrayList<Var> vars = new ArrayList<>();
    public static void main(String[] args) {
		String[] s = null;
		try {
			s = readFile("src/app/test.bcrt").split(";");
		} catch (Exception e) {
			e.printStackTrace();
        }
        for(String str : s) {
            System.out.println(str.trim()+";");
            execute(str);
        }
        for(Var v : vars){
            System.out.print('\n'+v.name+" - ");
            v.print();
        }
    }
    static void execute(String s){
        s = s.trim()+";";
        int mode = 0;
        String temp = "";
        Val tempval = null;
        for(char c : s.toCharArray()) switch(mode){
            case 0://check what to do with var
                if(c == '@'){
                    tempval = interpret(temp);
                    temp = "";
                    mode = 1;//set var
                    break;
                }else if(c == ';'){
                    tempval = interpret(temp);
                    if(tempval.vals == null); //do nothing
                    else if(tempval.vals.length == 0) execute("");
                    else if(tempval.vals[0] instanceof Bit)execute(StringTool.toString(tempval));
                    else{
                        for(Val v : tempval.vals){
                            if(v.vals[0] instanceof Bit)execute(StringTool.toString(v));
                        }
                    }
                    return;
                }
                temp += c;
            break;
            case 1://assign to value
                if(c == ';'){
                    tempval.set(interpret(temp));
                    if(tempval instanceof Var) setadd((Var) tempval);
                    return;
                }
                temp += c;
            break;
        }
    }
    static Val interpret(String s){
        Val ret = new Val();
        int mode = 0;
        int vallevel = 0;
        int indexlevel = 0;
        String temp = "";
        for(char c : s.toCharArray()) switch(mode){
            case 0://start
                if(c == '{'){
                    vallevel = 1;
                    mode = 1;
                    break;
                }else if(c == '\''){
                    mode = 2;
                    break;
                }
            break;
            case 1://value or array
                if(c == '{') vallevel++;
                if(c == '}') vallevel--;
                if(vallevel == 0){
                    if(StringTool.isList(temp)){
                        ArrayList<Val> newval = new ArrayList<Val>();
                        for(String str : StringTool.splitList(temp)){
                            newval.add(interpret(str));
                        }
                        ret = new Val();
                        ret.vals = new Val[newval.size()];
                        newval.toArray(ret.vals);
                    }else{
                        ret = new Val(temp);
                    }
                    temp = "";
                    vallevel = 0;
                    mode = 3;
                }
                temp += c;
            break;
            case 2: //Variable
                if(c == '`'){
                    ret = get(temp);
                    if(ret == null) ret = new Var(temp);
                    temp = "";
                    mode = 3;
                    break;
                }
                temp += c;
            break;
            case 3://read what to do with value or Variable
                if(c == '['){
                    indexlevel = 1;
                    mode = 4;
                    break;
                }
            break;
            case 4: //end index acess
            if(c == '[') indexlevel++;
            if(c == ']') indexlevel--;
            if(indexlevel == 0){
                if(temp.matches("\\d+")) ret = ret.vals[Integer.parseInt(temp)];
                else ret = ret.vals[interpret(temp).toInt()];
                temp = "";
                mode = 3;
                break;
            }
            temp += c;
            break;
        }
        return ret;
    }
    static Val get(String name){
        if(contains(new Var(name))){
            return vars.get(indexOf(new Var(name)));
        }
        return null;
    } 
    static void setadd(Var v){
        if(contains(v)){
            vars.set(indexOf(v), v);
        }else{
            vars.add(v);
        }
    }
    static boolean contains(Var v){
        for (Var var : vars) {
            if(var.name.equals(v.name))return true;
        }
        return false;
    }
    static int indexOf(Var v){
        int i = 0;
        for (Var var : vars) {
            if(var.name.equals(v.name))return i;
            i++;
        }
        return -1;
    }
	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		try {
			while((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			stringBuilder.delete(stringBuilder.lastIndexOf(ls), stringBuilder.length()) ;
			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}
}