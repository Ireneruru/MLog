package DS3Lab.Beryl.data;

import DS3Lab.Beryl.type.SourceType;

public class Source {
    private SourceType type;

    Source(SourceType type) {
        this.type = type;
    }

    public SourceType type() {
        return type;
    }
}

