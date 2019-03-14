import app.bcrt.compile.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Flatten extends Val {
    @Override
    public Val execute(Val context) {
        Val result = App.get(AppTool.litToVal("b"));
        int numberOfElems = result.value.stream().collect(Collectors.summingInt(n -> n.value.size()));
        List<Val> flatenedvalue = new ArrayList<Val>(numberOfElems);
        for(Val level1 : result.value)
            level1.value.forEach(flatenedvalue::add);
        result.value = flatenedvalue;
        return result;
    }

    protected Val clone() {
        return new Flatten();
    }
}