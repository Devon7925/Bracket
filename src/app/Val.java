package app;

class Val {
    Val[] vals;
    public Val(){}
    public Val(String newval){
        if(newval.equals("0")) set(false);
        else if(newval.matches("\\d+")) set(Integer.parseInt(newval));
        else set(newval);
    }
    public Val(boolean newval){
        vals = new Val[1];
        vals[0] = new Bit(newval);
    }
    public Val(int newval){
        vals = new Val[(int) Math.floor(Math.log(newval)/Math.log(2))+1];
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
        vals = new Val[(int) Math.ceil(Math.log(newval)/Math.log(2))+1];
        for (int i = 0; i < vals.length; i++) 
            vals[i] = new Bit((newval >> i)%2 == 1);
    }
    public void set(String newval){
        vals = new Val[8*newval.length()];
        for (int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            for(int j = 0; j < 8; j++)
                vals[8*i+j] = new Bit((charval>>j)%2 == 1);
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
        for (int i = 0; i < vals.length; i++) ret += vals[i].toInt() << i;
        return ret;
    }
    void print(){
        if(vals != null) for(Val v : vals) v.print();
        System.out.print(",");
    }
}