package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.data.Tensor;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.EntryType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Const extends Entry {
    private int minVal, maxVal;
    private HashMap<List<Integer>, Integer> value = new HashMap<>();

    Const(String name) {
        this.name = name;
        this.type = EntryType.CONST;
        dtype = DataType.INT;
        shape = new Value[0];
        minVal = Integer.MAX_VALUE;
        maxVal = Integer.MIN_VALUE;
    }

    public void setValue(Integer i) {
        if (index == null) {
            setIndexNum(0);
        }
        else if (indexNum() != 0) {
            throw new ParseCancellationException("Const " + name + " has " + indexNum() + " index");
        }
        value.put(new ArrayList<>(), i);
        if (i == null) {
            minVal = Integer.MAX_VALUE;
            maxVal = Integer.MIN_VALUE;
        }
        else {
            minVal = i;
            maxVal = i;
        }
    }

    void setValue(Integer[] idx, Integer i) {
        if (index == null) {
            setIndexNum(idx.length);
        }
        else if (indexNum() != idx.length) {
            throw new ParseCancellationException("Const " + name + " has " + indexNum() + " index");
        }
        for (int j = 0; j < indexNum(); ++j) {
            min[j] = Integer.min(min[j], idx[j]);
            max[j] = Integer.max(max[j], idx[j]);
        }
        value.put(Arrays.asList(idx), i);
        minVal = Integer.min(minVal, i);
        maxVal = Integer.max(maxVal, i);
    }

    public Integer getValue() {
        return value.get(new ArrayList<Integer>());
    }

    public Integer getValue(Integer[] index) {
        return value.get(Arrays.asList(index));
    }

    int minVal() {
        return minVal;
    }

    int maxVal() {
        return maxVal;
    }

    private void get(ByteBuffer buf, Integer[] idx, int p, int[] shape, Integer opt, int begin, int end) {
        if (p == shape.length) {
            Integer v = value.get(idx);
            if (v == null) {
                v = 0;
            }
            buf.putInt(v);
        }
        else {
            if (p == opt) {
                for (int i = begin; i <= end; ++i) {
                    idx[p] = i;
                    get(buf, idx, p + 1, shape, opt, begin, end);
                }
            }
            else {
                for (int i = 0; i < shape[p]; ++i) {
                    idx[p] = i;
                    get(buf, idx, p + 1, shape, opt, begin, end);
                }
            }
        }
    }

    public Tensor toTensor(Integer opt, int begin, int end) {
        int size = 1;
        int[] shape = new int[indexNum()];
        for (int i = 0; i < shape.length; ++i) {
            if (opt != null && opt == i) {
                shape[i] = end - begin + 1;
            }
            else {
                shape[i] = max(i) + 1;
            }
            size *= shape[i];
        }
        ByteBuffer buf = ByteBuffer.allocate(size * 4);
        Integer[] idx = new Integer[shape.length];
        get(buf, idx, 0, shape, opt, begin, end);
        return new Tensor(DataType.INT, shape, buf.array());
    }
}

