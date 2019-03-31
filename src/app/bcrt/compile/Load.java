package app.bcrt.compile;

public class Load extends Val {
    public Load(Val holder) {
        super(holder, litToVal("execute"));
    }

    public Val execute(Val context) {
        String path = get(AppTool.litToVal("b")).asString();
        Val loadedValue = Loader.loadFile(path, context);
        Val result = new Val(context);
        result.value.add(loadedValue);
        result.holder = context;
        if(loadedValue != null) get(AppTool.litToVal("b")).set(result);
        return result;
    }

    protected Load clone() {
        return new Load(holder);
    }
}