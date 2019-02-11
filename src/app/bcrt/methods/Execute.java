package app.bcrt.methods;

import app.bcrt.compile.App;
import app.bcrt.compile.StringTool;
import app.bcrt.compile.Val;
import app.bcrt.compile.Var;

public class Execute extends Var {
    public Execute(){
        super("execute");
    }

    public Val execute(Val context){
        App.executeFile(StringTool.toString(App.interpret("'b'", context)));
        return App.interpret("'b'", context);
    }

    protected Var clone(){
        return new Execute();
    }
}