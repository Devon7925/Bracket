package app.bcrt.compile;

import java.util.Optional;

public class Load extends Val {
    public Load(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Val> execute() {
        String path = get(AppTool.litToVal("b")).asString();
        Val result = new Val(this);
        Val loadedValue = Loader.loadFile(path, result);
        result.value.add(loadedValue);
        if(loadedValue != null) get(AppTool.litToVal("b")).set(result);
        return Optional.of(result);
    }

    protected Load clone() {
        return new Load(holder);
    }
}