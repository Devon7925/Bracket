import app.bcrt.compile.App;
import app.bcrt.compile.StringTool;
import app.bcrt.compile.Val;
import app.bcrt.compile.Var;

public class Remove extends Val {
    public Val execute(Val context){
        App.vars.removeIf(n -> n.name.equals(App.get(new Val("b").toString()).interpretString()));
        return App.get(new Val("b").toString());
    }

    protected Val clone(){
        return new Remove();
    }
}