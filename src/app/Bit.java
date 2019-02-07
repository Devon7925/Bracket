package app;

import java.util.ArrayList;

class Bit extends Val {

    boolean b;

    Bit(boolean b){
        this.b = b;
    }

    Bit(Val v){
        set(v);
    }

    int toInt(){
        return b?1:0;
    }

    public void set(Val newval){
        if(newval instanceof Bit) {
            b = ((Bit) newval).b;
            return;
        }
        this.b = ((Bit) newval.vals.get(0)).b;
        this.subelems = new ArrayList<>(newval.subelems.size());
        newval.subelems.stream().map(n->n.clone()).forEach(subelems::add);
    }

    public String toString(){
        return b?"T":"F";
    }

    void print(){
        System.out.print(toString()+",");
    }

    protected Bit clone(){
        return new Bit(this);
    }
}