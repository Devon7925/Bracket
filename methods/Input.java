import app.bcrt.compile.*;
import javax.swing.JOptionPane;

public class Input extends Var {
    public Input(){
        super("input");
    }

    public Val execute(Val context){
        Var b = new Var("b");
        String input = JOptionPane.showInputDialog(null, App.get("b").interpretString(), "Input", JOptionPane.INFORMATION_MESSAGE);
        if(input.matches("\\d+")) b.set(Integer.parseInt(input));
        else b.set(input);
        App.setVar(b);
        return App.get("b");
    }

    protected Var clone(){
        return new Input();
    }
}