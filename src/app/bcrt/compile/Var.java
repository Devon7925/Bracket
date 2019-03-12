package app.bcrt.compile;

public class Var extends Val {

    public String name;

    public Var(String name) {
        this.name = name;
    }

    public Var(Var v) {
        super(v);
        this.name = v.name;
    }

    public Var(String name, Val val) {
        this(name);
        value.add(val);
    }

    public String toString() {
        return name + " - " + super.toString();
    }

    protected Var clone() {
        return new Var(this);
    }
}