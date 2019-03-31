import app.bcrt.compile.*;
import javax.swing.JOptionPane;
import java.util.Optional;

public class Input extends Val {
    public Input(Val holder) {
        super(holder);
    }

    public Optional<Val> execute(){
        Val result = new Val(this);
        String input = JOptionPane.showInputDialog(null, get(litToVal("b")).asString(), "Input", JOptionPane.INFORMATION_MESSAGE);
        if(isNumeric(input)) result.set(Integer.parseInt(input));
        else result.set(input);
        subAssign(litToVal("b"), result);
        return Optional.of(get(litToVal("b")));
    }

    protected Val clone(){
        return new Input(holder);
    }
}