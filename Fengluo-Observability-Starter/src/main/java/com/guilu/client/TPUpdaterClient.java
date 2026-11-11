package com.guilu.client;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Event.TPCreateEvent;
import com.guilu.client.Web.WebClientResultInfo;
import com.guilu.client.Web.WebDispatchClient;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.PoolCreateConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.instance.InstanceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 任务分发器-只进行跨服务传输
 */
@Slf4j
public class TPUpdaterClient extends AbstractTPUpdaterClient {
    private final InstanceManager instanceManager;
    private final WebDispatchClient webDispatchClient;
    private final String UPDATE_POOL_URI = super.getPrefix() + "/pool/update";
    private final String REMOVE_POOL_URI = super.getPrefix() + "/pool/remove";
    private final String REGISTER_POOL_URI = super.getPrefix() + "/pool/register";

    public TPUpdaterClient(InstanceManager instanceManager,  WebDispatchClient webDispatchClient, String prefix) {
        super(prefix);
        this.instanceManager = instanceManager;
        this.webDispatchClient = webDispatchClient;
    }
    //接收线程池配置
    @Override
    public boolean publishTPConfigEvent(TPConfigUpdaterEvent event) {
        try {
            PoolUpdateConfig poolConfig = event.getPoolConfig();
            String service = poolConfig.getService();
            Integer port = poolConfig.getPort();
            String host = poolConfig.getHost();
            List<ServiceInstance> instances = getInstance(service, host, port);
            return dispatchRequest(instances, poolConfig, service,UPDATE_POOL_URI);
        } catch (Exception e) {
            throw new ThreadPoolException("线程池配置更新失败"+e.getMessage());
        }
    }

    @Override
    public boolean addTPConfig(TPCreateEvent event) throws ExecutionException, InterruptedException {
        PoolCreateConfig createConfig = event.getCreateConfig();
        String host = createConfig.getHost();
        Integer port = createConfig.getPort();
        List<ServiceInstance> instances = getInstance(createConfig.getService(),host,port);
        if (!dispatchRequest(instances,createConfig,createConfig.getService(),REGISTER_POOL_URI)){
            throw new ThreadPoolException("线程池配置注册失败");
        }
        return true;
    }

    @Override
    public void removeDynamicTP(String poolName, String serviceName, String instanceConfig) throws ExecutionException, InterruptedException {
        List<ServiceInstance> instances = getInstance(serviceName,instanceConfig);
        if (!dispatchRequest(instances, poolName, serviceName, REMOVE_POOL_URI)){
            throw new ThreadPoolException("线程池配置移除失败");
        }
    }

    /**
     * 该方法不进行线程池配置更新，仅用于发布线程池配置更新事件
     */
    @Override
    public boolean updateTP(BaseThreadPoolConfig threadPoolConfig) {
        return false;
    }

    private List<ServiceInstance> getInstance(String service,String instanceConfig){
        if(instanceConfig.isBlank()){
            return getInstance(service,null,null);
        }
        int index = instanceConfig.indexOf(":");
        if(index == -1){
            throw new ThreadPoolException("实例配置错误");
        }
        return getInstance(service,
                instanceConfig.substring(0,index),
                Integer.parseInt(instanceConfig.substring(index+1)));
    }
    private List<ServiceInstance> getInstance(String service,String host,Integer port){
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
        return instances;
    }

    private boolean dispatchRequest(List<ServiceInstance> instances,Object poolConfig,String service,String uri) throws ExecutionException, InterruptedException {
        List<CompletableFuture<WebClientResultInfo>> dispatchResult =
                webDispatchClient.updateDispatch(instances, poolConfig, service, uri);
        for(CompletableFuture<WebClientResultInfo> future:dispatchResult){
            WebClientResultInfo webClientResultInfo = future.get();
            if(!webClientResultInfo.isSuccess()){
                log.error("线程池配置更新失败{}result:{}", webClientResultInfo.getErrorMsg(), webClientResultInfo.getResult());
            }
        }
        return true;
    }
}
