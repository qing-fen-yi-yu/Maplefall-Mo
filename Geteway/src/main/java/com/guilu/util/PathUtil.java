package com.guilu.util;

import org.springframework.util.PathMatcher;

import java.util.Collection;

public class PathUtil {
    public static boolean isPath(String path, PathMatcher pathMatcher,
                                        Collection<String> paths) {
        for(String match : paths){
            if(pathMatcher.match(match, path)){
                return true;
            }
        }
        return false;
    }
}
