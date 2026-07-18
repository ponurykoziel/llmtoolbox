package com.sheahorn.llmtoolbox.buildtools.docker;

import java.util.List;
import java.util.Map;

public class DockerRunRequest {
    public String image;
    public String name;
    public String path;
    public List<String> cmd;
    public String network;
    public Map<String, String> volumes;
    public Map<String, String> ports;
    public Map<String, String> envs;
}
