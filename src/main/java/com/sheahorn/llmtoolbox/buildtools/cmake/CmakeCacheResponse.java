package com.sheahorn.llmtoolbox.buildtools.cmake;

import java.util.LinkedHashMap;
import java.util.Map;

public class CmakeCacheResponse {
    public String buildPath;
    public Map<String, String> entries = new LinkedHashMap<>();
}
