package com.guilu.client.TpConfig.fallback;

import com.guilu.client.TpConfig.TPConfigClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class TPConfigFallback implements FallbackFactory<TPConfigClient> {
    @Override
    public TPConfigClient create(Throwable cause) {
        return null;
    }
}
