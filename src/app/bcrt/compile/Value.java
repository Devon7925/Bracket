package app.bcrt.compile;

import java.util.Optional;

/**
 * Value
 */
public abstract class Value extends AppTool {
    public Val holder;

    public Value(Val holder){
    	this.holder = holder;
    }

    public abstract int asInt();
    public abstract Value clone();
    public abstract Optional<Value> execute();
}