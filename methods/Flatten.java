import app.bcrt.compile.*;
import java.util.ArrayList;


public class Flatten extends Val {
    @Override
    public Val execute(Val context){
        Val ret = App.get("b");
        int numvalue = 0;
        for(int i = 0; i < ret.value.size(); i++){
            numvalue += ret.value.get(i).value.size();
        }
        ArrayList<Val> flatvalue = new ArrayList<Val>(numvalue);
        for(int i = 0; i < ret.value.size(); i++){
            for(int j = 0; j < ret.value.get(i).value.size(); j++){
                flatvalue.add(ret.value.get(i).value.get(j));
            }
        }
        ret.value = flatvalue;
        return ret;
    }

    protected Val clone(){
        return new Flatten();
    }
}