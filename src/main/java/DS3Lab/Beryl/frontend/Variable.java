package DS3Lab.Beryl.frontend;

public class Variable {
    private Expression begin;
    private Expression end;

    public Variable(Expression begin, Expression end) {
        this.begin = begin;
        this.end = end;
    }

    public Expression begin() {
        return begin;
    }

    public Expression end() {
        return end;
    }
}
