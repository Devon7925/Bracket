package app;

class Remove extends Var {
    public Remove(){
        super("remove");
    }

    Val execute(Val context){
        App.vars.removeIf(n -> n.name.equals(StringTool.toString(App.interpret("'b'", context))));
        return App.interpret("'b'", context);
    }

    protected Var clone(){
        return new Remove();
    }
}