package app.bcrt.compile;

import java.util.Optional;

class Bit extends Value {

    boolean bit;

    Bit(Val holder) {
        this(holder, false);
    }

    Bit(Val holder, boolean bit) {
        super(holder);
        this.bit = bit;
    }

    @Override
    public int asInt() {
        return bit ? 1 : 0;
    }

    public String toString() {
        return bit ? "T" : "F";
    }

    public Bit clone() {
        return new Bit(holder, bit);
    }

    @Override
    public Optional<Value> execute() {
        return Optional.empty();
    }
}