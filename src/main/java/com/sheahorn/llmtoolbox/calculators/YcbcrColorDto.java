package com.sheahorn.llmtoolbox.calculators;

public class YcbcrColorDto implements ColorDto {
    public final double y, cb, cr;
    public YcbcrColorDto(double y, double cb, double cr) {
        this.y = y;
        this.cb = cb;
        this.cr = cr;
    }
}
