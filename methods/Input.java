package methods;

import app.bcrt.compile.*;

public class Input extends Var {
    public Input(){
        super("input");
    }

    public Val execute(Val context){
        System.out.println(new Val(App.get("b")));
        Var b = new Var("b");
        b.set(";");//TODO
        App.setVar(b);
        return App.get("b");
    }

    protected Var clone(){
        return new Input();
    }
}