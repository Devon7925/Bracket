package app;

class Var extends Val{
    String name;
    public Var(String name){
        super(false);
        this.name = name;
    }
    public void metaSet(String newval){
        if(newval.equals("1")){
            set(true);
        }else if(newval.equals("0")){
            set(false);
        }else if(newval.matches("\\d+")){
            set(Integer.parseInt(newval));
        }else if(newval.contains(",")){
            String[] str = newval.split(",");
            vals = new Val[str.length];
            for(int i = 0; i < vals.length; i++){
                vals[i] = new Val(str[i]);
            }
        }else{
            set(newval);
        }
    }
}