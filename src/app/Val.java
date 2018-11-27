package app;

class Val {
    Val[] vals;
    public Val(){}
    public Val(String newval){
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
    public Val(boolean newval){
        vals = new Val[1];
        vals[0] = new Bit(newval);
    }
    public Val(int newval){
        vals = new Val[(int) Math.ceil(Math.log(newval)/Math.log(2))];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = new Bit((newval >> i)%2 == 1);
        }
    }
    public Val(Val newval){
        this.vals = newval.vals;
    }
    public void set(boolean newval){
        vals = new Val[1];
        vals[0] = new Bit(newval);
    }
    public void set(int newval){
        vals = new Val[(int) Math.ceil(Math.log(newval)/Math.log(2))];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = new Bit((newval >> i)%2 == 1);
        }
    }
    public void set(String newval){
        vals = new Val[8*newval.length()];
        for (int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            vals[8*i+0] = new Bit((charval>>0)%2 == 1);
            vals[8*i+1] = new Bit((charval>>1)%2 == 1);
            vals[8*i+2] = new Bit((charval>>2)%2 == 1);
            vals[8*i+3] = new Bit((charval>>3)%2 == 1);
            vals[8*i+4] = new Bit((charval>>4)%2 == 1);
            vals[8*i+5] = new Bit((charval>>5)%2 == 1);
            vals[8*i+6] = new Bit((charval>>6)%2 == 1);
            vals[8*i+7] = new Bit((charval>>7)%2 == 1);
        }
    }
    public void set(Val newval){
        this.vals = newval.vals;
    }
    Val get(){
        return this;
    };
    int toInt(){
        int ret = 0;
        for (int i = 0; i < vals.length; i++) {
            ret += vals[i].toInt() << i;
        }
        return ret;
    }
    void print(){
        for(Val v : vals){
            v.print();
        }
        System.out.print(",");
    }
}