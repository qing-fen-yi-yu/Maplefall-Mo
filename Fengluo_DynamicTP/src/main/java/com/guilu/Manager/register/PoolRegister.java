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
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Executor;

@ConditionalOnClass(RegisterBeanPostProcessor.class)
@Slf4j
@Component("poolRegister")
public class PoolRegister extends PoolManager implements BeanFactoryAware{

    private DefaultListableBeanFactory beanFactory;

    public static int registerThreadPool(Collection<RegisPoolDto> tps) {
        if (tps == null || tps.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (RegisPoolDto tp : tps) {
            if (registerThreadPool(tp.getPool(), tp.getAnno())) {
                count++;
            } else {
                log.warn("线程池注册失败: {}", tp.getPool());
            }
        }
        return count;
    }

    public static boolean registerThreadPool(Object pool, DynamicThreadPool anno) {
        if (pool == null) {
            return false;
        }
        String name = (anno != null) ? anno.name() : null;
        if (StringUtils.isBlank(name)) {
            return registerDefaultThreadPool(pool);
        }
        return doRegister(pool, name);
    }

    public static boolean registerDefaultThreadPool(Object pool) {
        if (pool == null) {
            return false;
        }
        String name = (pool instanceof DynamicPool dp)
                ? dp.getThreadPoolName()
                : RegisDefaultNameUtil.getThreadPoolName(pool);
        return doRegister(pool, name);
    }

    private static boolean doRegister(Object pool, String poolName) {
        if (pool == null || StringUtils.isBlank(poolName)) {
            return false;
        }
        Executor prev = addRegistry(poolName,pool);
        if (prev != null) {
            log.warn("线程池 [{}] 已存在，跳过注册", poolName);
            return false;
        }
        incrementCount();
        return true;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}