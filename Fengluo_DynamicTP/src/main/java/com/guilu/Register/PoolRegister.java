package com.guilu.Register;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.domain.RegisPoolDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ConditionalOnClass(RegisterBeanPostProcessor.class)
@Slf4j
@Component(value = "poolRegister")
public class PoolRegister implements BeanFactoryAware {
    private DefaultListableBeanFactory beanFactory;
    /**
     * PoolRegister注册流程
     * 得到线程池信息
     * 校验是否存在，若存在则跳过
     * 判断是否要使用自用线程池名称或者创建
     */
    //建立动态线程池注册表
    private static volatile Map<String, DynamicPool> map = new ConcurrentHashMap<>();
    private static volatile AtomicInteger count = new AtomicInteger();
    public static int registerThreadPool(Collection<RegisPoolDto> TPS){
        if(TPS == null || TPS.isEmpty()){
            return 0;
        }
        for (RegisPoolDto tp : TPS) {
            DynamicThreadPool anno = tp.getAnno();
            Object pool = tp.getPool();
            registerThreadPool(pool, anno);
        }
        return count.get();
    }
    public static boolean registerThreadPool(Object pool){
        count.getAndIncrement();
        return true;
    }
    public static boolean registerThreadPool(Object poll, DynamicThreadPool pollAnno){
        count.getAndIncrement();
        return true;
    }
    public static boolean registerThreadPool(DynamicPool poll, DynamicThreadPool pollAnno){
        count.getAndIncrement();
        return true;
    }
    public static boolean registerThreadPool(DynamicPool pool,String name){
        count.getAndIncrement();
        return true;
    }
    private String createThreadName(Object value){
        return "";
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }
    public int getRegisCount(){
        return count.get();
    }
}
