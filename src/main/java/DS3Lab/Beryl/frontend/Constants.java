package DS3Lab.Beryl.frontend;

import DS3Lab.Beryl.ir.Context;
import org.antlr.v4.runtime.misc.ParseCancellationException;

public class Constants {
    public static void set(String name, Integer value) {
        Context.putInt(name, value);
    }

    public static void set(String name, Float value) {
        Context.putFloat(name, value);
    }
}
