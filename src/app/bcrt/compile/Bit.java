package app.bcrt.compile;

import java.util.ArrayList;

class Bit extends Val {

    boolean b;

    Bit(boolean b){
        this.b = b;
    }

    Bit(Val v){
        set(v);
    }

    int interpretInt(){
        return b?1:0;
    }

    public void set(Val newval){
        if(newval instanceof Bit) {
            b = ((Bit) newval).b;
            return;
        }
        this.b = ((Bit) newval.value.get(0)).b;
        this.subelems = new ArrayList<>(newval.subelems.size());
        newval.subelems.stream().map(n->n.clone()).forEach(subelems::add);
    }

    public String toString(){
        return b?"T":"F";
    }

    protected Bit clone(){
        return new Bit(this);
    }
}