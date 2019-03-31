package app.bcrt.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Val extends AppTool implements Cloneable {

    public List<Val> value;
    public Map<String, Val> subelems;
    public Val holder;

    public Val(Val holder) {
        this.holder = holder;
        set();
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

    public Val(Val holder, List<Val> value) {
        this(holder);
        set(value);
    }

    public void set() {
        value = new ArrayList<>(0);
        subelems = new HashMap<>(0);
    }

    public void set(List<Val> value) {
        value.forEach(n -> n.holder = this);
        this.value = value;
    }

    public void set(String newval) {
        value = new ArrayList<>(newval.length());
        for(char c : newval.toCharArray())
            value.add(new Val(this, c, 8));
    }

    public void set(int newval) {
        set(newval, (int) Math.floor(log(2, newval)) + 1);
    }

    public void set(int newval, int minsize) {
        if(newval == 0) {
            value = Arrays.asList(new Bit(this));
            return;
        }
        value = new ArrayList<>(minsize);
        for(int digit = 0; digit < minsize; digit++)
            value.add(new Bit(this, (newval >> digit) % 2 == 1));
    }

    public void set(Val newval) {
        this.value = newval.value.stream().map(n -> n.clone()).map(n -> {n.holder = this; return n;}).collect(Collectors.toList());
        this.subelems = newval.subelems.entrySet().stream().collect(Collectors.toMap(n -> n.getKey(), n -> {Val temp = n.getValue().clone(); temp.holder = this; return temp;}));
    }

    Val get(int index) {
        if(index == value.size()) value.add(new Bit(this));
        return (index < value.size()) ? value.get(index) : new Bit(this);
    };

    public Val get(String name) {
        Val newvar = subelems.get(name);
        if(newvar != null) return newvar;

        Val extv = (holder == null) ? null : holder.get(name);

        if(extv != null) return extv;

        Val v = new Val(this);
        subelems.put(name, v);
        return v;
    }

    public Val getLocal(String name) {
        Val extv = subelems.get(name);
        if(extv != null) return extv;
        Val v = new Val(this);
        subAssign(name, v);
        return v;
    }

    int asInt() {
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
        return value.stream().allMatch(n -> n.isChar());
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

    public List<Val> elemsToVals(List<String> elems, Val holder) {
        return elems.stream().map(this::interpret).map(n -> n.clone()).map(n -> {
            n.holder = holder;
            return n;
        }).collect(Collectors.toList());
    }

    public Optional<Val> execute() {
        return isString() ? execute(asString()) : value.stream().map(v -> v.execute()).filter(n -> n.isPresent()).map(n -> n.get()).findAny();
    }

    Optional<Val> execute(String s) {
        if(App.debugLevel >= 2) System.out.println(s);
        int index = baseIndex(s, '@');
        if(index >= 0) {
            Val tempval = interpret(s.substring(0, index));
            tempval.set(interpret(s.substring(index + 1)).clone());
            return Optional.empty();
        }
        index = baseIndex(s, '~');
        if(index >= 0) return Optional.of(interpret(s.substring(index + 1)).clone());
        if(s.isEmpty()) return Optional.empty();
        return interpret(s).execute();
    }

    public Val interpret(String code) {
        code += ";";
        Val result = new Val(this);
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
                        if(isNumeric(readString)) result = result.get(Integer.parseInt(readString));
                        else if(readString.contains(":")) result = result.filter(readString);
                        else result = result.get(interpret(readString).asInt());
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
                        Val argb = interpret(readString);
                        if(operation.equals(litToVal("."))) result = result.getLocal(argb.toString());
                        else {
                            Val op = get(operation).clone();
                            op.subAssign(litToVal("a"), valRebase(op, result));
                            op.subAssign(litToVal("b"), valRebase(op, argb));
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

    public void subAssign(String name, Val val) {
        subelems.put(name, val);
    }

    protected Val clone() {
        Val clone = new Val(holder);
        clone.set(this);
        return clone;
    }
}