package com.guilu.client.Auth.fallback;

import com.guilu.client.Auth.UserClient;
import com.guilu.domain.Result;
import com.guilu.dto.Auth.User;
import com.guilu.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class UserClientFallback implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        log.error(cause.getMessage(),cause);
        return new UserClient() {
            @Override
            public Result<List<User>> queryUserDetailByUserIds(Collection<Long> userIds) {
                return CollUtils.empty(User.class);
            }
        };
    }
}
