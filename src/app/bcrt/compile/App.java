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

public class App {

    public static final ArrayList<Var> vars = new ArrayList<>();

    public static int debugLevel = 1;

    public static void main(String[] args) throws IOException {
        vars.add(new Execute());
        for(String file : args) executeFile(file);
        if(debugLevel >= 1) vars.forEach(System.out::println);
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
            return StringTool.removeComments(fileToString(path)).split(";");
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
                    else throw new IllegalArgumentException("Improper format");
                } else {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                        System.err.format("Error on line %d in %s%n",
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
        if(debugLevel >= 2) System.out.println(s);
        int mode = 0;
        String current = "";
        Val tempval = null;
        int bracketlevel = 0;
        for(char c : s.toCharArray()) {
            switch(mode){
                case 0://check what to do with var
                    if(c == '{') bracketlevel++;
                    if(c == '}') bracketlevel--;
                    if(bracketlevel == 0) {
                        if(c == '@'){
                            tempval = interpret(current, context);
                            current = "";
                            mode = 1;//set var
                            break;
                        }else if(c == ';'){
                            tempval = interpret(current, context);
                            if(tempval != null){
                                Val ret = tempval.execute(context);
                                if(ret != null) return ret;
                            }
                        }else if(c == '~'){
                            current = "";
                            mode = 2;
                            break;
                        }
                    }
                break;
                case 1://assign to value
                    if(c == ';'){
                        tempval.set(interpret(current, context));
                        if(tempval instanceof Var && ((Var) tempval).holder == null) setVar((Var) tempval);
                        return null;
                    }
                break;
                case 2: if(c == ';') return new Val(interpret(current, context)); //return value
                break;
            }
            current += c;
        }
        return null;
    }

    public static Val interpret(String s, Val context){
        s += ";";
        Val ret = null;
        int mode = 0;
        int bracketlevel = 0;
        String current = "";
        String operation = "";
        for(char c : s.toCharArray()){
            switch(c){
                case '{': case '[': case '(': bracketlevel++;
                break;
                case '}': case ']': case ')': bracketlevel--;
                break;
            }
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
                    if(bracketlevel == 0){
                        if(StringTool.isList(current)){
                            ret = new Val();
                            ret.vals = new ArrayList<>(StringTool.stringToElems(current).stream().map(n -> interpret(n, context)).map(Val::new).collect(Collectors.toList()));
                        }else ret = new Val(current);
                        current = "";
                        mode = 3;//read what to do with this
                    }
                    current += c;
                break;
                case 2: //Variable
                    if(c == '\''){
                        ret = get(current);
                        if(ret == null) ret = new Var(current);
                        current = "";
                        mode = 3;//read what to do with this
                        break;
                    }
                    current += c;
                break;
                case 3://read what to do with ret
                    if(c == '['){
                        current = "";
                        mode = 4; //read index acess
                    }else{
                        current = ""+c;
                        mode = 5; //read operation
                    }
                break;
                case 4: //read index acess
                    if(bracketlevel == 0){
                        if(current.matches("\\d+")) ret = ret.get(Integer.parseInt(current));
                        else if(current.contains(":")){
                            Val newret = new Val();
                            for (int i = 0; i < ret.vals.size(); i++)
                                if(interpret(current, new Val(i)).interpretInt() == 1) newret.vals.add(ret.vals.get(i).clone());
                            ret = newret;
                        }else ret = ret.get(interpret(current, context).interpretInt());
                        current = "";
                        mode = 3;//read what to do with ret
                        break;
                    }
                    current += c;
                break;
                case 5://read operation
                    if(c == '{' || c == '\'' || c == '(' || c == ':'){
                        mode = 6;//read second input to operation
                        operation = current;
                        current = "";
                    }
                    current += c;
                break;
                case 6://read second input and execute operation
                    if(c == ';'){
                        Val tempb = interpret(current, context);
                        if(operation.equals(".")){
                            ret = ((Var) ret).get(tempb.toString());
                        }else{
                            Var a = new Var("a");
                            Var b = new Var("b");
                            if(ret instanceof Var){
                                a = (Var) ret.clone();
                                a.name = "a";
                            }else a.set(ret);
                            if(tempb instanceof Var){
                                b = (Var) tempb.clone();
                                b.name = "b";
                            }else b.set(tempb);
                            Val v = get(operation);
                            vars.add(0, a);
                            vars.add(0, b);
                            ret = v.execute(v);
                            vars.remove(0);
                            vars.remove(0);
                        }
                    }
                    current += c;
                break;
                case 7://end paren
                    if(bracketlevel == 0){
                        ret = interpret(current, context);
                        current = "";
                        mode = 3;//read what to do with ret
                    }
                    current += c;
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