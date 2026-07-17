package com.sheahorn.llmtoolbox.buildtools.docker;

public class DockerRequestDto {
    public String container;
    public String image;
    public String name;
    public String port;
    public String env;
    public String volume;
    public String command;
    public String composeFile;
    public String composeService;
    public String network;
    public String hostname;
    public String restart;
    public String source;
    public String target;
    public String tail;
    public String filter;
    public Boolean force;
    public Boolean rm;
}
