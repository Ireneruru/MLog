package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.ir.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.HashMap;

public class Assignment {
    private String name;
    private Expression[] index;
    private Expression value;
    private HashMap<String, Variable> vars = new HashMap<>();

    public Assignment(String name) {
        this.name = name;
    }

    public void setIndex(ArrayList<Expression> index) {
        this.index = index.toArray(new Expression[0]);
    }

    public void setValue(Expression expr) {
        this.value = expr;
    }

    public void setVar(HashMap<String, Variable> range) {
        for (String key : range.keySet()) {
            this.vars.put(key, range.get(key));
        }
    }

    public void finish() {
        View view;
        if (Context.getType(name) != null) {
            view = Context.getView(name);
        } else {
            view = new View(name, index.length);
        }
        if (index.length != view.indexNum()) {
            throw new ParseCancellationException("view definition with wrong index number");
        }
        String[] var = new String[index.length];
        String[] vars = this.vars.keySet().toArray(new String[0]);
        for (int i = 0; i < index.length; ++i) {
            var[i] = null;
            for (int j = 0; j < vars.length; ++j) {
                if (vars[j] == null) {
                    continue;
                }
                if (index[i].relate(vars[j])) {
                    if (var[i] == null) {
                        var[i] = vars[j];
                        vars[j] = null;
                    } else {
                        throw new ParseCancellationException("one index has most 1 variable");
                    }
                }
            }
        }
        Range[] range = new Range[index.length];
        for (int i = 0; i < index.length; ++i) {
            if (var[i] == null) {
                range[i] = new Range(index[i].toConst(), index[i].toConst());
                for (int j = i + 1; j < index.length; ++j) {
                    index[j] = Utils.replace(index[j], index[i], view.idx(i));
                    if (var[j] == null) {
                        continue;
                    }
                    Variable g = this.vars.get(var[j]);
                    Expression b = Utils.replace(g.begin(), index[i], view.idx(i));
                    Expression e = Utils.replace(g.end(), index[i], view.idx(i));
                    this.vars.put(var[j], new Variable(b, e));
                }
                value = Utils.replace(value, index[i], view.idx(i));
            }
            else {
                Variable r = Utils.replaceRange(index[i], var[i], this.vars.get(var[i]));
                range[i] = new Range(r.begin().toConst(), r.end().toConst());
                Expression v = Utils.convert(index[i], var[i], view.idx(i));
                for (int j = i + 1; j < index.length; ++j) {
                    index[j] = Utils.replace(index[j], var[i], v);
                }
                value = Utils.replace(value, var[i], v);
                for (String k : this.vars.keySet()) {
                    Variable g = this.vars.get(k);
                    Expression b = Utils.replace(g.begin(), var[i], v);
                    Expression e = Utils.replace(g.end(), var[i], v);
                    this.vars.put(k, new Variable(b, e));
                }
            }
        }
        view.putAssign(new Assign(range, value.toValue()));
    }
}
