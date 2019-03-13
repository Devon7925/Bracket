package app.bcrt.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Val implements Cloneable {

    public List<Val> value;
    List<Var> subelems = new ArrayList<>(0);
    Var holder = null;

    public Val() {
        set();
    }

    public Val(String newval) {
        if(newval.equals("")) set();
        else if(newval.matches("\\d+")) set(Integer.parseInt(newval));
        else set(newval);
    }

    public Val(int newval) {
        set(newval);
    }

    public Val(Val newval) {
        set(newval);
        holder = newval.holder;
    }

    public Val(ArrayList<Val> value) {
        this.value = value;
    }

    public void set() {
        value = new ArrayList<>(0);
    }

    public void set(String newval) {
        value = new ArrayList<>(newval.length());
        for(int i = 0; i < newval.length(); i++) {
            int charval = newval.toCharArray()[i];
            ArrayList<Val> character = new ArrayList<>(8);
            for(int j = 0; j < 8; j++)
                character.add(new Bit((charval >> j) % 2 == 1));
            Val valchar = new Val();
            valchar.value = character;
            value.add(new Val(character));
        }
    }

    public void set(int newval) {
        if(newval == 0) {
            value = new ArrayList<Val>(1);
            value.add(new Bit(false));
            return;
        }
        int digits = (int) Math.floor(Math.log(newval) / Math.log(2)) + 1;
        value = new ArrayList<>(digits);
        for(int i = 0; i < digits; i++)
            value.add(new Bit((newval >> i) % 2 == 1));
    }

    public void set(Bit newval) {
        value = new ArrayList<Val>(1);
        value.add(new Bit(newval));
    }

    public void set(Val newval) {
        if(newval instanceof Bit) {
            set((Bit) newval);
            return;
        }
        this.value = new ArrayList<>(newval.value.size());
        newval.value.stream().map(n -> n.clone()).forEach(value::add);
        this.subelems = new ArrayList<>(newval.subelems.size());
        newval.subelems.stream().map(n -> n.clone()).forEach(subelems::add);
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
        Var v;
        if(newvar.isPresent()) v = newvar.get();
        else {
            Var extv;
            if(holder == null) extv = App.get(name);
            else {
                extv = holder.get(name);
                if(extv != null) {
                    if(this instanceof Var) extv.holder = ((Var) this);
                    else extv.holder = holder;
                }
            }

            if(extv == null) {
                v = new Var(name);
                subelems.add(v);
            } else v = extv;
        }
        return v;
    }

    public Var getLocal(String name) {
        Var extv = subelems.stream().filter(n -> n.name.equals(name)).findAny().orElse(null);
        Var v;
        if(extv == null) {
            v = new Var(name);
            v.holder = getVarHolder();
            subelems.add(v);
        } else v = extv;
        return v;
    }

    int interpretInt() {
        int ret = 0;
        for(int i = 0; i < value.size(); i++)
            ret += value.get(i).interpretInt() << i;
        return ret;
    }

    public String interpretString() {
        return value.stream().map(n -> (char) n.interpretInt()).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public String toString() {
        String ret = "{";
        if(value.size() > 0) {
            if(value.get(0) instanceof Bit && value.size() > 2) ret += interpretInt();
            else if(isString()) ret += interpretString();
        }
        if(ret.length() == 1) for(Val v : value)
            ret += v.toString() + ",";
        return ret + "},";
    }

    public boolean isString() {
        return value.get(0).value.size() == 8 && value.get(0).get(0) instanceof Bit;
    }

    public Val execute(Val context) {
        Val ret = null;
        if(value.size() == 0 || isString()) return App.execute(interpretString(), context);
        else for(Val v1 : value) {
            Val toret = v1.execute(context);
            if(toret != null) ret = toret;
        }
        return ret;
    }

    public Val interpret(String code) {
        code += ";";
        Val result = null;
        Mode mode = Mode.START;
        int bracketlevel = 0;
        String readString = "";
        String operation = "";
        for(char c : code.toCharArray()) {
            switch(c) {
                case '{':
                case '[':
                case '(':
                    bracketlevel++;
                    break;
                case '}':
                case ']':
                case ')':
                    bracketlevel--;
                    break;
            }
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
                        if(StringTool.isList(readString)) {
                            result = new Val();
                            result.value = StringTool.stringToElems(readString).stream().map(this::interpret).map(n -> n.clone()).collect(Collectors.toList());
                        } else result = new Val(readString);
                        readString = "";
                        mode = Mode.MODIFIER;
                    }
                    readString += c;
                    break;
                case VARIABLE:
                    if(c == '\'') {
                        String name = new Val(readString).toString();
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
                        else if(readString.contains(":")) {
                            Val filteredval = new Val();
                            for(int i = 0; i < result.value.size(); i++)
                                if(new Val(i).interpret(readString).interpretInt() == 1) filteredval.value.add(result.value.get(i).clone());
                            result = filteredval;
                        } else result = result.get(interpret(readString).interpretInt());
                        readString = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    readString += c;
                    break;
                case OPERATION:// read operation
                    if(c == '{' || c == '\'' || c == '(' || c == ':') {
                        mode = Mode.OP_ARG_2;
                        operation = new Val(readString).toString();
                        readString = "";
                    }
                    readString += c;
                    break;
                case OP_ARG_2:// read second input and execute operation
                    if(c == ';') {
                        Val tempb = interpret(readString);
                        if(operation.equals(new Val(".").toString())) result = ((Var) result).getLocal(tempb.toString());
                        else {
                            Var a = new Var(new Val("a").toString());
                            Var b = new Var(new Val("b").toString());
                            a.set(result);
                            b.set(tempb);
                            Val v = get(operation);
                            App.vars.add(0, a);
                            App.vars.add(0, b);
                            result = v.execute(v);
                            App.vars.remove(a);
                            App.vars.remove(b);
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
        if(this instanceof Var) return (Var) this;
        else return holder.getVarHolder();
    }

    protected Val clone() {
        return new Val(this);
    }
}