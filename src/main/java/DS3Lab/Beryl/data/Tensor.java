package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.DataType;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Tensor {
    private DataType dtype;
    private int[] shape;
    private int size;
    private byte[] content;

    public Tensor(DataType dtype, int[] shape, byte[] content) {
        this.dtype = dtype;
        this.shape = shape;
        this.size = 1;
        for (int i : shape) {
            this.size *= i;
        }
        this.content = content;
    }

    public Tensor subTensor(int[] index) {
        if (index.length == 0) {
            return this;
        }
        int bias = 0, base = size;
        for (int i = 0; i < index.length; ++i) {
            base /= shape[i];
            bias += index[i] * base;
        }
        if (content == null) {
            return new Tensor(dtype,
                    Arrays.copyOfRange(shape, index.length, shape.length),
                    null);
        }
        else {
            return new Tensor(dtype,
                    Arrays.copyOfRange(shape, index.length, shape.length),
                    Arrays.copyOfRange(content, bias * 4, (bias + base) * 4));
        }
    }

    public static Tensor batch(Tensor[] t) {
        DataType dtype = t[0].dtype;
        int[] shape = new int[t[0].dim() + 1];
        shape[0] = t.length;
        for (int i = 1; i < shape.length; ++i) {
            shape[i] = t[0].shape[i - 1];
        }
        int size = 0;
        for (Tensor x : t) {
            size += x.content.length;
        }
        ByteBuffer buf = ByteBuffer.allocate(size);
        for (Tensor x : t) {
            buf.put(x.content);
        }
        return new Tensor(dtype, shape, buf.array());
    }

    public DataType dtype() {
        return this.dtype;
    }

    public int[] shape() {
        return shape;
    }

    public int shape(int index) {
        return shape[index];
    }

    public int dim() {
        return shape.length;
    }

    public int size() {
        return size;
    }

    public byte[] content() {
        return content;
    }
}
