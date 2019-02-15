package app.bcrt.compile;

import java.util.ArrayList;

public class Val implements Cloneable{
    
    public ArrayList<Val> value;
    ArrayList<Var> subelems = new ArrayList<>(0);

    public Val(){
        set();
    }

    public Val(String newval){
        if(newval.equals("")) set();
        else if(newval.matches("\\d+")) set(Integer.parseInt(newval));
        else set(newval);
    }

    public Val(int newval){
        set(newval);
    }

    public Val(Val newval){
        set(newval);
    }

    public void set(){
        value = new ArrayList<>(0);
    }

    public void set(String newval){
        value = new ArrayList<>(8*newval.length());
        for (int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            for(int j = 0; j < 8; j++)
                value.add(new Bit((charval>>j)%2 == 1));
        }
    }

    public void set(int newval){
        if(newval == 0){
            value = new ArrayList<Val>(1);
            value.add(new Bit(false));
            return;
        }
        int digits = (int) Math.floor(Math.log(newval)/Math.log(2))+1;
        value = new ArrayList<>(digits);
        for (int i = 0; i < digits; i++)
            value.add(new Bit((newval >> i)%2 == 1));
    }

    public void set(Bit newval){
        value = new ArrayList<Val>(1);
        value.add(new Bit(newval));
    }

    public void set(Val newval){
        if(newval instanceof Bit) {
            set((Bit) newval);
            return;
        }
        this.value = new ArrayList<>(newval.value.size());
        newval.value.stream().map(n->n.clone()).forEach(value::add);
        this.subelems = new ArrayList<>(newval.subelems.size());
        newval.subelems.stream().map(n->n.clone()).forEach(subelems::add);
    }

    Val get(int index){
        if(index < value.size())
            return value.get(index);
        else {
            Val oobelem = new Bit(false);
            if(index == value.size()) value.add(oobelem);
            return oobelem;
        }
    };

    int interpretInt(){
        int ret = 0;
        for (int i = 0; i < value.size(); i++) ret += value.get(i).interpretInt() << i;
        return ret;
    }

    public String interpretString(){
        String ret = "";
        for (int i = 0; i < value.size(); i+=8) {
            int ch = 0;
            for(int j = 0; j < 8; j++)
                ch += value.get(i+j).interpretInt() << j;
            ret += (char) ch;
        }
        return ret;
    }

    public String toString(){
        String ret = "{";
        if(value.size() > 0 && value.get(0) instanceof Bit){
            if(value.size() >= 24 && value.size()%8 == 0){
                ret += interpretString();
            }else if(value.size() > 2){
                ret += interpretInt();
            }
        }
        if(ret.length() == 1) for(Val v : value) ret += v.toString()+",";
        return ret + "},";
    }

    public Val execute(Val context){
        Val ret = null;
        if(value.size() == 0 || value.get(0) instanceof Bit) return App.execute(interpretString(), context);
        else for(Val v1 : value) if(v1.value.get(0) instanceof Bit) {
            Val toret = v1.execute(context);
            if(toret != null) ret = toret;
        }
        return ret;
    }

    protected Val clone(){
        return new Val(this);
    }
}