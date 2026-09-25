package com.guilu.util;

import org.springframework.util.PathMatcher;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class PathUtil {

    /** 未指定请求方法时使用的通配符，匹配所有方法 */
    private static final String ANY_METHOD = "*";
    private static final String METHOD_SEPARATOR = ":";

    public static boolean isPath(String path, PathMatcher pathMatcher,
                                        Collection<String> paths) {
        for(String match : paths){
            if(pathMatcher.match(match, path)){
                return true;
            }
        }
        return false;
    }

    /**
     * 把路径匹配符统一为 {method}:{path} 格式，以便与请求的 "METHOD:/path" 做 Ant 匹配。
     * 未指定方法时补 *（匹配所有方法），路径部分补全前导斜杠。
     * 例如：user/account/login -> *:/user/account/login，POST:/user/login -> POST:/user/login
     */
    public static Set<String> normalize(Collection<String> patterns) {
        Set<String> result = new LinkedHashSet<>();
        if (patterns == null) {
            return result;
        }
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                continue;
            }
            String value = pattern.trim();
            String method = ANY_METHOD;
            String path = value;
            int index = value.indexOf(METHOD_SEPARATOR);
            if (index >= 0) {
                method = value.substring(0, index);
                path = value.substring(index + 1);
            }
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            result.add(method + METHOD_SEPARATOR + path);
        }
        return result;
    }
}
