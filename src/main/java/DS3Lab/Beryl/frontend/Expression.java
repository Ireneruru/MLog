package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.ir.*;
import DS3Lab.Beryl.type.ExprType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Expression {
    private ExprType type;
    private Object value;

    public Expression(Integer i) {
        type = ExprType.INT;
        value = i;
    }

    public Expression(Float f) {
        type = ExprType.FLOAT;
        value = f;
    }

    public Expression(String id) {
        type = ExprType.ID;
        value = id;
    }

    public Expression(String op, Expression[] args) {
        type = ExprType.OP;
        value = new Operation(op, null, null, args);
    }

    public Expression(String op, String var, Variable range, Expression[] e) {
        type = ExprType.OP;
        value = new Operation(op, var, range, e);
    }

    ExprType type() {
        return type;
    }

    boolean isInt() {
        return type == ExprType.INT;
    }

    boolean isFloat() {
        return type == ExprType.FLOAT;
    }

    boolean isOp() {
        return type == ExprType.OP;
    }

    Integer asInt() {
        return (Integer)value;
    }

    Float asFloat() {
        return (Float)value;
    }

    String asId() {
        return (String) value;
    }

    Operation asOp() {
        return (Operation)value;
    }

    Value toConst() {
        switch (type) {
            case INT:
                return new Value(asInt());
            case FLOAT:
                throw new ParseCancellationException("need integer but get float");
            case ID:
                return new Value(Context.getConst(asId()), new Value[0]);
            case OP:
                Operation op = asOp();
                if (op.var() != null) {
                    throw new ParseCancellationException("const expression don't have loop");
                }
                switch (op.op()) {
                    case "add":
                    case "sub":
                        return new Value(op.op(), new Value[]{op.arg(0).toConst(), op.arg(1).toConst()});
                    case "extr": {
                        String id = op.arg(0).asId();
                        Const c = Context.getConst(id);
                        Value[] idx = new Value[op.args().length - 1];
                        for (int i = 0; i < idx.length; ++i) {
                            idx[i] = op.arg(i + 1).toConst();
                        }
                        return new Value(c, idx);
                    }
                    default:
                        throw new ParseCancellationException("illegal operator " + op.op());
                }
            default:
                return null;
        }
    }

    Value toValue() {
        switch (type) {
            case INT:
                return new Value(asInt());
            case FLOAT:
                return new Value(asFloat());
            case ID:
                switch (Context.getType(asId())) {
                    case SCHEMA:
                        return new Value(Context.getSchema(asId()), new Value[0]);
                    case VIEW:
                        return new Value(Context.getView(asId()), new Value[0]);
                    case INT:
                        return new Value(Context.getConst(asId()), new Value[0]);
                }
                return null;
            case OP: {
                String op = asOp().op();
                String var = asOp().var();
                Variable rng = asOp().range();
                Expression[] args = asOp().args();
                if (var != null) {
                    String nvar = Utils.tmpStr();
                    for (int i = 0; i < args.length; ++i) {
                        args[i] = Utils.replace(args[i], var, new Expression(nvar));
                    }
                    var = nvar;
                    Context.getConst(var);
                }
                if (!op.equals("extr")) {
                    Value[] argv = new Value[args.length];
                    for (int i = 0; i < argv.length; ++i) {
                        argv[i] = args[i].toValue();
                    }
                    if (var == null) {
                        return new Value(op, argv);
                    }
                    else {
                        Range r = new Range(rng.begin().toConst(), rng.end().toConst());
                        return new Value(Context.getConst(var), r, op, argv[0]);
                    }
                }
                else {
                    if (args[0].type() != ExprType.ID) {
                        throw new ParseCancellationException("illegal extr arg[0]");
                    }
                    String target = args[0].asId();
                    int indexNum;
                    Entry entry;
                    switch (Context.getType(target)) {
                        case SCHEMA: {
                            entry = Context.getSchema(target);
                            break;
                        }
                        case VIEW: {
                            entry = Context.getView(target);
                            break;
                        }
                        case INT: {
                            entry = Context.getConst(target);
                            break;
                        }
                        default:
                            throw new ParseCancellationException("Unrecognized id " + target);
                    }
                    indexNum = entry.indexNum();
                    if (indexNum > args.length - 1) {
                        throw new ParseCancellationException("need more index");
                    }
                    if (indexNum < args.length - 1) {
                        Expression[] largs = new Expression[args.length - 1];
                        System.arraycopy(args, 0, largs, 0, args.length - 1);
                        Expression lexpr = new Expression("extr", largs);
                        Expression rexpr = args[args.length - 1];
                        return (new Expression("extract", new Expression[]{lexpr, rexpr})).toValue();
                    }
                    else {
                        Value[] index = new Value[indexNum];
                        for (int i = 0; i < indexNum; ++i) {
                            index[i] = args[i + 1].toValue();
                        }
                        return new Value(entry, index);
                    }
                }
            }
        }
        return null;
    }

    boolean relate(String var) {
        switch (type) {
            case INT:
                return false;
            case FLOAT:
                return false;
            case ID:
                return var.equals(asId());
            case OP:
                for (Expression a : asOp().args()) {
                    if (a.relate(var)) {
                        return true;
                    }
                }
        }
        return false;
    }
}
