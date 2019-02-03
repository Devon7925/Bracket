package app;

import java.util.ArrayList;

class Bit extends Val {

    boolean b;

    Bit(boolean b){
        this.b = b;
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
        this.subelem = new ArrayList<>(newval.subelem);
    }

    public String toString(){
        return (b?"T":"F")+",";
    }

    void print(){
        System.out.print(toString());
    }
}