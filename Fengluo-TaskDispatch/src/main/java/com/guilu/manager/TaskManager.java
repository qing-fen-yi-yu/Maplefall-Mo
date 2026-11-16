package com.guilu.manager;

import com.guilu.domain.MFTask;
import com.guilu.domain.ScheduledTask;
import com.guilu.domain.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
public class TaskManager {
    //spring调度任务配置
    private static final Map<String, ScheduledTask> scheduleTasks = new ConcurrentHashMap<>();
    //自定义任务配置
    private static final Map<String, MFTask> mfTasks = new ConcurrentHashMap<>();

    public static MFTask getMFTaskByName(String name){
        return mfTasks.get(name);
    }

    public static ScheduledTask getScheduledTaskByName(String name){
        return scheduleTasks.get(name);
    }

    public boolean toRegistry(Task task){
        try {
            if(task instanceof MFTask mfTask){
                MFTask task1 = mfTasks.putIfAbsent(task.getTaskId(), mfTask);
                return task1 != null;
            } else if(task instanceof ScheduledTask scheduledTask){
                ScheduledTask scheduledTask1 = scheduleTasks.putIfAbsent(task.getTaskId(), scheduledTask);
                return scheduledTask1 != null;
            }
        } catch (Exception e){
            log.error("注册任务失败",e);
            return false;
        }
        log.warn("该任务类型暂不支持");
        return false;
    }

    public boolean removeScheduledTaskByName(String taskId){
        try {
            ScheduledTask remove = scheduleTasks.remove(taskId);
            org.springframework.scheduling.config.ScheduledTask task = remove.getTask();
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
        if(task instanceof MFTask mfTask){
            return updateMFTask(mfTask);
        } else if(task instanceof ScheduledTask scheduledTask){
            return updateScheduledTask(scheduledTask);
        }
        return false;
    }
    private boolean updateMFTask(MFTask mfTask){
        return true;
    }
    private  boolean updateScheduledTask(ScheduledTask scheduledTask){
        return true;
    }
}