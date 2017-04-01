package DS3Lab.Beryl.data;

import java.util.Arrays;

public class Index {
    private int[] index;

    public Index(int[] index) {
        this.index = index.clone();
    }

    public int at(int i) {
        return index[i];
    }

    public int[] index() {
        return index;
    }

    public int hashCode() {
        return Arrays.hashCode(index);
    }

    public boolean equals(Object obj) {
        if (obj instanceof  Index) {
            Index idx = (Index)obj;
            if (idx.index.length != index.length) {
                return false;
            }
            for (int i = 0; i < index.length; ++i) {
                if (idx.index[i] != index[i]) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
}
