import app.bcrt.compile.App;
import app.bcrt.compile.StringTool;
import app.bcrt.compile.Val;
import app.bcrt.compile.Var;

public class Remove extends Var {
    public Remove(){
        super("remove");
    }

    public Val execute(Val context){
        App.vars.removeIf(n -> n.name.equals(App.get("b").interpretString()));
        return App.get("b");
    }

    protected Var clone(){
        return new Remove();
    }
}