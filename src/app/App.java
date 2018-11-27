package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class App {
    static ArrayList<Var> vars = new ArrayList<>();
    public static void main(String[] args) throws Exception {
		String[] s = null;
		try {
			s = readFile("src/app/test.bcrt").split(";");
		} catch (Exception e) {
			e.printStackTrace();
        }
        for(String str : s) {
            str = str.trim()+";";
            System.out.println(str);
            execute(str);
        }
        System.out.println("");
        for(Var v : vars){
            System.out.print(v.name+" - ");
            v.print();
            System.out.println("");
        }
    }
    static void execute(String s){
        int mode = 0;
        String temp = "";
        Var tempvar = null;
        for(char c : s.toCharArray()) switch(mode){
            case 0://base
            if(c == '\'')
                mode = 1;//read var name
            break;
            case 1://read var name
                if(c == '`'){
                    tempvar = new Var(temp);
                    temp = "";
                    mode = 2;//check what to do with var
                    break;
                }
                temp += c;
            break;
            case 2://check what to do with var
                if(c == '@'){
                    temp = "";
                    mode = 3;//set var
                    break;
                }else if(c == ';'){
                    Val val = interpret("\'"+tempvar.name+"`"+temp);
                    if(val.vals.length == 0) execute("");
                    else if(val.vals[0].interp) execute(StringTool.toString(val)+";");
                    else{
                        for(Val v : val.vals){
                            if(v.vals[0].interp) execute(StringTool.toString(v)+";");
                        }
                    }
                    temp = "";
                    mode = 0;
                    break;
                }
                temp += c;
            break;
            case 3:
                if(c == ';'){
                    tempvar.set(interpret(temp));
                    setadd(tempvar);
                    tempvar = null;
                    mode = 0;//done
                    break;
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
        return new Bit(false);
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
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");
	
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