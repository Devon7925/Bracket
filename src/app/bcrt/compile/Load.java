package app.bcrt.compile;

import java.util.Optional;

public class Load extends Value {
    public Load(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Value> execute() {
        String path = ((Val) holder.get(AppTool.litToVal("b"))).asString();
        Value loadedValue = Loader.loadFile(path, holder);
        if(loadedValue != null) holder.set(AppTool.litToVal("b"), loadedValue);
        return Optional.of(loadedValue);
    }

    @Override
    public Load clone() {
        return new Load(holder);
    }

    @Override
    public int asInt() {
        return 0;
    }
}