package app.bcrt.compile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import app.bcrt.methods.Execute;

public class App {

    public static final ArrayList<Var> vars = new ArrayList<>();

    public static final boolean debuging = false;

    public static void main(String[] args) throws IOException {
        vars.add(new Execute());
        for(String file : args) executeFile(file);
        vars.forEach(System.out::println);
    }

    public static void executeFile(String path) {
        String[] split  = path.split("\\.");
        if(split.length >= 2){
            String extension = split[1];
            switch(extension){
                case "bcrt": loadBcrtMethod(path);
                break;
                case "java": loadJavaMethod(path);
                break;
                default:
                throw new IllegalArgumentException("File type not recognized");
            }
        }else throw new IllegalArgumentException("Input must be path to file");
    }

    public static String[] getCodeLines(String path){
        try {
            return StringTool.commentFilter(fileToString(path)).split(";");
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public static void loadBcrtMethod(String path) {
        for(String line : getCodeLines(path))
            execute(line, null);
    }

    public static void loadJavaMethod(String path) {
        File newMeth = new File(path);
        if (newMeth.getParentFile().exists() || newMeth.getParentFile().mkdirs()) {
            try {
                // Compilation Requirements
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                Iterable<? extends JavaFileObject> compilationUnit = fileManager
                        .getJavaFileObjectsFromFiles(Arrays.asList(newMeth));
                JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null,
                        compilationUnit);
                if (task.call()) {
                    // Load and execute
                    URLClassLoader classLoader = new URLClassLoader(new URL[] { new File("./").toURI().toURL() });
                    Object newmethod = classLoader.loadClass(
                        path.replaceAll("/", ".").replace("src.", "").replace(".java", "")
                    ).getConstructors()[0].newInstance();

                    classLoader.close();
                    if (newmethod instanceof Var) setVar((Var) newmethod);
                } else {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.out.format("Error on line %d in %s%n",
                                diagnostic.getLineNumber(),
                                diagnostic.getSource().toUri());
                    }
                }
                fileManager.close();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | 
                    IllegalArgumentException | InvocationTargetException | SecurityException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    static Val execute(String s, Val context){
        s = s.trim()+";";
        if(debuging) System.out.println(s);
        int mode = 0;
        String temp = "";
        Val tempval = null;
        int vallevel = 0;
        for(char c : s.toCharArray()) {
            switch(mode){
                case 0://check what to do with var
                    if(c == '{') vallevel++;
                    if(c == '}') vallevel--;
                    if(vallevel == 0) {
                        if(c == '@'){
                            tempval = interpret(temp, context);
                            temp = "";
                            mode = 1;//set var
                            break;
                        }else if(c == ';'){
                            tempval = interpret(temp, context);
                            if(tempval != null){
                                Val ret = tempval.execute(context);
                                if(ret != null) return ret;
                            }
                        }else if(c == '~'){
                            temp = "";
                            mode = 2;
                            break;
                        }
                    }
                break;
                case 1://assign to value
                    if(c == ';'){
                        tempval.set(interpret(temp, context));
                        if(tempval instanceof Var && ((Var) tempval).holder == null) setVar((Var) tempval);
                        return null;
                    }
                break;
                case 2: if(c == ';') return new Val(interpret(temp, context)); //return value
                break;
            }
            temp += c;
        }
        return null;
    }

    public static Val interpret(String s, Val context){
        s += ";";
        Val ret = null;
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
                    if(c == '{') mode = 1;//value
                    else if(c == '\'') mode = 2;//variable
                    else if(c == '(')  mode = 7;//group
                    else if(c == ':') {
                        ret = context;
                        mode = 3;//read what to do with this
                    }
                break;
                case 1://value
                    if(vallevel == 0){
                        if(StringTool.isList(temp)){
                            ret = new Val();
                            ret.vals = new ArrayList<>(StringTool.splitList(temp).stream().map(n -> interpret(n, context)).map(Val::new).collect(Collectors.toList()));
                        }else ret = new Val(temp);
                        temp = "";
                        mode = 3;//read what to do with this
                    }
                    temp += c;
                break;
                case 2: //Variable
                    if(c == '\''){
                        ret = get(temp);
                        if(ret == null) ret = new Var(temp);
                        temp = "";
                        mode = 3;//read what to do with this
                        break;
                    }
                    temp += c;
                break;
                case 3://read what to do with ret
                    if(c == '['){
                        temp = "";
                        mode = 4; //read index acess
                    }else if(c == '.'){
                        temp = "";
                        mode = 8; //subelem access
                    }else{
                        temp = ""+c;
                        mode = 5; //read operation
                    }
                break;
                case 4: //read index acess
                    if(indexlevel == 0){
                        if(temp.matches("\\d+")) ret = ret.get(Integer.parseInt(temp));
                        else if(temp.contains(":")){
                            Val newret = new Val();
                            for (int i = 0; i < ret.vals.size(); i++)
                                if(interpret(temp, new Val(i)).toInt() == 1) newret.vals.add(ret.vals.get(i).clone());
                            ret = newret;
                        }else ret = ret.get(interpret(temp, context).toInt());
                        temp = "";
                        mode = 3;//read what to do with ret
                        break;
                    }
                    temp += c;
                break;
                case 5://read operation
                    if(c == '{' || c == '\'' || c == '(' || c == ':'){
                        mode = 6;//read second input to operation
                        op = temp;
                        temp = "";
                    }
                    temp += c;
                break;
                case 6://read second input to operation
                    if(c == ';'){
                        Val tempb = interpret(temp, context);
                        Var a = new Var("a");
                        Var b = new Var("b");
                        if(ret instanceof Var){
                            a = (Var) ret.clone();
                            a.name = "a";
                        }else 
                            a.set(ret);
                        if(tempb instanceof Var){
                            b = (Var) tempb.clone();
                            b.name = "b";
                        }else 
                            b.set(tempb);
                        Val v = get(op);
                        vars.add(0, a);
                        vars.add(0, b);
                        ret = v.execute(v);
                        vars.remove(0);
                        vars.remove(0);
                    }
                    temp += c;
                break;
                case 7://end paren
                    if(parenlevel == 0){
                        ret = interpret(temp, context);
                        temp = "";
                        mode = 3;//read what to do with ret
                    }
                    temp += c;
                break;
                case 8://subelem acess
                    if(c == ';') return ((Var) ret).get(interpret(temp, context).toString());
                    temp += c;
                break;
            }
        }
        return ret;
    }
    
    public static Val get(String name){
        return vars.stream().filter(n -> n.name.equals(name)).findAny().orElse(null);
    }

    public static void setVar(Var v){
        int index = vars.indexOf(get(v.name));
        if(index == -1) vars.add(v); //if it does not already exist, add it
        else vars.set(index, v); //otherwise set it
    }

	private static String fileToString(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		try {
			while((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			stringBuilder.delete(stringBuilder.lastIndexOf(ls), stringBuilder.length());
			return stringBuilder.toString();
		} finally {
			reader.close();
		}
	}
}