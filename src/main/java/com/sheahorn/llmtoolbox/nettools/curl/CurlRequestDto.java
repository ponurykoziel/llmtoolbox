package com.sheahorn.llmtoolbox.nettools.curl;

import java.util.LinkedHashMap;
import java.util.Map;

public class CurlRequestDto {

    public HttpVerb verb;
    public String url;

    public String contentType;
    public String accept;

    public Map<String, String> headers = new LinkedHashMap<>();
    public Map<String, String> queryParams = new LinkedHashMap<>();

    public String body;
}