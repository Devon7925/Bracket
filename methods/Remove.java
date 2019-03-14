import app.bcrt.compile.*;

public class Remove extends Val {
    public Val execute(Val context){
        App.vars.removeIf(n -> n.name.equals(App.get(AppTool.litToVal("b")).asString()));
        return App.get(AppTool.litToVal("b"));
    }

    protected Val clone(){
        return new Remove();
    }
}