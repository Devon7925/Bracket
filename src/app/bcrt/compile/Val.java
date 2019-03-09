package app.bcrt.compile;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Val implements Cloneable {

    public ArrayList<Val> value;
    ArrayList<Var> subelems = new ArrayList<>(0);

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
        if(value.size() == 0 || isString()) {
            return App.execute(interpretString(), context);
        } else for(Val v1 : value) {
            Val toret = v1.execute(context);
            if(toret != null) ret = toret;
        }
        return ret;
    }

    public Val interpret(String s) {
        s += ";";
        Val ret = null;
        Mode mode = Mode.START;
        int bracketlevel = 0;
        String current = "";
        String operation = "";
        for(char c : s.toCharArray()) {
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
                        ret = this;
                        mode = Mode.MODIFIER;
                    }
                    break;
                case VALUE:
                    if(bracketlevel == 0) {
                        if(StringTool.isList(current)) {
                            ret = new Val();
                            ret.value = new ArrayList<>(
                                    StringTool.stringToElems(current).stream().map(n -> interpret(n).clone()).collect(Collectors.toList()));
                        } else ret = new Val(current);
                        current = "";
                        mode = Mode.MODIFIER;
                    }
                    current += c;
                    break;
                case VARIABLE:
                    if(c == '\'') {
                        ret = App.get(current);
                        if(ret == null) ret = new Var(current);
                        current = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    current += c;
                    break;
                case MODIFIER:// read what to do with ret
                    if(c == '[') {
                        current = "";
                        mode = Mode.INDEX;
                    } else {
                        current = "" + c;
                        mode = Mode.OPERATION;
                    }
                    break;
                case INDEX: // read index acess
                    if(bracketlevel == 0) {
                        if(current.matches("\\d+")) ret = ret.get(Integer.parseInt(current));
                        else if(current.contains(":")) {
                            Val filteredval = new Val();
                            for(int i = 0; i < ret.value.size(); i++)
                                if(new Val(i).interpret(current).interpretInt() == 1) filteredval.value.add(ret.value.get(i).clone());
                            ret = filteredval;
                        } else ret = ret.get(interpret(current).interpretInt());
                        current = "";
                        mode = Mode.MODIFIER;
                        break;
                    }
                    current += c;
                    break;
                case OPERATION:// read operation
                    if(c == '{' || c == '\'' || c == '(' || c == ':') {
                        mode = Mode.INPUT2;
                        operation = current;
                        current = "";
                    }
                    current += c;
                    break;
                case INPUT2:// read second input and execute operation
                    if(c == ';') {
                        Val tempb = interpret(current);
                        if(operation.equals(".")) {
                            ret = ((Var) ret).get(tempb.toString());
                        } else {
                            Var a = new Var("a");
                            Var b = new Var("b");
                            a.set(ret);
                            b.set(tempb);
                            Val v = App.get(operation);
                            App.vars.add(0, a);
                            App.vars.add(0, b);
                            ret = v.execute(v);
                            App.vars.remove(0);
                            App.vars.remove(0);
                        }
                    }
                    current += c;
                    break;
                case PARENTHESIS:// end paren
                    if(bracketlevel == 0) {
                        ret = interpret(current);
                        current = "";
                        mode = Mode.MODIFIER;
                    }
                    current += c;
                    break;
                default:
                    System.err.println("Something very wrong happened");
            }
        }
        return ret;
    }

    protected Val clone() {
        return new Val(this);
    }
}