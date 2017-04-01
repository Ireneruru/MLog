package DS3Lab.Beryl.target;

import DS3Lab.Beryl.data.Index;
import DS3Lab.Beryl.ir.*;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.EntryType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Target {
    HashMap<String, HashSet<Index>> entries = new HashMap<>();

    Node target;

    private Node get(Value value) {
        switch (value.type()) {
            case OP:
                Node[] args = new Node[value.args().length];
                for (int i = 0; i < args.length; ++i) {
                    args[i] = get(value.arg(i));
                }
                return new Node(value.op(), args);
            case OP_WITH_VAR:
                Integer bg = value.range().getBegin(), ed = value.range().getEnd();
                args = new Node[ed - bg + 1];
                for (int i = bg; i <= ed; ++i) {
                    value.var().setValue(i);
                    args[i - bg] = get(value.arg(0));
                }
                return new Node(value.op(), args);
            case REF:
                int[] idx = new int[value.index().length];
                for (int i = 0; i < idx.length; ++i) {
                    idx[i] = value.index()[i].getValue();
                }
                if (value.ref().type() == EntryType.CONST) {
                    Integer[] x = new Integer[idx.length];
                    for (int i = 0; i < x.length; ++i) {
                        x[i] = idx[i];
                    }
                    return new Node(value.ref().asConst().getValue(x));
                }
                return get(value.ref().name(), idx);
            case INT:
                return new Node(value.intVal());
            case FLOAT:
                return new Node(value.floatVal());
            case ZERO:
                int[] zero = new int[value.shape().length + 1];
                for (int i = 0; i < zero.length - 1; ++i) {
                    zero[i] = value.shape()[i].getValue();
                }
                zero[value.shape().length] = value.dtype() == DataType.INT ? 0 : 1;
                return new Node("_zeros_", zero);
        }
        return null;
    }

    HashMap<String, HashMap<Index, Node>> nodes = new HashMap<>();

    private Node get(String name, int[] index) {
        if (!nodes.containsKey(name)) {
            nodes.put(name, new HashMap<>());
        }
        HashMap<Index, Node> map = nodes.get(name);
        Index idx = new Index(index);
        if (map.containsKey(idx)) {
            return map.get(idx);
        }
        Node ret = null;
        switch (Context.getType(name)) {
            case INT:
            case SCHEMA:
                ret = new Node(name, index.clone());
                if (!entries.containsKey(name)) {
                    entries.put(name, new HashSet<>());
                }
                entries.get(name).add(new Index(index.clone()));
                break;
            case VIEW:
                View view = Context.getView(name);
                if (view.origin != null && view.origin.type() != EntryType.VIEW) {
                    ret = new Node(name, index.clone());
                    if (!entries.containsKey(name)) {
                        entries.put(name, new HashSet<>());
                    }
                    entries.get(name).add(new Index(index.clone()));
                    break;
                }
                Integer tmp[] = new Integer[index.length];
                for (int i = 0; i < index.length; ++i) {
                    tmp[i] = view.index(i).getValue();
                    view.index(i).setValue(index[i]);
                }
                for (Assign assign : view.getAssigns()) {
                    Range[] range = assign.range();
                    boolean fit = true;
                    for (int i = 0; i < index.length; ++i) {
                        if (range[i].getBegin() > index[i] || range[i].getEnd() < index[i]) {
                            fit = false;
                            break;
                        }
                    }
                    if (fit) {
                        ret = get(assign.value());
                    }
                }
                if (ret == null) {
                    int[] zero = new int[view.shape().length + 1];
                    for (int i = 0; i < zero.length - 1; ++i) {
                        zero[i] = view.shape(i).getValue();
                    }
                    zero[view.shape().length] = view.dtype() == DataType.INT ? 0 : 1;
                    ret = new Node("_zeros_", zero);
                }
                for (int i = 0; i < index.length; ++i) {
                    view.index(i).setValue(tmp[i]);
                }
                break;
            default:
                ret = null;
        }
        if (ret == null) {
            throw new ParseCancellationException("illegal target");
        }
        map.put(idx, ret);
        return ret;
    }

    public Target(String target) {
        Context.getConst("_zeros_");
        this.target = get(target, new int[0]);
    }
}
