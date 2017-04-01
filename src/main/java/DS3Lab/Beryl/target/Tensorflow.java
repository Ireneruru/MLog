package DS3Lab.Beryl.target;

import DS3Lab.Beryl.data.FuncSource;
import DS3Lab.Beryl.data.Index;
import DS3Lab.Beryl.data.Tensor;
import DS3Lab.Beryl.frontend.Query;
import DS3Lab.Beryl.ir.*;
import DS3Lab.Beryl.type.DataType;
import DS3Lab.Beryl.type.SourceType;
import DS3Lab.Beryl.type.SymbolType;
import DS3Lab.Beryl.type.TaskType;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.zeromq.ZMQ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.*;

public class Tensorflow {
    private static Random rand = new Random();
    private Integer port;
    private BufferedWriter file;

    private Task task;
    private Target target;
    private String eval;
    private HashSet<String> first = new HashSet<>();
    private HashSet<String> loop = new HashSet<>();

    private static String getFilename() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            int number = rand.nextInt(base.length());
            buf.append(base.charAt(number));
        }
        buf.append(".py");
        return buf.toString();
    }

    private void init() throws ParseCancellationException {
        try {
            String filename = getFilename();
            //this.file = new BufferedWriter(new FileWriter("/tmp/" + filename));
            //this.port = rand.nextInt(60000) + 2000;
            this.file = new BufferedWriter(new FileWriter("code.py"));
            this.port = 50000;
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void receiveTensor() throws ParseCancellationException {
        try {
            file.write("def _recv_tensor_():\n");
            file.write("\tdata = socket.recv()\n");
            file.write("\tsocket.send(b\"\\0\")\n");
            file.write("\tp = [0]\n");
            file.write("\tdef get(i, n):\n");
            file.write("\t\tif n == 0:\n");
            file.write("\t\t\treturn ()\n");
            file.write("\t\tf = \"!%d%s\" % (n, i)\n");
            file.write("\t\tr = struct.unpack(f, data[p[0]:p[0]+4*n])\n");
            file.write("\t\tp[0] += 4 * n\n");
            file.write("\t\treturn r\n");
            file.write("\tdata_map = dict()\n");
            file.write("\ttotal = get('i', 1)[0]\n");
            file.write("\twhile total > 0:\n");
            file.write("\t\ttotal -= 1\n");
            file.write("\t\tcode, idn, dtype, dim = get('i', 4)\n");
            file.write("\t\tdtype = \"if\"[dtype]\n");
            file.write("\t\tshape = get('i', dim)\n");
            file.write("\t\tsize = reduce(lambda x,y:x*y, shape + (1,))\n");
            file.write("\t\tnum = get('i', 1)[0]\n");
            file.write("\t\twhile num > 0:\n");
            file.write("\t\t\tnum -= 1\n");
            file.write("\t\t\tidx = get('i', idn)\n");
            file.write("\t\t\tt = np.array(get(dtype, size), dtype={'i':np.int32,'f':np.float32}[dtype]).reshape(shape)\n");
            file.write("\t\t\tdata_map[(code&0xffffffff,)+idx] = t\n");
            file.write("\treturn data_map\n");
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void sendTensor() throws ParseCancellationException {
        try {
            file.write("def send_tensor(name, index, value):\n");
            file.write("\tpass\n");
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void hash() {
        try {
            file.write("def _hash_(s):\n");
            file.write("\tbase = 137\n");
            file.write("\tnow = 1\n");
            file.write("\tcode = 0\n");
            file.write("\tfor c in s:\n");
            file.write("\t\tcode += ord(c) * now\n");
            file.write("\t\tcode &= 0xffffffff\n");
            file.write("\t\tnow *= base\n");
            file.write("\treturn code\n");
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void zero() {
        try {
            file.write("_zero_map_ = dict()\n");
            file.write("def _zeros_(s):\n");
            file.write("\tif not _zero_map_.has_key(s):\n");
            file.write("\t\tif s[-1] == 0:\n");
            file.write("\t\t\tdtype = tf.int32\n");
            file.write("\t\telse:\n");
            file.write("\t\t\tdtype = tf.float32\n");
            file.write("\t\tshape = s[0:-1]\n");
            file.write("\t\tif len(shape) == 3:\n");
            file.write("\t\t\tshape = (shape[0],shape[2])\n");
            file.write("\t\t_zero_map_[s] = tf.zeros(shape, dtype)\n");
            file.write("\treturn _zero_map_[s]\n");
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void printHeader() throws ParseCancellationException {
        try {
            file.write("##########   Imports   ##########\n");
            file.write("import tensorflow as tf\n");
            file.write("import numpy as np\n");
            file.write("import zmq\n");
            file.write("import struct\n");
            file.write("import sys\n");
            file.write("\n");
            file.write("context = zmq.Context()\n");
            file.write("socket = context.socket(zmq.REP)\n");
            file.write("socket.bind(\"tcp://*:" + port + "\")\n");
            file.write("##########   Util Functions   ##########\n");
            receiveTensor();
            sendTensor();
            hash();
            zero();
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private String arg(int i) {
        String s = "abcdefghijklmnopqrstuvwxyz";
        return "" + s.charAt(i);
    }

    private String argStr(int dim) {
        String s = "";
        for (int i = 0; i < dim; ++i) {
            if (i > 0) {
                s = s + ",";
            }
            s = s + arg(i);
        }
        return s;
    }

    private String pr(String format, Object... args) {
        return String.format(format, args);
    }

    private void printData() throws ParseCancellationException {
        try {
            file.write("##########   Variables   ##########\n");
            file.write("need_feed = []\n");
            file.write(pr("trained = [[]] * %d\n", task.args.size()));
            file.write(pr("varlist = [[]] * %d\n", task.args.size()));
            file.write("global_data = _recv_tensor_()\n");
            for (String s : target.entries.keySet()) {
                if (s.equals("_zeros_")) {
                    continue;
                }
                String ori = s;
                if (Context.getType(s) == SymbolType.VIEW) {
                    View v = Context.getView(s);
                    s = v.origin.name();
                }
                switch (Context.getType(s)) {
                    case INT: {
                        Const c = Context.getConst(s);
                        loop.add(s);
                        String map = s + "_map_";
                        String label = argStr(c.indexNum() - 1);
                        file.write(pr("%s = dict()\n", map));
                        file.write(pr("def %s(%s):\n", ori, label));
                        file.write(pr("\tif not %s.has_key((%s)):\n", map, label));
                        file.write(pr("\t\t_t = tf.placeholder(tf.int32)\n"));
                        file.write(pr("\t\tneed_feed.append(('%s', (%s), _t))\n", s, label + (label.length() > 0 ? "," : "")));
                        file.write(pr("\t\t%s[(%s)] = _t\n", map, label));
                        file.write(pr("\treturn %s[(%s)]\n", map, label));
                        break;
                    }
                    case SCHEMA: {
                        Schema sch = Context.getSchema(s);
                        String map = s + "_map_";
                        String label = argStr(sch.indexNum() - (task.optdim.containsKey(s) ? 1 : 0));
                        file.write(pr("%s = dict()\n", map));
                        file.write(pr("def %s(%s):\n", ori, label));
                        file.write(pr("\tif not %s.has_key((%s)):\n", map, label));
                        if (task.optdim.containsKey(s)) {
                            loop.add(s);
                            if (sch.dtype() == DataType.INT) {
                                file.write(pr("\t\t_t = tf.placeholder(tf.int32)\n"));
                                file.write(pr("\t\tneed_feed.append(('%s', (%s), _t))\n", s, label + (label.length() > 0 ? "," : "")));
                                file.write(pr("\t\t%s[(%s)] = _t\n", map, label));
                            }
                            else {
                                file.write(pr("\t\t_t = tf.placeholder(tf.float32)\n"));
                                file.write(pr("\t\tneed_feed.append(('%s', (%s), _t))\n", s, label + (label.length() > 0 ? "," : "")));
                                file.write(pr("\t\t%s[(%s)] = _t\n", map, label));
                            }
                        }
                        else {
                            int train = -1;
                            for (int i = 0; i < task.args.size(); ++i) {
                                if (task.args.get(i).contains(s)) {
                                    train = i;
                                    break;
                                }
                            }
                            String trainable = train >= 0 ? "trainable=True" : "trainable=False";
                            String dtype = sch.dtype() == DataType.INT ? "dtype=tf.int32" : "dtype=tf.float32";
                            if (sch.source().type() == SourceType.FILE) {
                                first.add(s);
                                file.write(pr("\t\t_t = global_data[(_hash_('%s'),)+(%s)]\n", s, label));
                                file.write(pr("\t\t%s[(%s)] = tf.Variable(_t, %s)\n", map, label, trainable));
                            }
                            else {
                                FuncSource src = (FuncSource)sch.source();
                                String shape = "";
                                for (int i = 0; i < sch.shape().length; ++i) {
                                    if (i > 0) {
                                        shape += ",";
                                    }
                                    shape += sch.shape(i).getValue();
                                }
                                shape = "[" + shape + "]";
                                switch (src.func()) {
                                    case "Zero":
                                        file.write(pr("\t\t%s[(%s)] = tf.Variable(tf.zeros(%s, %s), %s)\n",
                                                map, label, shape, dtype, trainable));
                                        break;
                                    case "Gaussian":
                                        if (src.args().length != 2) {
                                            throw new ParseCancellationException("illegal Gaussian arguments");
                                        }
                                        file.write(pr("\t\t%s[(%s)] = tf.Variable(tf.random_normal(%s, mean=%f, stddev=%f), %s)\n",
                                                map, label, shape, src.args()[0], src.args()[1], trainable));
                                        file.write(pr("\t\ttf.add_to_collection('losses', tf.nn.l2_loss(%s[(%s)]) * 1e-4)\n", map, label));
                                        break;
                                    default:
                                        throw new ParseCancellationException("unsupported function " + src.func());
                                }
                            }
                            if (train >= 0) {
                                file.write(pr("\t\ttrained[%d].append(('%s', (%s), %s[(%s)]))\n", train, s, label, map, label));
                                file.write(pr("\t\tvarlist[%d].append(%s[(%s)])\n", train, map, label));
                            }
                        }
                        file.write(pr("\treturn %s[(%s)]\n", map, label));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private int node = 0;
    private HashMap<Integer, String> nodes = new HashMap<>();

    private String printOp(Node n) {
        try {
            if (nodes.containsKey(n.hash)) {
                return nodes.get(n.hash);
            }
            String name = "T_" + (node++);
            if (n.op != null) {
                String[] args = new String[n.args.length];
                for (int i = 0; i < args.length; ++i) {
                    args[i] = printOp(n.args[i]);
                }
                file.write(pr("%s = %s\n", name, Operator.code(n.op, args)));
            }
            else if (n.entry != null) {
                String index = "";
                for (int i = 0; i < n.index.length; ++i) {
                    if (i > 0) {
                        index += ",";
                    }
                    index += n.index[i];
                }
                if (n.entry.equals("_zeros_")) {
                    file.write(pr("%s = %s((%s))\n", name, n.entry, index + (index.equals("") ? "" : ",")));
                }
                else {
                    file.write(pr("%s = %s(%s)\n", name, n.entry, index));
                }
            }
            else if (n.intVal != null) {
                return n.intVal.toString();
            }
            else {
                return n.floatVal.toString();
            }
            nodes.put(n.hash, name);
            return name;
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void printOperation() throws ParseCancellationException {
        try {
            file.write("##########   Computations   ##########\n");
            eval = printOp(target.target);
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void printTrain() throws ParseCancellationException {
        try {
            file.write("##########   Train   ##########\n");
            file.write(pr("_target_ = %s\n", eval));
            file.write("_sign_ = tf.placeholder(tf.float32)\n");
            file.write("tf.add_to_collection('losses', _target_*_sign_)\n");
            if (task.method.size() > 0) {
                file.write(pr("_train_ = [tf.train.AdamOptimizer(0.01).minimize(tf.add_n(tf.get_collection('losses')), var_list=varlist[i]) for i in range(%d)]\n",
                        task.method.size()));
                if (task.method.get(0) == TaskType.MAXIMIZE) {
                    file.write("_s_ = -1.0\n");
                }
                else {
                    file.write("_s_ = 1.0\n");
                }
            }

            file.write("sess = tf.Session()\n");
            file.write("sess.run(tf.initialize_all_variables())\n");
            file.write("print 'train'\n");
            file.write("sys.stdout.flush()\n");
            file.write("from time import time\n");
            if (task.batch_num > 0) {
                if (task.method.size() > 0) {
                    file.write("_opt_ = 0\n");
                    file.write("for epoch in range(100):\n");
                    file.write(pr("\t_total_ = 0\n"));
                    file.write(pr("\tfor batch_count in range(%d):\n", task.batch_num));
                    file.write("\t\t_a_=time()\n");
                    file.write("\t\tbatch_data = _recv_tensor_()\n");
                    file.write("\t\tfeed_data=dict()\n");
                    file.write("\t\tfor (s, i, t) in need_feed:\n");
                    file.write("\t\t\ttensor = batch_data[(_hash_(s),)+i]\n");
                    file.write("\t\t\tfeed_data[t] = tensor\n");
                    file.write("\t\tfeed_data[_sign_] = _s_\n");
                    file.write("\t\t_b_=time()\n");
                    file.write("\t\t_,_value_ = sess.run([_train_[_opt_], _target_], feed_dict=feed_data)\n");
                    file.write("\t\t_c_=time()\n");
                    file.write("\t\t_total_ += _value_\n");
                    file.write(pr("\t\tprint epoch+1, ':', batch_count, '/', %d, ' :  ',  _value_, 'data', _b_-_a_, 'calc', _c_-_b_\n", task.batch_num));
                    file.write("\t\tsys.stdout.flush()\n");
                    file.write("\tprint epoch+1, ':', _total_\n");
                    file.write("\tsys.stdout.flush()\n");
                    if (task.method.size() > 1) {
                        file.write(pr("\t_opt_ = (_opt_ + 1) %% %d\n", task.method.size()));
                        if (task.method.size() % 2 == 1) {
                            file.write("\tif _opt_ > 0:\n\t\t_s_ *= -1\n");
                        }
                        else {
                            file.write("\t_s_ *= -1\n");
                        }
                    }
                }
                else {
                    file.write(pr("\t_total_ = 0\n"));
                    file.write(pr("\tfor batch_count in range(%d):\n", task.batch_num));
                    file.write("\t\t_a_=time()\n");
                    file.write("\t\tbatch_data = _recv_tensor_()\n");
                    file.write("\t\tfeed_data=dict()\n");
                    file.write("\t\tfor (s, i, t) in need_feed:\n");
                    file.write("\t\t\ttensor = batch_data[(_hash_(s),)+i]\n");
                    file.write("\t\t\tfeed_data[t] = tensor\n");
                    file.write("\t\tfeed_data[_sign_] = _s_\n");
                    file.write("\t\t_b_=time()\n");
                    file.write("\t\t_value_ = sess.run(_target_, feed_dict=feed_data)\n");
                    file.write("\t\t_c_=time()\n");
                    file.write("\t\t_total_ += _value_\n");
                    file.write(pr("\t\tprint epoch+1, ':', batch_count, '/', %d, ' :  ',  _value_, 'data', _b_-_a_, 'calc', _c_-_b_\n", task.batch_num));
                    file.write("\t\tsys.stdout.flush()\n");
                    file.write("\tprint 'total :', _total_\n");
                    file.write("\tsys.stdout.flush()\n");
                }
            }
            else {
                if (task.method.size() > 0) {
                    file.write("_opt_ = 0\n");
                    file.write("for epoch in range(100):\n");
                    file.write("\t_a_=time()\n");
                    file.write("\t_,_value_ = sess.run([_train_[_opt_], _target_], feed_dict={_sign_:_s_})\n");
                    file.write("\t_b_=time()\n");
                    file.write("\tprint epoch, '/ 100', ':  ',  _value_, 'calc', _b_-_a_\n");
                    file.write("\tsys.stdout.flush()\n");
                    if (task.method.size() > 1) {
                        file.write(pr("\t_opt_ = (_opt_ + 1) %% %d\n", task.method.size()));
                        if (task.method.size() % 2 == 1) {
                            file.write("\tif _opt_ > 0:\n\t\t_s_ *= -1\n");
                        }
                        else {
                            file.write("\t_s_ *= -1\n");
                        }
                    }
                }
                else {
                    file.write("\t_value_ = sess.run(_target_, feed_dict={_sign_:_s_})\n");
                    file.write("\tprint _value_\n");
                    file.write("\tsys.stdout.flush()\n");
                }
            }
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void printResult() throws ParseCancellationException {
        try {
            file.write("##########   Send Result   ##########\n");
            file.write("for (s, i, t) in trained:\n");
            file.write("\ttensor = sess.run(t)\n");
            file.write("\tsend_tensor(s, i, tensor)\n");
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    private void genCode() {
        try {
            printHeader();
            printData();
            printOperation();
            printTrain();
            //printResult();
            file.close();
        }
        catch (Exception e) {
            throw new ParseCancellationException(e);
        }
    }

    public Tensorflow(Query query) {
        init();
        this.task = new Task(query);
        this.target = new Target(task.target);
        genCode();
    }

    private class DataSet {
        int name;
        int indexNum;
        int[] shape;
        int dtype;
        HashMap<Index, byte[]> value;
    }

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

    private void sendFirst(ZMQ.Socket sock) {
        HashSet<DataSet> data = new HashSet<>();
        for (String s : first) {
            DataSet set = new DataSet();
            Schema sch = Context.getSchema(s);
            int size = 1;
            set.name = hash(s);
            set.indexNum = sch.indexNum();
            set.dtype = (sch.dtype() == DataType.INT ? 0 : 1);
            set.shape = new int[sch.shape().length];
            for (int i = 0; i < set.shape.length; ++i) {
                set.shape[i] = sch.shape()[i].getValue();
                size *= set.shape[i];
            }
            set.value = new HashMap<>();
            byte[] zero = new byte[size * 4];
            Arrays.fill(zero, (byte)0);
            for (Index idx : target.entries.get(s)) {
                Tensor tensor = sch.getData(idx.index());
                if (tensor != null) {
                    set.value.put(idx, tensor.content());
                }
                else {
                    set.value.put(idx, zero);
                }
            }
            data.add(set);
        }
        sock.send(pack(data), 0);
        sock.recv();
    }

    private void sendBatch(ZMQ.Socket sock) {
        if (task.begin == 0 && task.end == 0) {
            return;
        }
        for (int epoch = 0; epoch < 100; ++epoch) {
            for (int batch = 0; batch < task.batch_num; ++batch) {
                int batch_begin = task.begin + batch * task.batch_size;
                int batch_end = batch_begin + task.batch_size - 1;
                HashSet<DataSet> data = new HashSet<>();
                for (String s : loop) {
                    int opt = task.optdim.get(s);
                    DataSet set = new DataSet();
                    set.name = hash(s);
                    if (Context.getType(s) == SymbolType.INT) {
                        Const c = Context.getConst(s);
                        set.indexNum = c.indexNum() - 1;
                        set.dtype = 0;
                        set.shape = new int[]{task.batch_size};
                        set.value = new HashMap<>();
                        byte[] zero = new byte[4];
                        Arrays.fill(zero, (byte) 0);
                        for (Index idx : target.entries.get("B_" + s)) {
                            byte[] content = new byte[task.batch_size * 4];
                            int point = 0;
                            Integer[] bidx = new Integer[idx.index().length + 1];
                            for (int i = 0, j = 0; i < bidx.length; ++i) {
                                if (i != opt) {
                                    bidx[i] = idx.at(j++);
                                }
                            }
                            for (int i = batch_begin; i <= batch_end; ++i) {
                                bidx[opt] = i;
                                Integer val = c.getValue(bidx);
                                if (val != null) {
                                    ByteBuffer buf = ByteBuffer.allocate(4);
                                    buf.putInt(val);
                                    System.arraycopy(buf.array(), 0, content, point, 4);
                                } else {
                                    System.arraycopy(zero, 0, content, point, 4);
                                }
                                point += 4;
                            }
                            set.value.put(idx, content);
                        }
                    } else {
                        Schema sch = Context.getSchema(s);
                        set.indexNum = sch.indexNum() - 1;
                        set.dtype = (sch.dtype() == DataType.INT ? 0 : 1);
                        set.shape = new int[sch.shape().length + 1];
                        int size = 1;
                        set.shape[0] = task.batch_size;
                        for (int i = 1; i < set.shape.length; ++i) {
                            set.shape[i] = sch.shape()[i - 1].getValue();
                            size *= set.shape[i];
                        }
                        set.value = new HashMap<>();
                        byte[] zero = new byte[size * 4];
                        Arrays.fill(zero, (byte) 0);
                        for (Index idx : target.entries.get("B_" + s)) {
                            byte[] content = new byte[task.batch_size * size * 4];
                            int point = 0;
                            int[] bidx = new int[idx.index().length + 1];
                            for (int i = 0, j = 0; i < bidx.length; ++i) {
                                if (i != opt) {
                                    bidx[i] = idx.at(j++);
                                }
                            }
                            for (int i = batch_begin; i <= batch_end; ++i) {
                                bidx[opt] = i;
                                Tensor tensor = sch.getData(bidx);
                                if (tensor != null) {
                                    System.arraycopy(tensor.content(), 0, content, point, tensor.content().length);
                                    point += tensor.content().length;
                                } else {
                                    System.arraycopy(zero, 0, content, point, zero.length);
                                    point += zero.length;
                                }
                            }
                            set.value.put(idx, content);
                        }
                    }
                    data.add(set);
                }
                sock.send(pack(data), 0);
                sock.recv();
            }
        }
    }

    ByteBuffer buf = ByteBuffer.allocate(640 << 20);

    private byte[] pack(HashSet<DataSet> dataset) {
        buf.clear();
        buf.putInt(dataset.size());
        for (DataSet data : dataset) {
            buf.putInt(data.name);
            buf.putInt(data.indexNum);
            buf.putInt(data.dtype);
            buf.putInt(data.shape.length);
            for (int i = 0; i < data.shape.length; ++i) {
                buf.putInt(data.shape[i]);
            }
            buf.putInt(data.value.size());
            for (Index idx : data.value.keySet()) {
                for (int i = 0; i < data.indexNum; ++i) {
                    buf.putInt(idx.at(i));
                }
                buf.put(data.value.get(idx));
            }
        }
        buf.flip();
        byte[] ret = new byte[buf.limit()];
        buf.get(ret);
        return ret;
    }

    public void run() throws ParseCancellationException {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket sock = context.socket(ZMQ.REQ);
        sock.connect("tcp://localhost:" + port);
        System.out.println("run");
        sendFirst(sock);
        sendBatch(sock);
    }
}
