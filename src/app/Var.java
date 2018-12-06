package app;

class Var extends Val{
    String name;
    public Var(String name){
        this.name = name;
    }
    void print(){
        System.out.print('\n'+name+" - ");
        super.print();
    }
}