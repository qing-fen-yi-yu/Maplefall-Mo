package com.guilu.constants;

public class AuthMetadataConstants {

    /** 无需登录（放行）的路径 */
    public static final String EXCLUDE_PATHS_KEY = "fl.auth.excludePaths";

    /** 需要登录（拦截）的路径 */
    public static final String INCLUDE_PATHS_KEY = "fl.auth.includePaths";

    /** 元数据中路径之间的分隔符 */
    public static final String PATH_DELIMITER = ",";
}
