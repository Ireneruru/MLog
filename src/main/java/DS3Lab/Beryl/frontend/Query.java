package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.type.ExprType;
import DS3Lab.Beryl.type.TaskType;

import java.util.ArrayList;

public class Query {
    class Optimize {
        TaskType type;
        ArrayList<String> args;
        public Optimize(TaskType type, ArrayList<String> args) {
            this.type = type;
            this.args = args;
        }
    }
    ArrayList<Optimize> opts;
    String target;

    public Query(Expression expr) {
        if (expr.type() != ExprType.ID) {
            String t = Utils.tmpStr();
            Assignment g = new Assignment(t);
            g.setIndex(new ArrayList<>());
            g.setValue(expr);
            g.finish();
            expr = new Expression(t);
        }
        this.target = expr.asId();
        this.opts = new ArrayList<>();
    }

    public Query(TaskType type, ArrayList<String> args, Query sub) {
        target = sub.target();
        opts = sub.opts;
        if (opts.size() > 0 && type == opts.get(opts.size() - 1).type) {
            opts.get(opts.size() - 1).args.addAll(args);
        }
        else {
            opts.add(new Optimize(type, args));
        }
    }

    public String target() {
        return target;
    }

    public int optLevel() {
        if (opts == null) {
            return 0;
        }
        return opts.size();
    }

    public TaskType type(int i) {
        return opts.get(i).type;
    }

    public ArrayList<String> args(int i) {
        return opts.get(i).args;
    }
}
