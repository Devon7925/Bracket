import app.bcrt.compile.*;
import javax.swing.JOptionPane;
import java.util.Optional;

public class Input extends Value {
    public Input(Val holder) {
        super(holder);
    }

    public Optional<Value> execute(){
        Val result;
        String input = JOptionPane.showInputDialog(null, get(litToVal("b")).asString(), "Input", JOptionPane.INFORMATION_MESSAGE);
        if(isNumeric(input)) result = new Val(Integer.parseInt(input));
        else result = new Val(input);
        subAssign(litToVal("b"), result);
        return Optional.of(get(litToVal("b")));
    }

    public Value clone(){
        return new Input(holder);
    }
}