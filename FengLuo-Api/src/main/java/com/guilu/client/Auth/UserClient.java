package com.guilu.client.Auth;

import com.guilu.client.Auth.fallback.UserClientFallback;
import com.guilu.domain.Result;
import com.guilu.dto.Auth.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.List;

@FeignClient(name = "UserApplication",fallbackFactory = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/detail")
    Result<List<User>> queryUserDetailByUserIds(Collection<Long> userIds);
}
