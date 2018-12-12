package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class App {
    static ArrayList<Var> vars = new ArrayList<>();
    public static void main(String[] args) {
		String[] lines = null;
		try {
			lines = readFile("src/app/test.bcrt").split(";");
		} catch (Exception e) {
			e.printStackTrace();
        }
        for(String line : lines) execute(line);
        vars.forEach(v -> v.print());
    }
    static Val execute(String s){
        s = s.trim()+";";
        System.out.println(s);
        int mode = 0;
        String temp = "";
        Val tempval = null;
        int vallevel = 0;
        for(char c : s.toCharArray()) switch(mode){
            case 0://check what to do with var
                if(c == '{') vallevel++;
                if(c == '}') vallevel--;
                if(vallevel == 0) {
                    if(c == '@'){
                        tempval = interpret(temp);
                        temp = "";
                        mode = 1;//set var
                        break;
                    }else if(c == ';'){
                        tempval = interpret(temp);
                        Val ret = null;
                        if(tempval == null);//do nothing
                        else if(tempval.vals == null); //do nothing
                        else if(tempval.vals.length == 0); //do nothing
                        else ret = execute(tempval);
                        if(ret != null) return ret;
                    }else if(c == '~'){
                        temp = "";
                        mode = 2;
                        break;
                    }
                }
                temp += c;
            break;
            case 1://assign to value
                if(c == ';'){
                    tempval.set(interpret(temp));
                    if(tempval instanceof Var) setadd((Var) tempval);
                    return null;
                }
                temp += c;
            break;
            case 2:
            if(c == ';'){
                return new Val(interpret(temp));
            }
            temp += c;
            break;
        }
        return null;
    }
    static Val execute(Val v){
        Val ret = null;
        if(v.vals[0] instanceof Bit) return execute(StringTool.toString(v));
        else for(Val v1 : v.vals) if(v1.vals[0] instanceof Bit) {
            Val toret = execute(v1);
            if(toret != null) ret = toret;
        }
        return ret;
    }
    static Val interpret(String s){
        s += ";";
        Val ret = new Val();
        int mode = 0;
        int vallevel = 0,
          indexlevel = 0,
          parenlevel = 0;
        String temp = "";
        String op = "";
        for(char c : s.toCharArray()){
            if(c == '{') vallevel++;
            else if(c == '}') vallevel--;
            else if(c == '[') indexlevel++;
            else if(c == ']') indexlevel--;
            else if(c == '(') parenlevel++;
            else if(c == ')') parenlevel--;
            switch(mode){
                case 0://start
                    if(c == '{') mode = 1;
                    else if(c == '\'') mode = 2;
                    else if(c == '(')  mode = 7;
                break;
                case 1://value or array
                    if(vallevel == 0){
                        if(StringTool.isList(temp)){
                            ArrayList<Val> newval = new ArrayList<Val>();
                            for(String str : StringTool.splitList(temp))
                                newval.add(interpret(str));
                            ret = new Val();
                            ret.vals = new Val[newval.size()];
                            newval.toArray(ret.vals);
                        }else{
                            ret = new Val(temp);
                        }
                        temp = "";
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
                        temp = "";
                        mode = 4;
                    }else{
                        temp = ""+c;
                        mode = 5;
                    }
                break;
                case 4: //end index acess
                if(indexlevel == 0){
                    if(temp.matches("\\d+")) ret = ret.vals[Integer.parseInt(temp)];
                    else ret = ret.vals[interpret(temp).toInt()];
                    temp = "";
                    mode = 3;
                    break;
                }
                temp += c;
                break;
                case 5://read operation
                if(c == '{' || c == '\''){
                    mode = 6;
                    op = temp;
                    temp = "";
                }
                temp += c;
                break;
                case 6://read second input to operation
                if(c == ';'){
                    Var a = new Var("a");
                    Var b = new Var("b");
                    a.set(ret);
                    b.set(interpret(temp));
                    vars.add(0, a);
                    vars.add(0, b);
                    ret = execute(get(op));
                    vars.remove(0);
                    vars.remove(0);
                }
                temp += c;
                break;
                case 7:
                if(parenlevel == 0){
                    ret = interpret(temp);
                    temp = "";
                    mode = 3;
                }
                temp += c;
                break;
            }
        }
        return ret;
    }
    static Val get(String name){
        if(contains(new Var(name)))
            return vars.get(indexOf(new Var(name)));
        return null;
    } 
    static void setadd(Var v){
        if(contains(v)) vars.set(indexOf(v), v);
        else vars.add(v);
    }
    static boolean contains(Var v){
        for (Var var : vars) 
            if(var.name.equals(v.name)) return true;
        return false;
    }
    static int indexOf(Var v){
        for (int i = 0; i < vars.size(); i++)
            if(vars.get(i).name.equals(v.name)) return i;
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