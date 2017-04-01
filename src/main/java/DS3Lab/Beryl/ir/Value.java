package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.target.Operator;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.EntryType;
import DS3Lab.Beryl.type.ValueType;

public class Value {
    private DataType dtype;
    private Value shape[];
    private ValueType type;

    private String op;
    private Const var;
    private Range range;
    private Value[] args;

    private Entry ref;
    private Value[] index;

    private int intVal;
    private float floatVal;

    public Value(String op, Value[] args) {
        this.op = op;
        this.args = args;
        this.type = ValueType.OP;
        this.dtype = Operator.dtype(op, args);
        this.shape = Operator.shape(op, range, args);
    }

    public Value(Const var, Range range, String op, Value arg) {
        this.var = var;
        this.range = range;
        this.op = op;
        this.args = new Value[] {arg};
        this.type = ValueType.OP_WITH_VAR;
        this.dtype = Operator.dtype(op, args);
        this.shape = Operator.shape(op, range, args);
    }

    public Value(Entry ref, Value[] index) {
        this.ref = ref;
        this.index = index;
        this.type = ValueType.REF;
        if (ref != null) {
            this.dtype = ref.dtype();
            this.shape = ref.shape();
        }
    }

    public Value(Integer n) {
        this.intVal = n;
        this.type = ValueType.INT;
        this.dtype = DataType.INT;
        this.shape = new Value[0];
    }

    public Value(Float f) {
        this.floatVal = f;
        this.type = ValueType.FLOAT;
        this.dtype = DataType.FLOAT;
        this.shape = new Value[0];
    }

    public Value(Value[] shape, DataType dtype) {
        this.type = ValueType.ZERO;
        this.dtype = dtype;
        this.shape = shape;
    }

    public ValueType type() {
        return type;
    }

    public DataType dtype() {
        return dtype;
    }

    public Value[] shape() {
        return shape;
    }

    public boolean isConst() {
        switch (type) {
            case INT:
                return true;
            case FLOAT:
                return false;
            case REF:
                if (ref.type() != EntryType.CONST) {
                    return false;
                }
                for (Value i : index) {
                    if (!i.isConst()) {
                        return false;
                    }
                }
                return true;
            case OP: {
                switch (op) {
                    case "add":
                    case "sub":
                        return arg(0).isConst() && arg(1).isConst();
                    default:
                        return false;
                }
            }
            case ZERO:
                return false;
        }
        return false;
    }

    public String op() {
        return op;
    }

    public Const var() {
        return var;
    }

    public Range range() {
        return range;
    }

    public Value[] args() {
        return args;
    }

    public Value arg(int i) {
        return args[i];
    }

    public Entry ref() {
        return ref;
    }

    public Value[] index() {
        return index;
    }

    public int intVal() {
        return intVal;
    }

    public float floatVal() {
        return floatVal;
    }

    public Integer getValue() {
        if (!isConst()) {
            return null;
        }
        switch (type) {
            case INT:
                return intVal;
            case REF: {
                if (ref.type() != EntryType.CONST) {
                    return null;
                }
                Const c = ref.asConst();
                Integer[] idx = new Integer[index.length];
                for (int i = 0; i < idx.length; ++i) {
                    idx[i] = index[i].getValue();
                    if (idx[i] == null) {
                        return null;
                    }
                }
                return c.getValue(idx);
            }
            case OP: {
                Integer a = args[0].getValue();
                Integer b = args[1].getValue();
                if (a == null || b == null) {
                    return null;
                }
                switch (op) {
                    case "add":
                        return a + b;
                    case "sub":
                        return a - b;
                }
            }
        }
        return null;
    }

    public boolean setValue(int value) {
        if (!isConst()) {
            return false;
        }
        Integer v = getValue();
        if (v != null && v != value) {
            return false;
        }
        switch (type) {
            case INT:
                return true;
            case REF: {
                Const c = ref.asConst();
                Integer[] idx = new Integer[index.length];
                for (int i = 0; i < idx.length; ++i) {
                    idx[i] = index[i].getValue();
                    if (idx[i] == null) {
                        return false;
                    }
                }
                c.setValue(idx, value);
                return true;
            }
            case OP: {
                Integer a = args[0].getValue();
                Integer b = args[1].getValue();
                if (a == null && b == null) {
                    return false;
                }
                switch (op) {
                    case "add":
                        if (a == null) {
                            args[0].setValue(value - b);
                        }
                        else {
                            args[1].setValue(value - a);
                        }
                        break;
                    case "sub":
                        if (a == null) {
                            args[0].setValue(value + b);
                        }
                        else {
                            args[1].setValue(a - value);
                        }
                        break;
                }
                return true;
            }
        }
        return false;
    }

    public Integer minVal() {
        if (!isConst()) {
            return null;
        }
        switch (type) {
            case INT:
                return intVal();
            case REF: {
                Const c = ref.asConst();
                Integer[] idx = new Integer[index.length];
                for (int i = 0; i < idx.length; ++i) {
                    idx[i] = index[i].getValue();
                    if (idx[i] == null) {
                        return c.minVal();
                    }
                }
                Integer val = c.getValue(idx);
                if (val == null) {
                    return c.minVal();
                }
                return val;
            }
            case OP: {
                if (op.equals("add")) {
                    return args[0].minVal() + args[1].minVal();
                }
                else {
                    return args[0].minVal() - args[1].maxVal();
                }
            }
        }
        return null;
    }

    public Integer maxVal() {
        if (!isConst()) {
            return null;
        }
        switch (type) {
            case INT:
                return intVal();
            case REF: {
                Const c = ref.asConst();
                Integer[] idx = new Integer[index.length];
                for (int i = 0; i < idx.length; ++i) {
                    idx[i] = index[i].getValue();
                    if (idx[i] == null) {
                        return c.maxVal();
                    }
                }
                Integer val = c.getValue(idx);
                if (val == null) {
                    return c.maxVal();
                }
                return val;
            }
            case OP: {
                if (op.equals("add")) {
                    return args[0].maxVal() + args[1].maxVal();
                }
                else {
                    return args[0].maxVal() - args[1].minVal();
                }
            }
        }
        return null;
    }

    public boolean relate(Const c) {
        switch (type) {
            case INT:
                return false;
            case REF: {
                if (ref.asConst().name().equals(c.name())) {
                    return true;
                }
                for (Value i : index) {
                    if (i.relate(c)) {
                        return true;
                    }
                }
                return false;
            }
            case OP: {
                if (range != null && (range.begin().relate(c) || range.end().relate(c))) {
                    return true;
                }
                for (Value a : args) {
                    if (a.relate(c)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }
}
