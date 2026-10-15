package com.guilu.client.Auth;

import com.guilu.client.Auth.fallback.RoleClientFallback;
import com.guilu.domain.Result;
import com.guilu.dto.Auth.Role;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "UserApplication" ,contextId = "roleClient",fallbackFactory = RoleClientFallback.class,url = "")
public interface RoleClient {
    @GetMapping("/role/detail")
    public Result<List<Role>> getDetailByIds(List<Long>  ids);
}
