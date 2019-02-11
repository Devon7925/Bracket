package app;

class Execute extends Var {
    public Execute(){
        super("execute");
    }

    Val execute(Val context){
        App.executeFile(StringTool.toString(App.interpret("'b'", context)));
        return App.interpret("'b'", context);
    }

    protected Var clone(){
        return new Execute();
    }
}