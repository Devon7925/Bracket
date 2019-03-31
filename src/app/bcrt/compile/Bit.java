package app.bcrt.compile;

class Bit extends Val {

    boolean bit;

    Bit(Val holder) {
        this(holder, false);
    }

    Bit(Val holder, boolean bit) {
        super(holder);
        this.bit = bit;
    }

    Bit(Val holder, Val v) {
        super(holder);
        set(v);
    }

    @Override
    int asInt() {
        return bit ? 1 : 0;
    }

    public void set(Val newval) {
        bit = getBit(newval);
        super.set(newval);
    }

    public static boolean getBit(Val val) {
        return (val instanceof Bit) ? ((Bit) val).bit : getBit(val.get(0));
    }

    public String toString() {
        return bit ? "T" : "F";
    }

    protected Bit clone() {
        Bit clone = new Bit(holder, bit);
        clone.set(this);
        return clone;
    }
}