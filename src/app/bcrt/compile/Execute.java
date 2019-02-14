package app.bcrt.compile;

public class Execute extends Var {
    public Execute(){
        super("execute");
    }

    public Val execute(Val context){
        App.executeFile(App.get("b").interpretString());
        return App.get("b");
    }

    protected Var clone(){
        return new Execute();
    }
}