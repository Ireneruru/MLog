package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.data.FileSource;
import DS3Lab.Beryl.data.FuncSource;
import DS3Lab.Beryl.ir.Context;
import DS3Lab.Beryl.ir.Schema;
import DS3Lab.Beryl.target.Tensorflow;
import DS3Lab.Beryl.type.ExprType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;

public class Processor {
    public static void copyFromFile(String target, String path)
            throws ParseCancellationException {
        Schema s = Context.getSchema(target);
        if (s == null) {
            throw new ParseCancellationException(target + " is not a Schema");
        }
        s.setSource(new FileSource(path));
    }

    public static void copyFromFunction(String target, String func, Expression[] args)
            throws ParseCancellationException {
        Schema s = Context.getSchema(target);
        if (s == null) {
            throw new ParseCancellationException(target + " is not a Schema");
        }
        float[] a = new float[args.length];
        for (int i = 0; i < args.length; ++i) {
            Float f = Utils.floatValue(args[i]);
            if (f == null) {
                throw new ParseCancellationException("illegal function definition");
            }
            a[i] = f;
        }
        s.setSource(new FuncSource(func, a));
    }

    public static void evaluate(Query query) {
        Tensorflow tf = new Tensorflow(query);
        tf.run();
    }
}
