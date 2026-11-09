package com.guilu.client.RequestPoint;

import com.guilu.Constants.ClientConstant;
import com.guilu.client.TPUpdaterClient;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.instance.InstanceManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pool")
@RequiredArgsConstructor
public class PoolClientRequestPoint {

    private final TPUpdaterClient client;
    private final InstanceManager instanceManager;

    @PostMapping("/update")
    public boolean updateTP(@Valid @RequestBody PoolUpdateConfig config,
                            HttpServletRequest request) {
        log.info("接收线程池配置更新请求, config={}", config);
        if (!valiRequest(request)) {
            return false;
        }
        boolean ok = client.updateTP(config);
        if (!ok) {
            log.warn("线程池配置更新未生效, poolName={}, config={}", config.getPoolName(), config);
        }
        return ok;
    }

    private boolean valiRequest(HttpServletRequest request) {
        String header = request.getHeader(ClientConstant.POINT_HEADER_REQUEST);
        String remote = request.getRemoteAddr() + ":" + request.getRemotePort();
        if (!instanceManager.getCurAppName().equals(header)) {
            log.warn("非法线程池更新请求, header={}, remote={}", header, remote);
            return false;
        }
        return true;
    }
}