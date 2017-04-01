package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.type.EntryType;

import java.util.ArrayList;

public class View extends Entry {

    private ArrayList<Assign> assigns = new ArrayList<>();

    public View(String name, int num) {
        this.name = name;
        this.type = EntryType.VIEW;
        setIndexNum(num);
        Context.putView(name, this);
    }

    public Entry origin;
    public int batchIndex;

    public View (String name, Entry origin, int batchIndex, int batchSize) {
        this.name = name;
        this.type = EntryType.VIEW;
        this.origin = origin;
        this.batchIndex = batchIndex;
        setIndexNum(origin.indexNum() - 1);
        this.dtype = origin.dtype();
        this.shape = new Value[origin.shape().length + 1];
        this.shape[0] = new Value(batchSize);
        System.arraycopy(origin.shape(), 0, this.shape, 1, origin.shape().length);
        Context.putView(this.name(), this);
    }

    public void putAssign(Assign assign) {
        if (this.dtype == null || this.shape == null) {
            this.dtype = assign.value().dtype();
            this.shape = assign.value().shape();
        }
        assigns.add(assign);
    }

    public Assign[] getAssigns() {
        return assigns.toArray(new Assign[0]);
    }

    public void calcRanges() {
        for (int i = 0; i < index.length; ++i) {
            this.min[i] = Integer.MAX_VALUE;
            this.max[i] = Integer.MIN_VALUE;
        }
        for (Assign ass : assigns) {
            Range[] r = ass.range();
            for (int i = 0; i < indexNum(); ++i) {
                min[i] = Integer.min(min[i], r[i].begin().minVal());
                max[i] = Integer.max(max[i], r[i].end().maxVal());
            }
        }
    }

    public int min(int i) {
        return min[i];
    }

    public int max(int i) {
        return max[i];
    }
}
