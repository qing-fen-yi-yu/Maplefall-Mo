package com.guilu.Manager.updater;

import com.guilu.Manager.PoolManager;
import com.guilu.utils.ObjectUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.*;

//线程池销毁自动检测器
@Slf4j
public class AutoDestroyTP extends PoolManager {
    private  static final AutoDestroyTP autoDestroyTP = new AutoDestroyTP();
    @Setter
    private volatile TimeUnit unit = TimeUnit.MILLISECONDS;
    @Setter
    private volatile Long checkTimes = 10*1000L;
    private final ScheduledExecutorService checkShutDownPool =
            new ScheduledThreadPoolExecutor(1);
    public void checkDown(){
        checkShutDownPool.scheduleAtFixedRate(() -> {
            try {
                if(getCurCount() == 0){
                    return;
                }
                Map<String, Executor> alivePool = getAlivePool();
                for(String key : alivePool.keySet()){
                    ExecutorService executor = (ExecutorService) alivePool.get(key);
                    if(ObjectUtils.isEmpty(executor) ||executor.isShutdown()){
                        log.info("remove pool--{},cause: the pool is shutdown",executor);
                        removePoolByRegis(key,executor);
                    }
                }
            } catch (Exception e){
                log.error("线程池检查线程报错",e);
            }
                },0,checkTimes,unit
        );
    }
    public static AutoDestroyTP of(){
        return  autoDestroyTP;
    }
    private AutoDestroyTP(){}
}
