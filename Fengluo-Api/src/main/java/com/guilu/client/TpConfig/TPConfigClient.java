package com.guilu.client.TpConfig;

import com.guilu.client.TpConfig.fallback.TPConfigFallback;
import com.guilu.domain.Result;
import com.guilu.domain.po.ThreadPool.CommonThreadPoolConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "tp-config-service",fallbackFactory = TPConfigFallback.class)
public interface TPConfigClient {
    Result<Void> publishPoll(CommonThreadPoolConfig threadPoolConfig);
}
