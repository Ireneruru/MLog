package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.SourceType;

public class FuncSource extends Source {
    private String func;
    private float[] args;

    public FuncSource(String func, float[] args) {
        super(SourceType.FUNCTION);
        this.func = func;
        this.args = args;
    }

    public String func() {
        return func;
    }

    public float[] args() {
        return args;
    }
}
