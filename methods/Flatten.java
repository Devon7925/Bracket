package methods;

import app.bcrt.compile.*;
import java.util.ArrayList;


public class Flatten extends Var {
    public Flatten(){
        super("flatten");
    }

    public Val execute(Val context){
        Val ret = App.get("b");
        int numvals = 0;
        for(int i = 0; i < ret.vals.size(); i++){
            numvals += ret.vals.get(i).vals.size();
        }
        ArrayList<Val> flatvals = new ArrayList<Val>(numvals);
        for(int i = 0; i < ret.vals.size(); i++){
            for(int j = 0; j < ret.vals.get(i).vals.size(); j++){
                flatvals.add(ret.vals.get(i).vals.get(j));
            }
        }
        ret.vals = flatvals;
        return ret;
    }

    protected Var clone(){
        return new Flatten();
    }
}