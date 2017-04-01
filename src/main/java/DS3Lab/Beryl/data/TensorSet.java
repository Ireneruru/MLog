package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.DataType;

public class TensorSet {
    DataType dtype;
    int[] shape;
    TensorSet[] subsets;
    Tensor content;

    public TensorSet(TensorSet[] subsets) {
        this.dtype = subsets[0].dtype;
        this.shape = subsets[0].shape;
        this.subsets = subsets;
        this.content = null;
    }

    public TensorSet(Tensor tensor) {
        this.dtype = tensor.dtype();
        this.shape = tensor.shape();
        this.subsets = null;
        this.content = tensor;
    }

    public DataType dtype() {
        return dtype;
    }

    public int[] shape() {
        return shape;
    }

    public int size() {
        if (subsets != null) {
            return subsets.length;
        }
        return 1;
    }

    public TensorSet subset(int i) {
        return subsets[0];
    }

    public Tensor content() {
        return content;
    }
}
