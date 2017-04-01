package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.SourceType;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;

public class FileSource extends Source {
    private RandomAccessFile file;
    private HashMap<Integer, HashMap<Index, Long>> bias = new HashMap<>();
    private HashMap<Integer, HashMap<Index, Integer>> size = new HashMap<>();
    private HashMap<Integer, Info> info = new HashMap<>();

    private static int hash(String s) {
        int base = 137, now = 1;
        int result = 0;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            result += c * now;
            now *= base;
        }
        return result;
    }

    private void readFile(int[] index, Info info, HashMap<Index, Long> bias, HashMap<Index, Integer> size, int num)
            throws ParseCancellationException {
        try {
            if (num == 0) {
                long pos = file.getFilePointer();
                bias.put(new Index(index), pos);
                file.skipBytes(info.size() * 4);
                return;
            }
            int l = file.readInt();
            size.put(new Index(index), l);
            if (index.length == num - 1) {
                long pos = file.getFilePointer();
                bias.put(new Index(index), pos);
                long need_skip = info.size() * (long)l * 4;
                while (need_skip > 0) {
                    if (need_skip > 512 << 20) {
                        file.skipBytes(512 << 20);
                        need_skip -= 512 << 20;
                    }
                    else {
                        file.skipBytes((int)need_skip);
                        need_skip = 0;
                    }
                }
            }
            else {
                int[] next = new int[index.length + 1];
                System.arraycopy(index, 0, next, 0, index.length);
                for (int k = 0; k < l; ++k) {
                    next[index.length] = k;
                    readFile(next, info, bias, size, num);
                }
            }
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    public FileSource(String path) {
        super(SourceType.FILE);
        try {
            file = new RandomAccessFile(new File(path), "rw");
            int total = file.readInt();
            for (int i = 0; i < total; ++i) {
                int key = file.readInt();
                int type = file.readInt();
                DataType dtype = type == 0 ? DataType.INT : DataType.FLOAT;
                int dim = file.readInt();
                int[] shape = new int[dim];
                for (int j = 0; j < dim; ++j) {
                    shape[j] = file.readInt();
                }
                HashMap<Index, Long> bias = new HashMap<>();
                HashMap<Index, Integer> size = new HashMap<>();
                Info info = new Info(dtype, shape);
                this.bias.put(key, bias);
                this.info.put(key, info);
                this.size.put(key, size);

                int indexNum = file.readInt();
                readFile(new int[0], info, bias, size, indexNum);
            }
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    public Tensor readData(String name, int[] index) {
        int code = hash(name);
        if (!bias.containsKey(code)) {
            throw new ParseCancellationException("Tensor Set " + name + " does not exist");
        }
        int[] idx = Arrays.copyOfRange(index, 0, Integer.max(0, index.length - 1));
        Index id = new Index(idx);
        if (!bias.get(code).containsKey(id)) {
            return null;
        }
        int last = 0;
        if (index.length > 0) {
            last = index[index.length - 1];
        }
        Info info = this.info.get(code);
        byte[] content = new byte[info.size() * 4];
        try {
            file.seek(bias.get(code).get(id) + (long)last * info.size() * 4);
            file.read(content);
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
        return new Tensor(info.dtype(), info.shape(), content);
    }

    public Tensor getInfo(String name) {
        int code = hash(name);
        if (!this.info.containsKey(code)) {
            throw new ParseCancellationException("Tensor Set " + name + " does not exist");
        }
        Info info = this.info.get(code);
        return new Tensor(info.dtype(), info.shape(), null);
    }

    public int getSize(String name, int[] index) {
        int code = hash(name);
        if (!size.containsKey(code)) {
            throw new ParseCancellationException("Tensor Set " + name + " does not exist");
        }
        Index id = new Index(index);
        if (!size.get(code).containsKey(id)) {
            throw new ParseCancellationException(name + "'s index out of range");
        }
        return size.get(code).get(id);
    }

    private static void writeContent(RandomAccessFile file, TensorSet set) throws Exception {
        if (set.subsets == null) {
            file.write(set.content.content());
        }
        else {
            file.writeInt(set.size());
            for (TensorSet s : set.subsets) {
                writeContent(file, s);
            }
        }
    }

    public static void createFile(String path, HashMap<String, TensorSet> data) throws Exception {
        RandomAccessFile file = new RandomAccessFile(new File(path), "rw");
        file.writeInt(data.size());
        for (String name : data.keySet()) {
            TensorSet set = data.get(name);
            int code = hash(name);
            file.writeInt(code);
            file.writeInt(set.dtype == DataType.INT ? 0 : 1);
            file.writeInt(set.shape.length);
            for (int i = 0; i < set.shape.length; ++i) {
                file.writeInt(set.shape()[i]);
            }
            int indexNum = 0;
            TensorSet tmp = set;
            while (tmp.content == null) {
                indexNum++;
                tmp = tmp.subset(0);
            }
            file.writeInt(indexNum);
            writeContent(file, set);
        }
        file.close();
    }
}
