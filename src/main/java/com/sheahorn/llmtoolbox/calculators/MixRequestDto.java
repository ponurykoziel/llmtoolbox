package com.sheahorn.llmtoolbox.calculators;

public class MixRequestDto implements ColorSource {
    public String source_color;
    public String target_color;
    public Double ratio; // 0 = source, 1 = target

    @Override
    public String sourceColor() {
        return source_color;
    }
}
