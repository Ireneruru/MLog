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

public class MC {
    public static void main(String[] args) throws Exception {
        Scanner reader = new Scanner(new FileInputStream(new File("experiment/movielens/ratings.dat")));
        ArrayList<TensorSet> X = new ArrayList<>();
        float[][] mat = new float[6040][3952];
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            String[] c = line.split("::");
            int x = Integer.valueOf(c[0]);
            int y = Integer.valueOf(c[1]);
            float v = Float.valueOf(c[2]);
            mat[x-1][y-1] = v;
        }
        ByteBuffer buf = ByteBuffer.allocate(6040*3952*4);
        for (int i = 0; i < 6040; ++i)
            for (int j = 0; j < 3952; ++j)
                buf.putFloat(mat[i][j]);
        HashMap<String, TensorSet> data = new HashMap<>();
        data.put("V", new TensorSet(new Tensor(DataType.FLOAT, new int[]{6040,3952}, buf.array())));
        FileSource.createFile("movielens.data", data);
    }
}
