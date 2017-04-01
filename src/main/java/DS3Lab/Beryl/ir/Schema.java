package DS3Lab.Beryl.ir;

import DS3Lab.Beryl.data.FileSource;
import DS3Lab.Beryl.data.Source;
import DS3Lab.Beryl.data.Tensor;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.EntryType;
import DS3Lab.Beryl.type.SourceType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Stack;

public class Schema extends Entry {
    private Range[] range;
    private Source source;

    public Schema(String name, DataType dtype, Value[] shape) {
        this.name = name;
        this.type = EntryType.SCHEMA;
        this.dtype = dtype;
        this.shape = shape;
        Context.putSchema(name, this);
    }

    public void setRange(Range[] range) {
        this.range = range;
    }

    private void parseInfo(int i, Stack<Integer> idx) throws ParseCancellationException {
        Range r = range[i];
        int[] id = new int[idx.size()];
        for (int j = 0; j < id.length; ++j) {
            id[j] = idx.elementAt(j);
        }
        int size = ((FileSource)source).getSize(name, id);
        if (!r.setRange(size)) {
            throw new ParseCancellationException("Set size failed");
        }
        int begin = r.getBegin(), end = r.getEnd();
        min[i] = Integer.min(min[i], begin);
        max[i] = Integer.max(max[i], end);
        if (i < index.length - 1) {
            for (int j = begin; j <= end; ++j) {
                index(i).setValue(j);
                idx.push(j - begin);
                parseInfo(i + 1, idx);
                idx.pop();
            }
            index(i).setValue(null);
        }
    }

    public void setSource(Source source) throws ParseCancellationException {
        this.source = source;
        if (source.type() == SourceType.FUNCTION) {
            return;
        }
        FileSource fs = (FileSource)source;
        Tensor info = fs.getInfo(name);
        if (dtype != info.dtype() || shape.length != info.dim()) {
            throw new ParseCancellationException("file source do not match " + name);
        }
        for (int i = 0; i < shape.length; ++i) {
            if (!shape[i].setValue(info.shape(i))) {
                throw new ParseCancellationException("file source do not match " + name);
            }
        }
        if (index.length > 0) {
            parseInfo(0, new Stack<>());
        }
    }

    public Tensor getData(int[] idx) throws ParseCancellationException {
        if (idx.length != indexNum()) {
            throw new ParseCancellationException("index number do not match");
        }
        if (source.type() != SourceType.FILE) {
            throw new ParseCancellationException("get data from function source");
        }
        int[] tmp = new int[idx.length];
        System.arraycopy(idx, 0, tmp, 0, idx.length);
        for (int i = 0; i < index.length; ++i) {
            tmp[i] = tmp[i] -  range[i].getBegin();
        }
        return ((FileSource)source).readData(name, tmp);
    }

    public Range range(int i) {
        return range[i];
    }

    public Source source() {
        return source;
    }
}
