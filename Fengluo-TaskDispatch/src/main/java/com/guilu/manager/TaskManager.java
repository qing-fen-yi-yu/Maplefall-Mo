package com.guilu.manager;

import com.guilu.domain.DispatchMFTask;
import com.guilu.domain.DispatchScheduledTask;
import com.guilu.domain.Task;
import com.guilu.utils.RandomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.config.ScheduledTask;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class TaskManager {
    private static final AtomicLong TASKID = new AtomicLong(System.currentTimeMillis()%100000);
    //spring调度任务配置
    private static final Map<String, DispatchScheduledTask> scheduleTasks = new ConcurrentHashMap<>();
    //自定义任务配置
    private static final Map<String, DispatchMFTask> mfTasks = new ConcurrentHashMap<>();

    public DispatchMFTask getMFTaskByName(String name){
        return mfTasks.get(name);
    }

    public DispatchScheduledTask getScheduledTaskByName(String name){
        return scheduleTasks.get(name);
    }
    public static long getTaskID(){
        return TASKID.getAndIncrement();
    }

    public boolean toRegistry(Task task){
        try {
            if(task instanceof DispatchMFTask dispatchMfTask){
                DispatchMFTask task1 = mfTasks.putIfAbsent(task.getTaskId(), dispatchMfTask);
                return task1 != null;
            } else if(task instanceof DispatchScheduledTask dispatchScheduledTask){
                DispatchScheduledTask dispatchScheduledTask1 = scheduleTasks.putIfAbsent(task.getTaskId(), dispatchScheduledTask);
                return dispatchScheduledTask1 != null;
            }
            System.err.println("注册任务"+task.getTaskId());
        } catch (Exception e){
            log.error("注册任务失败",e);
            return false;
        }
        log.warn("该任务类型暂不支持");
        return false;
    }

    public boolean removeScheduledTaskByName(String taskId){
        try {
            DispatchScheduledTask remove = scheduleTasks.remove(taskId);
            ScheduledTask task = remove.getTask();
            task.cancel();//取消任务
            return true;
        } catch (Exception e){
            log.error("取消任务失败",e);
            return false;
        }
    }

    public boolean removeMFTaskByName(String taskId){
        try {
            mfTasks.remove(taskId);
            return true;
        } catch (Exception e){
            log.error("取消任务失败",e);
            return false;
        }
    }

    public boolean updateTask(Task task){
        if(task instanceof DispatchMFTask dispatchMfTask){
            return updateMFTask(dispatchMfTask);
        } else if(task instanceof DispatchScheduledTask dispatchScheduledTask){
            return updateScheduledTask(dispatchScheduledTask);
        }
        return false;
    }
    private boolean updateMFTask(DispatchMFTask dispatchMfTask){
        return true;
    }
    private  boolean updateScheduledTask(DispatchScheduledTask dispatchScheduledTask){
        return true;
    }
}