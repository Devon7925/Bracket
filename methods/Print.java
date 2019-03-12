import app.bcrt.compile.*;

public class Print extends Val {
    public Val execute(Val context){
        System.out.println(new Val(App.get(new Val("b").toString())));
        App.setVar(new Var(new Val("b").toString()));
        return App.get(new Val("b").toString());
    }

    protected Val clone(){
        return new Print();
    }
}