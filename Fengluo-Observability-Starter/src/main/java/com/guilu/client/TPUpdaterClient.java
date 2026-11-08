package com.guilu.client;

import com.guilu.Constants.ClientConstant;
import com.guilu.DynamicTPConfig.TpConfigUpdater;
import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Event.TPRefreshEvent;
import com.guilu.EventPublisher.AbstractEventPublisher;
import com.guilu.EventPublisher.FLSpringEventPublisher;
import com.guilu.Manager.updater.PoolUpdater;
import com.guilu.client.Web.WebClientResultInfo;
import com.guilu.client.Web.WebDispatchClient;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.instance.InstanceManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;


//todo 识别每个服务设置的前缀
@Slf4j
@RequiredArgsConstructor
public class TPUpdaterClient extends AbstractTPUpdaterClient {
    private final InstanceManager instanceManager;
    private final PoolUpdater poolUpdater;
    private final WebDispatchClient webDispatchClient;
    private final String UPDATE_POOL_URI = "/maple/pool/update";

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
            }else {
                instances.removeIf(instance -> !(instance.getHost()
                        .equals(host) && port.equals(instance.getPort())));
            }
            List<CompletableFuture<WebClientResultInfo>> dispatchResult =
                    webDispatchClient.updateDispatch(instances, poolConfig, instanceManager.getCurAppName(), UPDATE_POOL_URI);
            for(CompletableFuture<WebClientResultInfo> future:dispatchResult){
                WebClientResultInfo webClientResultInfo = future.get();
                if(!webClientResultInfo.isSuccess()){
                    throw new ThreadPoolException("线程池配置更新失败"+webClientResultInfo.getErrorMsg()+"result:"+webClientResultInfo.getResult());
                }
            }
            return true ;
        } catch (Exception e) {
            throw new ThreadPoolException("线程池配置更新失败"+e.getMessage());
        }
    }

    @Override
    public boolean updateTP(BaseThreadPoolConfig threadPoolConfig) {
        poolUpdater.update(threadPoolConfig);
        AbstractEventPublisher.publishEvent(new TPRefreshEvent(this));
        return true;
    }

    public boolean updateTPs(@Valid Collection<BaseThreadPoolConfig> config) {
        poolUpdater.update(config);
        return true;
    }
}
