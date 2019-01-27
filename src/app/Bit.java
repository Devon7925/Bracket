package app;

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
    }
    public String toString(){
        return (b?"T":"F")+",";
    }
    void print(){
        System.out.print(toString());
    }
}