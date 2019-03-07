import app.bcrt.compile.*;

public class Print extends Val {
    public Val execute(Val context){
        System.out.println(new Val(App.get("b")));
        App.setVar(new Var("b"));
        return App.get("b");
    }

    protected Val clone(){
        return new Print();
    }
}