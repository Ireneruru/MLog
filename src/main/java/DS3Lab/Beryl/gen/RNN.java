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

public class RNN {
    public static void main(String[] args) throws Exception {
        Scanner reader = new Scanner(new FileInputStream(new File("../experiment/imdb/data/imdb_new")));
        ArrayList<TensorSet> Y = new ArrayList<>();
        ArrayList<TensorSet> X = new ArrayList<>();
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            String label = line.substring(0, line.indexOf(':') - 1);
            String data = line.substring(line.indexOf(':') + 2);
            String[] words = data.split(" ");
            ArrayList<TensorSet> Xi = new ArrayList<>();
            int y = Integer.valueOf(label);
            if (y < 0) y = 0;
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(y);
            Y.add(new TensorSet(new Tensor(DataType.INT, new int[0], buf.array())));
            for (String w : words) {
                Integer x = Integer.valueOf(w);
                if (x == null) continue;
                buf = ByteBuffer.allocate(4);
                buf.putInt(x);
                Xi.add(new TensorSet(new Tensor(DataType.INT, new int[0], buf.array())));
                if (Xi.size() > 100) {
                    break;
                }
            }
            X.add(new TensorSet(Xi.toArray(new TensorSet[0])));
        }
        HashMap<String, TensorSet> data = new HashMap<>();
        data.put("X", new TensorSet(X.toArray(new TensorSet[0])));
        data.put("Y", new TensorSet(Y.toArray(new TensorSet[0])));
        FileSource.createFile("imdb.data.new", data);
    }
}
