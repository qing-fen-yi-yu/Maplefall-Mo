package com.guilu.interceptor;

import com.guilu.utils.StringUtils;
import com.guilu.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.guilu.constants.JwtConstants.USER_HEADER;

/**
 * 业务模块侧拦截器：把网关解析后注入的 user-info 请求头写入 UserContext，
 * 业务代码可直接通过 ThreadLocal 获取当前登录用户，请求结束后清理。
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(USER_HEADER);
        if (!StringUtils.isBlank(userId)) {
            try {
                UserContext.setUser(Long.valueOf(userId));
            } catch (NumberFormatException e) {
                log.warn("非法的 {} 请求头: {}", USER_HEADER, userId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.removeUser();
    }
}
