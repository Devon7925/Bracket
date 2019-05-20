package app.bcrt.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Val extends Value implements Cloneable, Iterable<Value> {

    protected List<Value> value;
    protected Map<String, Value> subelems;

    public Val(Val holder) {
        super(holder);
        value = new ArrayList<>(0);
        subelems = new HashMap<>(0);
    }

    public Val(Val holder, String newval) {
        this(holder);
        if(isNumeric(newval)) set(Integer.parseInt(newval));
        else if(!newval.isEmpty()) set(newval);
    }

    public Val(Val holder, int newval, int minsize) {
        this(holder);
        set(newval, minsize);
    }

    public Val(Val holder, int newval) {
        this(holder);
        set(newval);
    }

    public Val(Val holder, List<Value> value) {
        this(holder);
        set(value);
    }

    private void set(List<Value> value) {
        value.forEach(n -> n.holder = this);
        this.value = value;
    }

    private void set(String newval) {
        value = new ArrayList<>(newval.length());
        for(char c : newval.toCharArray())
            value.add(new Val(this, c, 8));
    }

    private void set(int newval) {
        set(newval, (int) Math.floor(log(2, newval)) + 1);
    }

    private void set(int newval, int minsize) {
        if(newval == 0) {
            value = Arrays.asList(new Bit(this));
            return;
        }
        value = new ArrayList<>(minsize);
        for(int digit = 0; digit < minsize; digit++)
            value.add(new Bit(this, (newval >> digit) % 2 == 1));
    }

    Value get(int index) {
        if(index == value.size()) value.add(new Bit(this));
        return (index < value.size()) ? value.get(index) : new Bit(this);
    };

    public Value get(String name) {
        Value newvar = subelems.get(name);
        if(newvar != null) return newvar;

        Value extv = (holder == null) ? null : holder.get(name);

        if(extv != null) return extv;

        Val v = new Val(this);
        subelems.put(name, v);
        return v;
    }

    public void set(String name, Value newval) {
        if(subelems.containsKey(name)) {
            put(name, newval);
            return;
        }

        if(holder != null) {
            holder.set(name, newval);
            return;
        }

        subelems.put(name, newval);
    }

    public void set(Value oldval, Value newval) {
        top().setFromTop(oldval, newval);
    }

    private boolean setFromTop(Value oldval, Value newval) {
        for(Map.Entry<String, Value> e : subelems.entrySet()) {
            if(e.getValue().equals(oldval)) {
                put(e.getKey(), newval);
                return true;
            }
            if(e.getValue() instanceof Val && ((Val) e.getValue()).setFromTop(oldval, newval)) return true;
        }
        for(int i = 0; i < value.size(); i++) {
            if(value.get(i).equals(oldval)) {
                put(i, newval);
                return true;
            }
            if(value.get(i) instanceof Val && ((Val) value.get(i)).setFromTop(oldval, newval)) return true;
        }
        return false;
    }

    private void put(int i, Value newval) {
        newval.holder = this;
        value.set(i, newval);
    }

    public void put(String name, Value newval) {
        newval.holder = this;
        subelems.put(name, newval);
    }

    public Value getLocal(String name) {
        Value extv = subelems.get(name);
        if(extv != null) return extv;
        Val v = new Val(this);
        put(name, v);
        return v;
    }

    public int asInt() {
        int result = 0;
        for(int i = 0; i < value.size(); i++)
            result += value.get(i).asInt() << i;
        return result;
    }

    public String asString() {
        return value.stream().map(n -> (char) n.asInt()).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public String toString() {
        if(value.size() > 0) {
            if(value.get(0) instanceof Bit && value.size() > 2) return "{" + asInt() + "},";
            else if(isString()) return "{" + asString() + "},";
        }
        return value.stream().map(v -> v.toString()).collect(Collectors.joining(",", "{", "},"));
    }

    public boolean isString() {
        return value.stream().allMatch(n -> n instanceof Val && ((Val) n).isChar());
    }

    public boolean isChar() {
        return value.size() == 8 && value.stream().allMatch(n -> n instanceof Bit);
    }

    public Val filter(String condition) {
        Val filteredval = new Val(this);
        for(int i = 0; i < value.size(); i++)
            if(new Val(this, i).interpret(condition).asInt() == 1) filteredval.value.add(value.get(i).clone());
        return filteredval;
    }

    public List<Value> elemsToVals(List<String> elems, Val holder) {
        return elems.stream().map(this::interpret).map(n -> n.clone()).map(n -> {
            n.holder = holder;
            return n;
        }).collect(Collectors.toList());
    }

    public Optional<Value> execute() {
        return isString() ? execute(asString()) : value.stream().map(v -> v.execute()).filter(n -> n.isPresent()).map(n -> n.get()).findAny();
    }

    Optional<Value> execute(String s) {
        if(App.debugLevel >= 2) System.out.println(s);
        int index = baseIndex(s, '@');
        if(index >= 0) {
            set(interpret(s.substring(0, index)), interpret(s.substring(index + 1)).clone());
            return Optional.empty();
        }
        index = baseIndex(s, '~');
        if(index >= 0) return Optional.of(interpret(s.substring(index + 1)).clone());
        if(s.isEmpty()) return Optional.empty();
        return interpret(s).execute();
    }

    public Value interpret(String code) {
        code += ";";
        Value result = new Val(this);
        Mode mode = Mode.START;
        int bracketlevel = 0;
        String readString = "";
        String operation = "";
        for(char c : code.toCharArray()) {
            if((c + "").matches("[\\[{(]")) bracketlevel++;
            if((c + "").matches("[\\]})]")) bracketlevel--;
            switch(mode) {
                case START:
                    if(c == '{') mode = Mode.VALUE;
                    else if(c == '\'') mode = Mode.VARIABLE;
                    else if(c == '(') mode = Mode.PARENTHESIS;
                    else if(c == ':') {
                        result = this;
                        mode = Mode.MODIFIER;
                    } else if(c == '`') {
                        result = holder;
                        mode = Mode.MODIFIER;
                    }
                    break;
                case VALUE:
                    if(bracketlevel == 0) {
                        result = isList(readString) ? new Val(this, elemsToVals(stringToElems(readString), this)) : new Val(this, readString);
                        readString = "";
                        mode = Mode.MODIFIER;
                    }
                    readString += c;
                    break;
                case VARIABLE:
                    if(c == '\'') {
                        result = get(litToVal(readString));
                        readString = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    readString += c;
                    break;
                case MODIFIER:// read what to do with ret
                    if(c == '[') {
                        readString = "";
                        mode = Mode.INDEX;
                    } else {
                        readString = String.valueOf(c);
                        mode = Mode.OPERATION;
                    }
                    break;
                case INDEX: // read index acess
                    if(bracketlevel == 0) {
                        if(isNumeric(readString)) result = ((Val) result).get(Integer.parseInt(readString));
                        else if(readString.contains(":")) result = ((Val) result).filter(readString);
                        else result = ((Val) result).get(interpret(readString).asInt());
                        readString = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    readString += c;
                    break;
                case OPERATION:// read operation
                    if(String.valueOf(c).matches("[{'(:]")) {
                        mode = Mode.OP_ARG_2;
                        operation = litToVal(readString);
                        readString = "";
                    }
                    readString += c;
                    break;
                case OP_ARG_2:// read second input and execute operation
                    if(c == ';') {
                        Value argb = interpret(readString);
                        if(operation.equals(litToVal("."))) result = ((Val) result).getLocal(argb.toString());
                        else {
                            Val op = ((Val) get(operation)).clone();
                            op.put(litToVal("a"), result);
                            op.put(litToVal("b"), argb);
                            result = op.execute().get();
                        }
                    }
                    readString += c;
                    break;
                case PARENTHESIS:// end paren
                    if(bracketlevel == 0) {
                        result = interpret(readString);
                        readString = "";
                        mode = Mode.MODIFIER;
                    }
                    readString += c;
                    break;
                default:
                    System.err.println("Something very wrong happened");
            }
        }
        return result;
    }

    public Val top() {
        if(holder == null) return this;
        return holder.top();
    }

    @Override
    public Val clone() {
        Val clone = new Val(holder);
        value.stream().map(n -> n.clone()).forEach(clone.value::add);
        subelems.entrySet().forEach(n -> clone.subelems.put(n.getKey(), n.getValue().clone()));
        ;
        return clone;
    }

    public void print() {
        subelems.entrySet().stream().map(n -> n.getKey() + " - " + n.getValue().toString()).forEach(System.out::println);
    }

    @Override
    public Iterator<Value> iterator() {
        return value.iterator();
    }
}