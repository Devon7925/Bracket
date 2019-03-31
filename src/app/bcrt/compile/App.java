package app.bcrt.compile;

import java.io.IOException;
import java.util.Arrays;

public class App extends AppTool {

    public final Val root;
    public static int debugLevel = 0;

    public App(Val v) {
        root = v;
        root.subAssign(litToVal("load"), new Load(root));
        root.execute();
        if(debugLevel >= 1) root.subelems.entrySet().stream().map(n -> n.getKey() + " - " + n.getValue().toString()).forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {
        Val root = new Val(null);
        root.set(Arrays.asList(Loader.loadFile(interpretArgs(args), root)));
        new App(root);
    }

    public static String interpretArgs(String[] args) {
        for(int i = 0; i < args.length - 1; i++)
            if(args[i].equals("-d")) {
                String debug = args[++i];
                if(isNumeric(debug)) debugLevel = Integer.parseInt(debug);
                else throw new IllegalArgumentException("Debug level must be integer");
            }
        return args[args.length - 1];
    }
}