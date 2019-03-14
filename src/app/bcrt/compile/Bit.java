package app.bcrt.compile;

class Bit extends Val {

    boolean bit;

    Bit(boolean bit) {
        super();
        this.bit = bit;
    }

    Bit(Val v) {
        super();
        set(v);
    }

    Bit(Bit b) {
        this((Val) b);
        bit = b.bit;
    }

    int asInt() {
        return bit ? 1 : 0;
    }

    public void set(Val newval) {
        bit = getBit(newval);
        super.set(newval);
    }

    public static boolean getBit(Val val){
        return (val instanceof Bit) ? ((Bit) val).bit : getBit(val.get(0));
    }

    public String toString() {
        return bit ? "T" : "F";
    }

    protected Bit clone() {
        return new Bit(this);
    }
}