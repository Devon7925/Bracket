import app.bcrt.compile.*;
import java.util.Optional;

public class Remove extends Value {
    public Remove(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Value> execute() {
        get(litToVal("b")).holder.remove(get(litToVal("b")).toString());
        return Optional.of(get(litToVal("b")));
    }

    public Val clone() {
        return new Remove(holder);
    }
}