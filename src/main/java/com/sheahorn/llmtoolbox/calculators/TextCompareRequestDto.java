package com.sheahorn.llmtoolbox.calculators;

public class TextCompareRequestDto {
    public String a;
    public String b;
    public Integer n; // for n-gram
    public TextNormalizationOptions normalization;
}