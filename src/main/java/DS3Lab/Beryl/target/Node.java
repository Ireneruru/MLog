package DS3Lab.Beryl.target;

import java.util.Arrays;

public class Node {
    public int hash;

    public String op;
    public Node[] args;

    public String entry;
    public int[] index;

    public Integer intVal;
    public Float floatVal;

    public Node(String op, Node[] args) {
        this.op = op;
        this.args = args;
        hash = op.hashCode();
        int base = 10000007;
        for (Node n : args) {
            hash = hash + n.hash * base;
            base *= 100000007;
        }
    }

    public Node(String entry, int[] index) {
        this.entry = entry;
        this.index = index;
        hash = entry.hashCode() ^ Arrays.hashCode(index);
    }

    public Node(Integer intVal) {
        this.intVal = intVal;
        hash = ("I" + intVal).hashCode();
    }

    public Node(Float floatVal) {
        this.floatVal = floatVal;
        hash = ("F" + floatVal).hashCode();
    }
}
