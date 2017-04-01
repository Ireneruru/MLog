package DS3Lab.Beryl.ir;

public class Assign {
    private Range[] range;
    private Value value;

    public Assign(Range[] range, Value value) {
        this.range = range;
        this.value = value;
    }

    public int indexNum() {
        return range().length;
    }

    public Range[] range() {
        return range;
    }

    public Range range(int i) {
        return range[i];
    }

    public Value value() {
        return value;
    }
}
