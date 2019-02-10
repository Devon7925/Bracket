package app;

import java.util.ArrayList;

class Val implements Cloneable{
    
    ArrayList<Val> vals;
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
        vals = new ArrayList<>(0);
    }

    public void set(String newval){
        vals = new ArrayList<>(8*newval.length());
        for (int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            for(int j = 0; j < 8; j++)
                vals.add(new Bit((charval>>j)%2 == 1));
        }
    }

    public void set(int newval){
        if(newval == 0){
            vals = new ArrayList<Val>(1);
            vals.add(new Bit(false));
            return;
        }
        int digits = (int) Math.floor(Math.log(newval)/Math.log(2))+1;
        vals = new ArrayList<>(digits);
        for (int i = 0; i < digits; i++)
            vals.add(new Bit((newval >> i)%2 == 1));
    }

    public void set(Bit newval){
        vals = new ArrayList<Val>(1);
        vals.add(new Bit(newval));
    }

    public void set(Val newval){
        if(newval instanceof Bit) {
            set((Bit) newval);
            return;
        }
        this.vals = new ArrayList<>(newval.vals.size());
        newval.vals.stream().map(n->n.clone()).forEach(vals::add);
        this.subelems = new ArrayList<>(newval.subelems.size());
        newval.subelems.stream().map(n->n.clone()).forEach(subelems::add);
    }

    Val get(int index){
        if(index < vals.size())
            return vals.get(index);
        else {
            Val oobelem = new Bit(false);
            if(index == vals.size()) vals.add(oobelem);
            return oobelem;
        }
    };

    int toInt(){
        int ret = 0;
        for (int i = 0; i < vals.size(); i++) ret += vals.get(i).toInt() << i;
        return ret;
    }

    public String toString(){
        String ret = "{";
        if(vals.size() > 0 && vals.get(0) instanceof Bit){
            if(vals.size() >= 24 && vals.size()%8 == 0){
                ret += StringTool.toString(this);
            }else if(vals.size() > 2){
                ret += toInt();
            }
        }
        if(ret.length() == 1) for(Val v : vals) ret += v.toString()+",";
        return ret + "},";
    }

    protected Val clone(){
        return new Val(this);
    }
}