import app.bcrt.compile.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class Flatten extends Val {
    public Flatten(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Val> execute() {
        Val result = get(litToVal("b"));
        int numberOfElems = result.value.stream().collect(Collectors.summingInt(n -> n.value.size()));
        List<Val> flatenedvalue = new ArrayList<Val>(numberOfElems);
        for(Val level1 : result.value)
            level1.value.forEach(flatenedvalue::add);
        result.value = flatenedvalue;
        return Optional.of(result);
    }

    protected Val clone() {
        return new Flatten(holder);
    }
}