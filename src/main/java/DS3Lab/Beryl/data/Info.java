package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.DataType;

public class Info {
    private DataType dtype;
    private int[] shape;
    private int size;

    public Info(DataType dtype, int[] shape) {
        this.dtype = dtype;
        this.shape = shape;
        this.size = 1;
        for (int s : shape) {
            size *= s;
        }
    }

    public int dim() {
        return shape.length;
    }

    public DataType dtype() {
        return dtype;
    }

    public int[] shape() {
        return shape;
    }

    public int shape(int i) {
        return shape[i];
    }

    public int size() {
        return size;
    }
}

