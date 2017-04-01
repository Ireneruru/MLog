package DS3Lab.Beryl.ir;

import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Range {
    private Value begin;
    private Value end;

    public Range(Value a, Value b) {
        begin = a;
        end = b;
    }

    public Value begin() {
        return begin;
    }

    public Value end() {
        return end;
    }

    public Integer getBegin() {
        return begin.getValue();
    }

    public Integer getEnd() {
        return end.getValue();
    }

    public Integer getRange() {
        Integer b = getBegin(), e = getEnd();
        if (b == null || e == null) {
            return null;
        }
        return e - b + 1;
    }

    boolean setRange(Integer r) throws ParseCancellationException {
        Integer vb = getBegin(), ve = getEnd();
        if (vb == null && ve == null) {
            return false;
        }
        if (vb == null) {
            begin.setValue(ve - r + 1);
        }
        if (ve == null) {
            end.setValue(vb + r - 1);
        }
        return r.equals(getRange());
    }
}
