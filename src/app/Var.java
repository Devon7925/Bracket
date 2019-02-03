package app;

class Var extends Val {

    String name;
    Var holder = null;

    public Var(String name){
        this.name = name;
    }

    void print(){
        System.out.print('\n'+name+" - ");
        super.print();
    }

    public Var get(String s){
        Var newvar = subelem.stream().filter(n -> n.name.equals(s)).findAny().orElse(null);
        if(newvar == null){
            newvar = new Var(s);
            subelem.add(newvar);
        }
        newvar.holder = this;
        return newvar;
    }
}