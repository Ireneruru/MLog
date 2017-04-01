package DS3Lab.Beryl.target;

import DS3Lab.Beryl.ir.Range;
import DS3Lab.Beryl.ir.Value;
import DS3Lab.Beryl.type.DataType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Operator {
    public static Value[] shape(String op, Range var, Value[] args) {
        Value[] ret;
        int max = 0;
        switch (op) {
            case "HWC":
                return args[0].shape();
            case "select":
                return args[1].shape();
            case "extract":
                ret = new Value[args[0].shape().length - 1];
                System.arraycopy(args[0].shape(), 1, ret, 0, ret.length);
                break;
            case "concat":
                ret = new Value[args[0].shape().length + 1];
                ret[0] = new Value("sub", new Value[]{var.end(), var.begin()});
                System.arraycopy(args[0].shape(), 0, ret, 1, ret.length - 1);
                break;
            case "add":
            case "sub":
            case "mul":
            case "div":
            case "and":
            case "ge":
            case "le":
                for (int i = 1; i < args.length; ++i) {
                    if (args[i].shape().length > args[max].shape().length) {
                        max = i;
                    }
                }
                return args[max].shape();
            case "matmul":
                Value[] a = args[0].shape();
                Value[] b = args[1].shape();
                if (a.length == 1 && b.length == 1) {
                    return new Value[0];
                }
                if (a.length == 1) {
                    return new Value[]{b[1]};
                }
                if (b.length == 1) {
                    return new Value[]{a[0]};
                }
                ret = new Value[2];
                ret[0] = a[0];
                ret[1] = b[1];
                break;
            case "neg":
                return args[0].shape();
            case "trans":
                ret = new Value[2];
                ret[0] = args[0].shape()[1];
                ret[1] = args[0].shape()[0];
                break;
            case "sum":
                return args[0].shape();
            case "Rsum":
                ret = new Value[0];
                break;
            case "Sqr":
                return args[0].shape();
            case "Sigmoid":
                return args[0].shape();
            case "Tanh":
                return args[0].shape();
            case "CrossEntropy":
                ret = new Value[0];
                break;
            case "Conv2d":
                return args[0].shape();
            case "MaxPool":
                ret = new Value[args[0].shape().length];
                System.arraycopy(args[0].shape(), 0, ret, 0, ret.length);
                ret[1] = null;
                ret[2] = null;
                return ret;
            case "ReLU":
                return args[0].shape();
            case "DropOut":
                return args[0].shape();
            case "Flatten":
                return new Value[] {null};
            case "OneHot":
                ret = new Value[args[0].shape().length + 1];
                System.arraycopy(args[0].shape(), 0, ret, 0, ret.length - 1);
                ret[ret.length - 1] = null;
                return ret;
            case "Softmax":
                return args[0].shape();
            case "Hinge":
                return new Value[0];
            default:
                throw new ParseCancellationException("unsupported operator " + op);
        }
        return ret;
    }

    public static DataType dtype(String op, Value[] args) {
        switch (op) {
            case "select":
                return args[1].dtype();
            case "extract":
            case "concat":
            case "add":
            case "and":
            case "ge":
            case "le":
            case "sub":
            case "mul":
            case "div":
            case "matmul":
            case "neg":
            case "trans":
            case "sum":
            case "Rsum":
            case "Sqr":
                return args[0].dtype();
            case "Sigmoid":
            case "Tanh":
            case "CrossEntropy":
            case "Conv2d":
                return DataType.FLOAT;
            case "HWC":
            case "MaxPool":
            case "ReLU":
            case "DropOut":
            case "Flatten":
                return args[0].dtype();
            case "OneHot":
                return DataType.INT;
            case "Softmax":
            case "Hinge":
                return DataType.FLOAT;
            default:
                throw new ParseCancellationException("unsupported operator " + op);
        }
    }

    private static String pr(String format, Object... args) {
        return String.format(format, args);
    }

    static String code(String op, String[] args) {
        switch (op) {
            case "and":
                return pr("tf.logical_and(%s, %s)", args[0], args[1]);
            case "ge":
                return pr("tf.greater_equal(%s, %s)", args[0], args[1]);
            case "le":
                return pr("tf.less_equal(%s, %s)", args[0], args[1]);
            case "select":
                return pr("tf.select(%s, %s, %s)", args[0], args[1], args[2]);
            case "extract":
                return pr("tf.gather(%s, %s)", args[0], args[1]);
            case "concat":
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (String s : args) {
                    sb.append(s);
                    sb.append(",");
                }
                sb.append("]");
                return pr("tf.pack(%s, 0)", sb.toString());
            case "add":
                return pr("tf.add(%s, %s)", args[0], args[1]);
            case "sub":
                return pr("tf.sub(%s, %s)", args[0], args[1]);
            case "mul":
                return pr("tf.mul(%s, %s)", args[0], args[1]);
            case "div":
                return pr("tf.div(%s, %s)", args[0], args[1]);
            case "matmul":
                return pr("tf.matmul(%s, %s)", args[0], args[1]);
            case "neg":
                return pr("tf.neg(%s)", args[0]);
            case "trans":
                return pr("tf.transpose(%s)", args[0]);
            case "sum":
                StringBuilder sb2 = new StringBuilder();
                sb2.append("[");
                for (String s : args) {
                    sb2.append(s);
                    sb2.append(",");
                }
                sb2.append("]");
                return pr("tf.add_n(%s)", sb2.toString());
            case "Rsum":
                return pr("tf.reduce_sum(%s)", args[0]);
            case "Sqr":
                return pr("tf.square(%s)", args[0]);
            case "Sigmoid":
                return pr("tf.sigmoid(%s)", args[0]);
            case "Tanh":
                return pr("tf.tanh(%s)", args[0]);
            case "CrossEntropy":
                return pr("tf.nn.softmax_cross_entropy_with_logits(%s, %s)", args[0], args[1]);
            case "HWC":
                return pr("tf.transpose(%s, [0,2,3,1])", args[0]);
            case "Conv2d":
                return pr("tf.nn.bias_add(tf.nn.conv2d(%s, %s, [1,1,1,1], padding='SAME'), %s)",
                        args[0], args[1], args[2]);
            case "MaxPool":
                return pr("tf.nn.max_pool(%s, ksize=[1,%s, %s,1], strides=[1,%s,%s,1], padding='SAME')",
                        args[0], args[1], args[1], args[1], args[1]);
            case "ReLU":
                return pr("tf.nn.relu(%s)", args[0]);
            case "DropOut":
                return pr("tf.nn.dropout(%s, %s)", args[0], args[1]);
            case "Flatten":
                return pr("tf.reshape(%s, [100,-1])", args[0]);
            case "OneHot":
                return pr("tf.one_hot(%s, %s)", args[0], args[1]);
            case "Softmax":
                return pr("tf.nn.softmax(%s)", args[0]);
            case "Hinge":
                return pr("tf.maximum(0.0, 1.0-%s)", args[0]);
            default:
                throw new ParseCancellationException("unsupported operator " + op);
        }
    }
}
