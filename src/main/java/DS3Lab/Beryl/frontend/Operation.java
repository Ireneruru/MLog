package DS3Lab.Beryl.frontend;

class Operation {
    private String op;
    private String var;
    private Variable range;
    private Expression[] args;

    Operation(String op, String var, Variable range, Expression[] args) {
        this.op = op;
        this.var = var;
        this.range = range;
        this.args = args;
    }

    String op() {
        return op;
    }

    String var() {
        return var;
    }

    Variable range() {
        return range;
    }

    Expression[] args() {
        return args;
    }

    Expression arg(int i) {
        return args[i];
    }
}
