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

public class CNN {
    public static void main(String[] args) throws Exception {
        ArrayList<TensorSet> image = new ArrayList<>();
        ArrayList<TensorSet> label = new ArrayList<>();
        int n = 0;
        for (int file = 1; file <= 5; ++file) {
            FileInputStream reader = new FileInputStream(new File("experiment/cifar10/data/data_batch_" + file + ".bin"));
            for (int i = 0; i < 10000; ++i) {
                byte[] img = new byte[32 * 32 * 3];
                byte[] lab = new byte[1];
                reader.read(lab);
                reader.read(img);
                ByteBuffer img_buf = ByteBuffer.allocate(32 * 32 * 3 * 4);
                ByteBuffer lab_buf = ByteBuffer.allocate(4);
                lab_buf.putInt((int) lab[0]);
                for (int x = 0; x < 32 * 32 * 3; ++x) {
                    float v = (float)((img[x] + 256) % 256 / 128.0 - 1.0);
                    img_buf.putFloat(v);
                }
                image.add(new TensorSet(new Tensor(DataType.FLOAT, new int[]{3, 32, 32}, img_buf.array())));
                label.add(new TensorSet(new Tensor(DataType.INT, new int[0], lab_buf.array())));
            }
        }
        HashMap<String, TensorSet> data = new HashMap<>();
        data.put("image", new TensorSet(image.toArray(new TensorSet[0])));
        data.put("label", new TensorSet(label.toArray(new TensorSet[0])));
        FileSource.createFile("cifar10.data", data);
    }
}
