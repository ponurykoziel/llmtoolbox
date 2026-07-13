package com.sheahorn.llmtoolbox.calculators;

public class RandomValueRequestDto {
    public String type; // byte, boolean, int, long, float, double, gaussian, uuid
    public Long min;
    public Long max;
    public Double mean;
    public Double stddev;
}
