package app.bcrt.compile;

public class Execute extends Val {
    public Val execute(Val context){
        Val ret = App.executeFile(App.get("b").interpretString(), context);
        Val trueret = new Val();
        trueret.value.add(ret);
        if(ret != null) App.get("b").set(trueret);
        return trueret;
    }

    protected Val clone(){
        return new Execute();
    }
}