package com.guilu.Manager.register;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.Manager.PoolManager;
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.domain.RegisPoolDto;
import com.guilu.utils.RegisDefaultNameUtil;
import com.guilu.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Executor;

@Slf4j
public class PoolRegister extends PoolManager {

    public static int registerThreadPool(Collection<RegisPoolDto> tps) {
        if (tps == null || tps.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (RegisPoolDto tp : tps) {
            boolean regisRes;

            String beanName = tp.getBeanName();
            Object pool = tp.getPool();
            DynamicThreadPool anno = tp.getAnno();
            if(StringUtils.isBlank(beanName)){
                regisRes = registerThreadPool(pool,anno);
            } else {
                regisRes = registerThreadPool(pool,anno,beanName);
            }
            if (regisRes) {
                count++;
            } else {
                log.warn("线程池注册失败: {}", pool);
            }
        }
        return count;
    }

    public static boolean registerThreadPool(Object pool, DynamicThreadPool anno) {
        return registerThreadPool(pool,anno,null);
    }
    public static boolean registerThreadPool(Object pool, DynamicThreadPool anno,String beanName) {
        if (pool == null) {
            return false;
        }
        // 显式注解名  > DynamicPool 自带名称 > 获取/生成线程池名称。
        String name = anno != null && StringUtils.isNotBlank(anno.name()) ? anno.name() : beanName;
        if (StringUtils.isBlank(name) && pool instanceof DynamicPool dynamicPool) {
            name = dynamicPool.getThreadPoolName();
        }
        if (StringUtils.isBlank(name)) name = RegisDefaultNameUtil.createThreadPoolName();
        return doRegister(pool, name);
    }

    public static boolean registerDefaultThreadPool(Object pool) {
        if (pool == null) {
            return false;
        }
        return registerThreadPool(pool, null, null);
    }

    private static boolean doRegister(Object pool, String poolName) {
        if (pool instanceof ThreadPoolTaskExecutor taskExecutor) {
            pool = taskExecutor.getThreadPoolExecutor();
        }
        if (pool == null || StringUtils.isBlank(poolName)) {
            return false;
        }
        Executor prev = addRegistry(poolName,pool);
        if (pool instanceof DynamicPool dynamicPool) dynamicPool.setThreadPoolName(poolName);
        if (prev != null) {
            log.warn("线程池 [{}] 已存在，跳过注册", poolName);
            return prev == pool;
        }
        incrementCount();
        return true;
    }
}
