package app.bcrt.compile;

import app.bcrt.compile.App;
import app.bcrt.compile.StringTool;
import app.bcrt.compile.Val;
import app.bcrt.compile.Var;

public class Execute extends Var {
    public Execute(){
        super("execute");
    }

    public Val execute(Val context){
        App.executeFile(StringTool.toString(App.get("b")));
        return App.get("b");
    }

    protected Var clone(){
        return new Execute();
    }
}