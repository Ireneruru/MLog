package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.ir.Context;
import DS3Lab.Beryl.type.ExprType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Utils {
    static Expression convert(Expression e, String s, String r)
            throws ParseCancellationException {
        if (!e.relate(s)) {
            throw new ParseCancellationException("convert failed, don't relate");
        }
        switch (e.type()) {
            case INT:
                return e;
            case FLOAT:
                return e;
            case ID:
                if (e.asId().equals(s)) {
                    return new Expression(r);
                }
                else {
                    return e;
                }
            case OP:
                String op = e.asOp().op();
                Expression[] args = e.asOp().args();
                switch (op) {
                    case "add": {
                        Expression a = args[0], b = args[1];
                        if (a.relate(s)) {
                            if (b.relate(s)) {
                                throw new ParseCancellationException("convert 'add' failed");
                            }
                            String tmp = tmpStr();
                            b = new Expression("add", new Expression[]{new Expression(r), b});
                            return replace(convert(a, s, tmp), tmp, b);
                        }
                        else {
                            String tmp = tmpStr();
                            a = new Expression("sub", new Expression[]{a, new Expression(r)});
                            return replace(convert(b, s, tmp), tmp, a);
                        }
                    }
                    case "sub": {
                        Expression a = args[0], b = args[1];
                        if (a.relate(s)) {
                            if (b.relate(s)) {
                                throw new ParseCancellationException("convert 'add' failed");
                            }
                            String tmp = tmpStr();
                            b = new Expression("sub", new Expression[]{new Expression(r), b});
                            return replace(convert(a, s, tmp), tmp, b);
                        }
                        else {
                            String tmp = tmpStr();
                            a = new Expression("sub", new Expression[]{new Expression(r), a});
                            return replace(convert(b, s, tmp), tmp, a);
                        }
                    }
                    default:
                        throw new ParseCancellationException("unsupport convert operator");
                }
            default:
                return null;
        }
    }

    static Variable replaceRange(Expression e, String s, Variable v)
            throws ParseCancellationException {
        if (!e.relate(s)) {
            return new Variable(e, e);
        }
        switch (e.type()) {
            case ID:
                return v;
            case OP:
                String op = e.asOp().op();
                Expression[] args = e.asOp().args();
                switch (op) {
                    case "add": {
                        Expression a = args[0], b = args[1];
                        if (a.relate(s)) {
                            if (b.relate(s)) {
                                throw new ParseCancellationException("replace range 'add' failed");
                            }
                            v = replaceRange(a, s, v);
                            Expression begin = new Expression("add", new Expression[]{v.begin(), b});
                            Expression end = new Expression("add", new Expression[]{v.end(), b});
                            return new Variable(begin, end);
                        }
                        else {
                            v = replaceRange(b, s, v);
                            Expression begin = new Expression("add", new Expression[]{v.begin(), a});
                            Expression end = new Expression("add", new Expression[]{v.end(), a});
                            return new Variable(begin, end);
                        }
                    }
                    case "sub": {
                        Expression a = args[0], b = args[1];
                        if (a.relate(s)) {
                            if (b.relate(s)) {
                                throw new ParseCancellationException("convert 'add' failed");
                            }
                            v = replaceRange(a, s, v);
                            Expression begin = new Expression("sub", new Expression[]{v.begin(), b});
                            Expression end = new Expression("sub", new Expression[]{v.end(), b});
                            return new Variable(begin, end);
                        }
                        else {
                            v = replaceRange(b, s, v);
                            Expression begin = new Expression("sub", new Expression[]{a, v.end()});
                            Expression end = new Expression("sub", new Expression[]{a, v.begin()});
                            return new Variable(begin, end);
                        }
                    }
                }
        }
        throw new ParseCancellationException("unsupport replace range");
    }

    static Expression replace(Expression e, Expression r, String s) {
        if (equal(e, r)) {
            return new Expression(s);
        }
        if (e.type() != ExprType.OP) {
            return e;
        }
        String op = e.asOp().op();
        String var = e.asOp().var();
        Variable range = e.asOp().range();
        Expression[] args = e.asOp().args();
        if (var != null) {
            if (r.type() != ExprType.ID) {
                throw new ParseCancellationException("can't replace loop variable with expr");
            }
            else {
                var = r.asId();
            }
        }
        if (range != null) {
            Expression begin = replace(range.begin(), r, s);
            Expression end = replace(range.end(), r, s);
            range = new Variable(begin, end);
        }
        for (int i = 0; i < args.length; ++i) {
            args[i] = replace(args[i], r, s);
        }
        return new Expression(op, var, range, args);
    }

    static Expression replace(Expression e, String s, Expression r) {
        switch (e.type()) {
            case INT:
                return e;
            case FLOAT:
                return e;
            case ID:
                if (e.asId().equals(s)) {
                    return r;
                }
                else {
                    return e;
                }
            case OP:
                String op = e.asOp().op();
                String var = e.asOp().var();
                Variable range = e.asOp().range();
                Expression[] args = e.asOp().args();
                if (var != null && var.equals(s)) {
                    if (r.type() != ExprType.ID) {
                        throw new ParseCancellationException("can't replace loop variable with expr");
                    }
                    else {
                        var = r.asId();
                    }
                }
                if (range != null) {
                    Expression begin = replace(range.begin(), s, r);
                    Expression end = replace(range.end(), s, r);
                    range = new Variable(begin, end);
                }
                for (int i = 0; i < args.length; ++i) {
                    args[i] = replace(args[i], s, r);
                }
                return new Expression(op, var, range, args);
        }
        return null;
    }

    private static boolean equal(Expression a, Expression b) {
        if (a.type() != b.type()) {
            return false;
        }
        switch (a.type()) {
            case INT:
                return a.asInt().equals(b.asInt());
            case FLOAT:
                return a.asFloat().equals(b.asFloat());
            case ID:
                return a.asId().equals(b.asId());
            case OP:
                Operation oa = a.asOp(), ob = b.asOp();
                if (!oa.op().equals(ob.op())) {
                    return false;
                }
                if (oa.var() != null || ob.var() != null) {
                    if (oa.var() == null || ob.var() == null || !oa.var().equals(ob.var())) {
                        return false;
                    }
                    if (!equal(oa.range().begin(), ob.range().begin()) || !equal(oa.range().end(), ob.range().end())) {
                        return false;
                    }
                }
                if (oa.args().length != ob.args().length) {
                    return false;
                }
                for (int i = 0; i < oa.args().length; ++i) {
                    if (!equal(oa.arg(i), ob.arg(i))) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    static Float floatValue(Expression e) {
        switch (e.type()) {
            case INT:
                return e.asInt().floatValue();
            case FLOAT:
                return e.asFloat();
            case ID:
                switch (Context.getType(e.asId())) {
                    case INT:
                        return Context.getInt(e.asId()).floatValue();
                    case FLOAT:
                        return Context.getFloat(e.asId());
                    default:
                        return null;
                }
            case OP:
                Operation op = e.asOp();
                switch (op.op()) {
                    case "add": {
                        Float f1 = floatValue(op.arg(0));
                        Float f2 = floatValue(op.arg(1));
                        if (f1 == null || f2 == null) {
                            return null;
                        }
                        return f1 + f2;
                    }
                    case "sub": {
                        Float f1 = floatValue(op.arg(0));
                        Float f2 = floatValue(op.arg(1));
                        if (f1 == null || f2 == null) {
                            return null;
                        }
                        return f1 - f2;
                    }
                    case "mul": {
                        Float f1 = floatValue(op.arg(0));
                        Float f2 = floatValue(op.arg(1));
                        if (f1 == null || f2 == null) {
                            return null;
                        }
                        return f1 * f2;
                    }
                    case "div": {
                        Float f1 = floatValue(op.arg(0));
                        Float f2 = floatValue(op.arg(1));
                        if (f1 == null || f2 == null) {
                            return null;
                        }
                        return f1 / f2;
                    }
                    case "neg": {
                        Float f = floatValue(op.arg(0));
                        if (f == null) {
                            return null;
                        }
                        return -f;
                    }
                    default:
                        throw new ParseCancellationException("unsupported argument");
                }
            default:
                return null;
        }
    }

    private static int count = 0;
    public static String tmpStr() {
        count += 1;
        return "T_" + count;
    }
}
