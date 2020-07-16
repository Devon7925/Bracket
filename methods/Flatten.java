import app.bcrt.compile.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class Flatten extends Value {
    public Flatten(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Value> execute() {
        Val result = get(litToVal("b"));
        int numberOfElems = result.stream().collect(Collectors.summingInt(n -> n.size()));
        List<Value> flatenedvalue = new ArrayList<Val>(numberOfElems);
        for(Value level1 : result)
            if(level1 instanceof Val) ((Val) level1).forEach(flatenedvalue::add);
        Val res = new Val(holder, flatenedvalue);
        holder.set(litToVal("b"), res);
        return Optional.of(res);
    }

    public Value clone() {
        return new Flatten(holder);
    }
}