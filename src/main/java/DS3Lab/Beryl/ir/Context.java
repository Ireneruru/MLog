package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.type.SymbolType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.HashMap;

public class Context {
    private static HashMap<String, SymbolType> stype = new HashMap<>();
    private static HashMap<String, Object> value = new HashMap<>();

    public static SymbolType getType(String s) {
        return stype.get(s);
    }

    public static void putFloat(String s, Float f) {
        stype.put(s, SymbolType.FLOAT);
        value.put(s, f);
    }

    public static Float getFloat(String s) throws ParseCancellationException {
        if (getType(s) == SymbolType.FLOAT) {
            return (Float) value.get(s);
        }
        else {
            throw new ParseCancellationException("Float " + s + " do not exist");
        }
    }

    public static Const getConst(String s) throws ParseCancellationException {
        if (getType(s) == SymbolType.INT) {
            return (Const) value.get(s);
        }
        if (getType(s) == null) {
            Const c = new Const(s);
            stype.put(s, SymbolType.INT);
            value.put(s, c);
            if (s.charAt(0) == '#') {
                c.setValue(null);
            }
            return c;
        }
        throw new ParseCancellationException("get Const " + s + " failed");
    }

    public static void putInt(String s, Integer i) {
        Const c = getConst(s);
        c.setValue(i);
    }

    public static Integer getInt(String s) throws ParseCancellationException {
        if (getType(s) == SymbolType.INT) {
            return ((Const)value.get(s)).getValue();
        }
        else {
            throw new ParseCancellationException("Int " + s + " do not exist");
        }
    }

    static void putSchema(String s, Schema sch) {
        stype.put(s, SymbolType.SCHEMA);
        value.put(s, sch);
    }

    public static Schema getSchema(String s) throws ParseCancellationException {
        if (getType(s) == SymbolType.SCHEMA) {
            return (Schema) value.get(s);
        }
        else {
            throw new ParseCancellationException("Schema " + s + " do not exist");
        }
    }

    public static void putView(String s, View v) {
        stype.put(s, SymbolType.VIEW);
        value.put(s, v);
    }

    public static View getView(String s) throws ParseCancellationException {
        if (getType(s) == SymbolType.VIEW) {
            return (View) value.get(s);
        }
        else {
            throw new ParseCancellationException("View " + s + " do not exist");
        }
    }
}
