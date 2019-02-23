package app.bcrt.compile;

import java.io.File;
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

    public static int debugLevel = 0;

    public static void main(String[] args) throws IOException {
        vars.add(new Execute());
        interpretArgs(args);
        for(String file : args[args.length-1].split(",")) executeFile(file);
        if(debugLevel >= 1) vars.forEach(System.out::println);
    }

    public static void interpretArgs(String[] args){
        for (int i = 0; i < args.length-1; i++) {
            if(args[i].equals("-d")){
                String debug = args[++i];
                if(debug.matches("\\d+")){
                    debugLevel = Integer.parseInt(debug);
                }else throw new IllegalArgumentException("Debug level must be integer");
            }
        }
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

    public static void loadBcrtMethod(String path) {
        for(String line : StringTool.getCodeLines(path))
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
        Mode mode = Mode.START;
        String current = "";
        Val tempval = null;
        int bracketlevel = 0;
        for(char c : s.toCharArray()) {
            switch(mode){
                case START:
                    if(c == '{') bracketlevel++;
                    if(c == '}') bracketlevel--;
                    if(bracketlevel == 0) {
                        if(c == '@'){
                            tempval = interpret(current, context);
                            current = "";
                            mode = Mode.ASSIGN;
                            break;
                        }else if(c == ';'){
                            tempval = interpret(current, context);
                            if(tempval != null){
                                Val ret = tempval.execute(tempval instanceof Var?tempval:context);
                                if(ret != null) return ret;
                            }
                        }else if(c == '~'){
                            current = "";
                            mode = Mode.RETURN;
                            break;
                        }
                    }
                break;
                case ASSIGN:
                    if(c == ';'){
                        tempval.set(interpret(current, context));
                        if(tempval instanceof Var && ((Var) tempval).holder == null) setVar((Var) tempval);
                        return null;
                    }
                break;
                case RETURN: 
                    if(c == ';') return new Val(interpret(current, context));
                break;
                default: System.err.println("Something very wrong happened");
            }
            current += c;
        }
        return null;
    }

    public static Val interpret(String s, Val context){
        s += ";";
        Val ret = null;
        Mode mode = Mode.START;
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
                case START:
                    if(c == '{') mode = Mode.VALUE;
                    else if(c == '\'') mode = Mode.VARIABLE;
                    else if(c == '(')  mode = Mode.PARENTHESIS;
                    else if(c == ':') {
                        ret = context;
                        mode = Mode.MODIFIER;
                    }
                break;
                case VALUE:
                    if(bracketlevel == 0){
                        if(StringTool.isList(current)){
                            ret = new Val();
                            ret.value = new ArrayList<>(
                                StringTool.stringToElems(current).stream().map(n -> interpret(n, context)).map(Val::new).collect(Collectors.toList())
                            );
                        }else ret = new Val(current);
                        current = "";
                        mode = Mode.MODIFIER;
                    }
                    current += c;
                break;
                case VARIABLE:
                    if(c == '\''){
                        ret = get(current);
                        if(ret == null) ret = new Var(current);
                        current = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    current += c;
                break;
                case MODIFIER://read what to do with ret
                    if(c == '['){
                        current = "";
                        mode = Mode.INDEX;
                    }else{
                        current = ""+c;
                        mode = Mode.OPERATION;
                    }
                break;
                case INDEX: //read index acess
                    if(bracketlevel == 0){
                        if(current.matches("\\d+")) ret = ret.get(Integer.parseInt(current));
                        else if(current.contains(":")){
                            Val filteredval = new Val();
                            for (int i = 0; i < ret.value.size(); i++)
                                if(interpret(current, new Val(i)).interpretInt() == 1) filteredval.value.add(ret.value.get(i).clone());
                            ret = filteredval;
                        }else ret = ret.get(interpret(current, context).interpretInt());
                        current = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    current += c;
                break;
                case OPERATION://read operation
                    if(c == '{' || c == '\'' || c == '(' || c == ':'){
                        mode = Mode.INPUT2;
                        operation = current;
                        current = "";
                    }
                    current += c;
                break;
                case INPUT2://read second input and execute operation
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
                case PARENTHESIS://end paren
                    if(bracketlevel == 0){
                        ret = interpret(current, context);
                        current = "";
                        mode = Mode.MODIFIER;
                    }
                    current += c;
                break;
                default: System.err.println("Something very wrong happened");
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
}