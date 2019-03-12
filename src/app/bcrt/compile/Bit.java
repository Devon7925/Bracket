package app.bcrt.compile;

class Bit extends Val {

    boolean bit;

    Bit(boolean b) {
        this.bit = b;
    }

    Bit(Val v) {
        set(v);
    }

    int interpretInt() {
        return bit ? 1 : 0;
    }

    public void set(Val newval) {
        if(newval instanceof Bit) {
            bit = ((Bit) newval).bit;
            return;
        }
        this.bit = ((Bit) newval.value.get(0)).bit;
        super.set(newval);
    }

    public String toString() {
        return bit ? "T" : "F";
    }

    protected Bit clone() {
        return new Bit(this);
    }
}