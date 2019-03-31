import app.bcrt.compile.*;
import java.util.Optional;

public class Remove extends Val {
    public Remove(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Val> execute() {
        get(litToVal("b")).holder.subelems.remove(get(litToVal("b")).toString());
        return Optional.of(get(litToVal("b")));
    }

    protected Val clone() {
        return new Remove(holder);
    }
}