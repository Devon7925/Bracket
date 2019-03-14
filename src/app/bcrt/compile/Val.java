package app.bcrt.compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Val implements Cloneable {

    public List<Val> value;
    List<Var> subelems;
    Var holder = null;

    public Val() {
        set();
    }

    public Val(String newval) {
        this();
        if(newval.equals("")) set();
        else if(newval.matches("\\d+")) set(Integer.parseInt(newval));
        else set(newval);
    }

    public Val(int newval) {
        this();
        set(newval);
    }

    public Val(Val newval) {
        this();
        set(newval);
        holder = newval.holder;
    }

    public Val(List<Val> value) {
        this();
        this.value = value;
    }

    public void set() {
        value = new ArrayList<>(0);
        subelems = new ArrayList<>(0);
    }

    public void set(String newval) {
        value = new ArrayList<>(newval.length());
        for(char c : newval.toCharArray()) {
            List<Val> character = new ArrayList<>(8);
            for(int digit = 0; digit < 8; digit++)
                character.add(new Bit((c >> digit) % 2 == 1));
            value.add(new Val(character));
        }
    }

    public void set(int newval) {
        if(newval == 0) {
            value = Arrays.asList(new Bit(false));
            return;
        }
        int numOfDigits = (int) Math.floor(AppTool.log(2, newval)) + 1;
        value = new ArrayList<>(numOfDigits);
        for(int digit = 0; digit < numOfDigits; digit++)
            value.add(new Bit((newval >> digit) % 2 == 1));
    }

    public void set(Bit newval) {
        value = Arrays.asList(newval);
    }

    public void set(Val newval) {
        if(newval instanceof Bit) {
            set((Bit) newval);
            return;
        }
        this.value = newval.value.stream().map(n -> n.clone()).collect(Collectors.toList());
        this.subelems = newval.subelems.stream().map(n -> n.clone()).collect(Collectors.toList());
    }

    Val get(int index) {
        if(index < value.size()) return value.get(index);
        else {
            Val oobelem = new Bit(false);
            if(index == value.size()) value.add(oobelem);
            return oobelem;
        }
    };

    public Var get(String name) {
        Optional<Var> newvar = subelems.stream().filter(n -> n.name.equals(name)).findAny();
        if(newvar.isPresent()) return newvar.get();
        else {
            Var extv = (holder == null) ? App.get(name) : holder.get(name);

            if(extv == null) {
                Var v = new Var(name);
                subelems.add(v);
                return v;
            } else return extv;
        }
    }

    public Var getLocal(String name) {
        Var extv = subelems.stream().filter(n -> n.name.equals(name)).findAny().orElse(null);
        if(extv == null) {
            Var v = new Var(name);
            v.holder = getVarHolder();
            subelems.add(v);
            return v;
        }
        return extv;
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
        Val filteredval = new Val();
        for(int i = 0; i < value.size(); i++)
            if(new Val(i).interpret(condition).asInt() == 1) filteredval.value.add(value.get(i).clone());
        return filteredval;
    }

    public List<Val> elemsToVals(List<String> elems) {
        return elems.stream().map(this::interpret).map(n -> n.clone()).collect(Collectors.toList());
    }

    public Val execute(Val context) {
        return (value.size() == 0 || isString()) ? App.execute(asString(), context)
                : value.stream().map(v -> v.execute(context)).filter(n -> n != null).findAny().orElse(null);
    }

    public Val interpret(String code) {
        code += ";";
        Val result = null;
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
                        result = holder.getVarHolder();
                        mode = Mode.MODIFIER;
                    }
                    break;
                case VALUE:
                    if(bracketlevel == 0) {
                        result = AppTool.isList(readString) ? new Val(elemsToVals(AppTool.stringToElems(readString))) : new Val(readString);
                        readString = "";
                        mode = Mode.MODIFIER;
                    }
                    readString += c;
                    break;
                case VARIABLE:
                    if(c == '\'') {
                        String name = AppTool.litToVal(readString);
                        result = get(name);
                        if(result == null) result = new Var(name);
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
                        readString = "" + c;
                        mode = Mode.OPERATION;
                    }
                    break;
                case INDEX: // read index acess
                    if(bracketlevel == 0) {
                        if(readString.matches("\\d+")) result = result.get(Integer.parseInt(readString));
                        else if(readString.contains(":")) result = result.filter(readString);
                        else result = result.get(interpret(readString).asInt());
                        readString = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    readString += c;
                    break;
                case OPERATION:// read operation
                    if(("" + c).matches("[{'(:]")) {
                        mode = Mode.OP_ARG_2;
                        operation = AppTool.litToVal(readString);
                        readString = "";
                    }
                    readString += c;
                    break;
                case OP_ARG_2:// read second input and execute operation
                    if(c == ';') {
                        Val tempb = interpret(readString);
                        if(operation.equals(AppTool.litToVal("."))) result = ((Var) result).getLocal(tempb.toString());
                        else {
                            Var a = new Var(AppTool.litToVal("a"));
                            Var b = new Var(AppTool.litToVal("b"));
                            a.set(result);
                            b.set(tempb);
                            Val v = get(operation);
                            App.vars.addAll(0, Arrays.asList(a, b));
                            result = v.execute(v);
                            App.vars.removeAll(Arrays.asList(a, b));
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

    public Var getVarHolder() {
        return (this instanceof Var) ? (Var) this : holder.getVarHolder();
    }

    protected Val clone() {
        return new Val(this);
    }
}