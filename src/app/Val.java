package app;

import java.util.ArrayList;

class Val {
    
    ArrayList<Val> vals;
    ArrayList<Var> subelem = new ArrayList<>(0);

    public Val(){}

    public Val(String newval){
        if(newval.equals("0")) set(false);
        else if(newval.equals("")) this.vals = new ArrayList<Val>(0);
        else if(newval.matches("\\d+")) set(Integer.parseInt(newval));
        else set(newval);
    }

    public Val(boolean newval){
        set(newval);
    }

    public Val(int newval){
        set(newval);
    }

    public Val(Val newval){
        this.vals = newval.vals;
        if(newval instanceof Bit) set(((Bit) newval).b);
    }

    public void set(boolean newval){
        vals = new ArrayList<Val>(1);
        vals.add(new Bit(newval));
    }

    public void set(int newval){
        int digits = (int) Math.floor(Math.log(newval)/Math.log(2))+1;
        vals = new ArrayList<Val>(digits);
        for (int i = 0; i < digits; i++)
            vals.add(new Bit((newval >> i)%2 == 1));
    }

    public void set(String newval){
        vals = new ArrayList<Val>(8*newval.length());
        for (int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            for(int j = 0; j < 8; j++)
                vals.add(new Bit((charval>>j)%2 == 1));
        }
    }

    public void set(Val newval){
        if(newval instanceof Bit) {
            set((Bit) newval);
            return;
        }
        this.vals = new ArrayList<>(newval.vals);
        this.subelem = new ArrayList<>(newval.subelem);
    }

    public void set(Bit newval){
        vals = new ArrayList<Val>(1);
        vals.add(newval);
    }

    Val get(){
        return this;
    };

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
        String ret = "";
        if(vals != null) for(Val v : vals) ret += v.toString();
        ret +=",";
        return ret;
    }

    void print(){
        if(vals != null) for(Val v : vals) v.print();
        System.out.print(",");
    }
}