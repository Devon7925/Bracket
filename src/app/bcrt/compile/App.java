package app.bcrt.compile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class App {

    public static final ArrayList<Var> vars = new ArrayList<>(Arrays.asList(new Var("execute", new Execute())));

    public static int debugLevel = 0;

    public static void main(String[] args) throws IOException {
        interpretArgs(args);
        Val root = new Val();
        Loader.loadFile(args[args.length - 1], root).execute(root);
        if(debugLevel >= 1) vars.forEach(System.out::println);
    }

    public static void interpretArgs(String[] args) {
        for(int i = 0; i < args.length - 1; i++)
            if(args[i].equals("-d")) {
                String debug = args[++i];
                if(debug.matches("\\d+")) debugLevel = Integer.parseInt(debug);
                else throw new IllegalArgumentException("Debug level must be integer");
            }
    }

    static Val execute(String s, Val context) {
        s = s.trim() + ";";
        if(debugLevel >= 2) System.out.println(s);
        Mode mode = Mode.START;
        String current = "";
        Val tempval = null;
        int bracketlevel = 0;
        for(char c : s.toCharArray()) {
            switch(mode) {
                case START:
                    if(c == '{') bracketlevel++;
                    if(c == '}') bracketlevel--;
                    if(bracketlevel == 0) {
                        if(c == '@') {
                            tempval = context.interpret(current);
                            current = "";
                            mode = Mode.ASSIGN;
                            break;
                        } else if(c == ';') {
                            tempval = context.interpret(current);
                            if(tempval != null) {
                                Val ret = tempval.execute(tempval instanceof Var ? tempval : context);
                                if(ret != null) return ret;
                            }
                        } else if(c == '~') {
                            current = "";
                            mode = Mode.RETURN;
                            break;
                        }
                    }
                    break;
                case ASSIGN:
                    if(c == ';') {
                        tempval.set(context.interpret(current));
                        if(tempval instanceof Var && ((Var) tempval).holder == null) setVar((Var) tempval);
                        return null;
                    }
                    break;
                case RETURN:
                    if(c == ';') return new Val(context.interpret(current));
                    break;
                default:
                    System.err.println("Something very wrong happened");
            }
            current += c;
        }
        return null;
    }

    public static Val get(String name) {
        return vars.stream().filter(n -> n.name.equals(name)).findAny().orElse(null);
    }

    public static void setVar(Var v) {
        int index = vars.indexOf(get(v.name));
        if(index == -1) vars.add(v); // if it does not already exist, add it
        else vars.set(index, v); // otherwise set it
    }
}