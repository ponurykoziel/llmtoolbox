package com.sheahorn.llmtoolbox.nettools.dig;

import java.util.List;

public class DigRequestDto {

    public String name;

    /**
     * Optional.
     * If empty/null, controller checks common record types.
     */
    public List<DigRecordType> types;
}