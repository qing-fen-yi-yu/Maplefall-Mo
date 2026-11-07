package com.guilu.client.RequestPoint;

import com.guilu.Constants.ClientConstant;
import com.guilu.client.TPUpdaterClient;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.instance.InstanceManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequestMapping("/pool")
@RequiredArgsConstructor
public class PoolClientRequestPoint {
    private final TPUpdaterClient client;
    private final InstanceManager instanceManager;

    @PostMapping("/update")
    public boolean updateTP(@Valid @RequestBody BaseThreadPoolConfig config,
                            HttpServletRequest request) {

        String header = request.getHeader(ClientConstant.POINT_HEADER_REQUEST);
        String remote = request.getRemoteAddr() + ":" + request.getRemotePort();
        if (header == null || instanceManager.getInstance(header).isEmpty()) {
            log.warn("非法线程池更新请求, header={}, remote={}", header, remote);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid instance header");
        }
        log.info("实例{}收到线程池更新请求, remote={}, config={}", header, remote, config);
        boolean ok = client.updateTP(config);
        if (!ok) {
            log.warn("线程池配置更新未生效, instance={}, config={}", header, config);
        }
        return ok;
    }
}
