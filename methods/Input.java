import app.bcrt.compile.*;
import javax.swing.JOptionPane;

public class Input extends Val {
    public Val execute(Val context){
        Var result = new Var(new Val("b").toString());
        String input = JOptionPane.showInputDialog(null, App.get(new Val("b").toString()).interpretString(), "Input", JOptionPane.INFORMATION_MESSAGE);
        if(input.matches("\\d+")) result.set(Integer.parseInt(input));
        else result.set(input);
        App.setVar(result);
        return App.get(new Val("b").toString());
    }

    protected Val clone(){
        return new Input();
    }
}