package com.guilu.client.RequestPoint;

import com.guilu.Constants.ClientConstant;
import com.guilu.DynamicTPConfig.TpConfigUpdater;
import com.guilu.Event.TPCreateEvent;
import com.guilu.domain.po.ThreadPool.PoolCreateConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.instance.InstanceManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/pool")
@RequiredArgsConstructor
public class PoolClientRequestPoint {
    private final TpConfigUpdater updater;
    private final InstanceManager instanceManager;

    @PostMapping("/update")
    public boolean updateTP(@Valid @RequestBody PoolUpdateConfig config,
                            HttpServletRequest request) {
        log.info("接收线程池配置更新请求, config={}", config);
        if (!valiRequest(request)) {
            return false;
        }
        boolean ok = updater.updateTP(config);
        if (!ok) {
            log.warn("线程池配置更新未生效, poolName={}, config={}", config.getPoolName(), config);
        }
        return ok;
    }
    //接收线程池配置注册请求
    @PostMapping("/register")
    public boolean registerTP(@Valid @RequestBody PoolCreateConfig config,
                            HttpServletRequest request) throws ExecutionException, InterruptedException {
        log.info("接收线程池配置注册请求, config={}", config);
        if (!valiRequest(request)) {
            return false;
        }
        boolean ok = updater.addTPConfig(new TPCreateEvent(this,config));
        if (!ok) {
            log.warn("线程池配置注册未生效, poolName={}, config={}", config.getPoolName(), config);
        }
        return ok;
    }

    @PostMapping("/remove")
    public boolean removeTP(@RequestParam String poolName,
                            HttpServletRequest request) throws ExecutionException, InterruptedException {
        log.info("接收线程池配置删除请求, poolName={}", poolName);
        if (!valiRequest(request)) {
            return false;
        }
        updater.removeDynamicTP(poolName,null,null);
        return true;
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