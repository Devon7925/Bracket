package app.bcrt.compile;

public class Load extends Val {
    public Val execute(Val context) {
        String path = get(new Val("b").toString()).interpretString();
        Val loadedValue = Loader.loadFile(path, context);
        Var result = new Var(path);
        result.value.add(loadedValue);
        result.holder = context.getVarHolder();
        if(loadedValue != null) get(new Val("b").toString()).set(result);
        return result;
    }

    protected Val clone() {
        return new Load();
    }
}