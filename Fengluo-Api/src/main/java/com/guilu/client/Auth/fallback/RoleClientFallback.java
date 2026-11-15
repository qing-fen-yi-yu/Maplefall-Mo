package com.guilu.client.Auth.fallback;

import com.guilu.client.Auth.RoleClient;
import com.guilu.domain.Result;
import com.guilu.dto.Auth.Role;
import com.guilu.utils.CollUtils;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

public class RoleClientFallback implements FallbackFactory<RoleClient> {
    @Override
    public RoleClient create(Throwable cause) {
        return new RoleClient() {
            @Override
            public Result<List<Role>> getDetailByIds(List<Long> ids) {
                return CollUtils.empty(Role.class);
            }
        };
    }
}
