package app.bcrt.methods;

import app.bcrt.compile.*;

public class Print extends Var {
    public Print(){
        super("print");
    }

    public Val execute(Val context){
        System.out.println(new Val(App.interpret("'b'", context)));
        return App.interpret("'b'", context);
    }

    protected Var clone(){
        return new Print();
    }
}