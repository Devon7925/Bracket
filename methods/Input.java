import app.bcrt.compile.*;
import javax.swing.JOptionPane;

public class Input extends Val {
    public Val execute(Val context){
        Var result = new Var(AppTool.litToVal("b"));
        String input = JOptionPane.showInputDialog(null, App.get(AppTool.litToVal("b")).asString(), "Input", JOptionPane.INFORMATION_MESSAGE);
        if(input.matches("\\d+")) result.set(Integer.parseInt(input));
        else result.set(input);
        App.setVar(result);
        return App.get(AppTool.litToVal("b"));
    }

    protected Val clone(){
        return new Input();
    }
}