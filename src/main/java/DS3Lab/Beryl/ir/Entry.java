package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.frontend.Utils;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.EntryType;

public class Entry {
    EntryType type;
    String name;
    String[] index;
    Range[] range;
    DataType dtype;
    Value[] shape;
    int[] min, max;

    public String name() {
        return name;
    }

    public String idx(int i) {
        return index[i];
    }

    public String[] idx() {
        return index;
    }

    public Const index(int i) {
        return Context.getConst(index[i]);
    }

    public int indexNum() {
        return index.length;
    }

    public void setIndexNum(int num) {
        this.index = new String[num];
        for (int i = 0; i < num; ++i) {
            index[i] = Utils.tmpStr();
            Context.getConst(index[i]);
        }
        min = new int[index.length];
        max = new int[index.length];
        for (int j = 0; j < indexNum(); ++j) {
            min[j] = Integer.MAX_VALUE;
            max[j] = Integer.MIN_VALUE;
        }
    }

    public EntryType type() {
        return type;
    }

    public DataType dtype() {
        return dtype;
    }

    public Value[] shape() {
        return shape;
    }

    public Value shape(int i) {
        return shape[i];
    }

    public Const asConst() {
        return (Const)this;
    }

    public Schema asSchema() {
        return (Schema)this;
    }

    public View asView() {
        return (View)this;
    }

    public int min(int i) {
        return min[i];
    }

    public int max(int i) {
        return max[i];
    }
}
