import app.bcrt.compile.*;

public class Print extends Val {
    public Val execute(Val context){
        System.out.println(new Val(App.get(AppTool.litToVal("b"))));
        App.setVar(new Var(AppTool.litToVal("b")));
        return App.get(AppTool.litToVal("b"));
    }

    protected Val clone(){
        return new Print();
    }
}