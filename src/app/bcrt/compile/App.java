package app.bcrt.compile;

import java.io.IOException;
import java.util.Arrays;

public class App extends AppTool {

    public final Val root;
    public static int debugLevel = 0;

    public App(Val v) {
        root = v;
        root.put(litToVal("load"), new Load(root));
        root.execute();
        if(debugLevel >= 1) root.print();
    }

    public static void main(String[] args) throws IOException {
        Val root = new Val(null, Arrays.asList(Loader.loadBcrtMethod(interpretArgs(args))));
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