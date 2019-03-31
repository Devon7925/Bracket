import app.bcrt.compile.*;
import java.util.Optional;

public class Print extends Val {
    public Print(Val holder) {
        super(holder);
    }

    @Override
    public Optional<Val> execute(){
        System.out.println(new Val(get(litToVal("b"))));
        return Optional.of(get(litToVal("b")));
    }

    protected Val clone(){
        return new Print(holder);
    }
}