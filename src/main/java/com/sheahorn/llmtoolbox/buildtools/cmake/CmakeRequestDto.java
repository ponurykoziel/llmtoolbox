package com.sheahorn.llmtoolbox.buildtools.cmake;

import java.util.Map;

public class CmakeRequestDto {
    /** Where CMakeLists.txt lives (required for configure) */
    public String sourcePath;

    /** Out-of-tree build directory (required for all operations) */
    public String buildPath;

    /** Debug | Release | RelWithDebInfo | MinSizeRel */
    public String buildType;

    /** -D KEY=VALUE pairs passed to cmake configure */
    public Map<String, String> defines;

    /** Specific target to build (optional) */
    public String target;

    /** -j N parallelism (optional, defaults to nproc) */
    public Integer parallel;

    /** --clean-first (optional) */
    public Boolean cleanFirst;

    /** Dump full compiler/linker commands (optional) */
    public Boolean verbose;
}
