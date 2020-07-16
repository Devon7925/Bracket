import app.bcrt.compile.*;
import java.util.Optional;

public class Print extends Value {
    public Print(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Value> execute(){
        System.out.println(get(litToVal("b")));
        return Optional.of(get(litToVal("b")));
    }

    public Value clone(){
        return new Print(holder);
    }
}