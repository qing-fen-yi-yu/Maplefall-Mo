package com.guilu.client;

import com.guilu.Constants.ClientConstant;
import com.guilu.DynamicTPConfig.TpConfigUpdater;
import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Manager.updater.PoolUpdater;
import com.guilu.client.Web.WebClientResultInfo;
import com.guilu.client.Web.WebDispatchClient;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.instance.InstanceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class TPUpdaterClient extends AbstractTPUpdaterClient {
    private final InstanceManager instanceManager;
    private final PoolUpdater poolUpdater;
    private final WebDispatchClient webDispatchClient;
    private final String UPDATE_POOL_URI = "/pool/update";

    @Override
    public boolean publishTPConfigEvent(TPConfigUpdaterEvent event) {
        try {
            PoolUpdateConfig poolConfig = event.getPoolConfig();
            String service = poolConfig.getService();
            Integer port = poolConfig.getPort();
            String host = poolConfig.getHost();
            if(service==null){
                throw new ThreadPoolException("服务名不能为空");
            }
            List<ServiceInstance> instances = instanceManager.getInstance(service);
            if(port==null||host==null){
                log.warn("全局级配置更新");
                return true;
            }else {
                instances.removeIf(instance -> instance.getHost()
                        .equals(host) && port.equals(instance.getPort()));
            }
            List<CompletableFuture<WebClientResultInfo>> dispatchResult =
                    webDispatchClient.updateDispatch(instances, poolConfig, instanceManager.getCurAppName(), UPDATE_POOL_URI);
            //todo 校验dispatchResult是否成功
            return false;
        } catch (Exception e) {
            log.error("线程池配置更新失败", e);
            throw new ThreadPoolException("线程池配置更新失败"+e.getMessage());
        }
    }

    @Override
    public boolean updateTP(BaseThreadPoolConfig threadPoolConfig) {
        poolUpdater.update(threadPoolConfig);
        return true;
    }
}
