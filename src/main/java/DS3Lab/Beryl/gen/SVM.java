package DS3Lab.Beryl.gen;

import DS3Lab.Beryl.data.FileSource;
import DS3Lab.Beryl.data.Tensor;
import DS3Lab.Beryl.data.TensorSet;
import DS3Lab.Beryl.type.DataType;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class SVM {
    public static void main(String[] args) throws Exception {
        Scanner reader = new Scanner(new FileInputStream(new File("../experiment/epsilon/epsilon_normalized")));
        ArrayList<TensorSet> X = new ArrayList<>();
        ArrayList<TensorSet> Y = new ArrayList<>();
        int n = 0;
        while (reader.hasNextLine()) {
            n += 1;
            System.out.println(n);
            String line = reader.nextLine();
            String[] c = line.split(" ");
            float y = Float.valueOf(c[0]);
            float[] x = new float[2000];
            for (int i = 1; i < c.length; ++i) {
                String[] v = c[i].split(":");
                x[Integer.valueOf(v[0])-1] = Float.valueOf(v[1]);
            }
            ByteBuffer buf_x = ByteBuffer.allocate(2000*4);
            ByteBuffer buf_y = ByteBuffer.allocate(4);
            buf_y.putFloat(y);
            for (int i = 0; i < x.length; ++i) {
                buf_x.putFloat(x[i]);
            }
            X.add(new TensorSet(new Tensor(DataType.FLOAT, new int[]{2000}, buf_x.array())));
            Y.add(new TensorSet(new Tensor(DataType.FLOAT, new int[0], buf_y.array())));
            if (n >= 20000) {
                break;
            }
        }
        HashMap<String, TensorSet> data = new HashMap<>();
        data.put("X", new TensorSet(X.toArray(new TensorSet[0])));
        data.put("Y", new TensorSet(Y.toArray(new TensorSet[0])));
        FileSource.createFile("epsilon.data", data);
    }
}
