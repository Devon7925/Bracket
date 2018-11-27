package app;

class Bit extends Val {
    boolean b;
    Bit(boolean b){
        this.b = b;
    }
    int toInt(){
        return b?1:0;
    }
    void print(){
        System.out.print((b?"T":"F")+",");
    }
}