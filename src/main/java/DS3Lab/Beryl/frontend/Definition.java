package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.ir.*;
import DS3Lab.Beryl.type.DataType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.HashMap;

public class Definition {
    private String name;
    private String[] index;
    private Variable[] vars;
    private DataType dtype;
    private Expression[] shape;

    public Definition(String name) {
        this.name = name;
    }

    public void setIndex(ArrayList<String> index) {
        this.index = index.toArray(new String[0]);
    }

    public void setVar(HashMap<String, Variable> range) {
        vars = new Variable[index.length];
        for (int i = 0; i < index.length; ++i) {
            vars[i] = range.get(index[i]);
        }
    }

    public void setDtype(DataType dtype) {
        this.dtype = dtype;
    }

    public void setShape(ArrayList<Expression> shapes) {
        this.shape = shapes.toArray(new Expression[0]);
    }

    public void finish() {
        if (Context.getType(name) != null) {
            throw new ParseCancellationException(name + " has defined");
        }
        Value[] shape = new Value[this.shape.length];
        for (int i = 0; i < shape.length; ++i) {
            for (String idx : index) {
                if (this.shape[i].relate(idx)) {
                    throw new ParseCancellationException("illegal schema definition");
                }
            }
            shape[i] = this.shape[i].toConst();
        }
        Schema schema = new Schema(name, dtype, shape);
        schema.setIndexNum(index.length);
        Range[] range = new Range[index.length];
        for (int i = 0; i < index.length; ++i) {
            Expression begin = vars[i].begin(), end = vars[i].end();
            for (int j = 0; j < index.length; ++j) {
                begin = Utils.replace(begin, index[j], new Expression(schema.idx(j)));
                end = Utils.replace(end, index[j], new Expression(schema.idx(j)));
            }
            range[i] = new Range(begin.toConst(), end.toConst());
        }
        schema.setRange(range);
    }
}
