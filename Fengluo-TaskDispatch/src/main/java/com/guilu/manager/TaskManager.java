package com.guilu.manager;

import com.guilu.domain.DispatchMFTask;
import com.guilu.domain.DispatchScheduledTask;
import com.guilu.domain.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TaskManager {
    private static final AtomicLong TASKID = new AtomicLong(System.currentTimeMillis()%100000);
    private final Map<String, DispatchScheduledTask> scheduleTasks = new ConcurrentHashMap<>();
    private final Map<String, DispatchMFTask> mfTasks = new ConcurrentHashMap<>();

    public Map<String, DispatchMFTask> getMfTasks() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(mfTasks));
    }

    public Map<String, DispatchScheduledTask> getScheduleTasks() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(scheduleTasks));
    }
    public DispatchMFTask getMFTaskByName(String name){
        return mfTasks.get(name);
    }

    public DispatchScheduledTask getScheduledTaskByName(String name){
        return scheduleTasks.get(name);
    }
    public static long getTaskID(){
        return TASKID.getAndIncrement();
    }

    public synchronized boolean toRegistry(Task task) {
        if (task == null) return false;
        try {
            if(task instanceof DispatchMFTask dispatchMfTask){
                return !scheduleTasks.containsKey(task.getTaskId())
                        && mfTasks.putIfAbsent(task.getTaskId(), dispatchMfTask) == null;
            } else if(task instanceof DispatchScheduledTask dispatchScheduledTask){
                return !mfTasks.containsKey(task.getTaskId())
                        && scheduleTasks.putIfAbsent(task.getTaskId(), dispatchScheduledTask) == null;
            }
        } catch (Exception e){
            log.error("注册任务失败",e);
            return false;
        }
        log.warn("该任务类型暂不支持");
        return false;
    }

    public boolean removeScheduledTaskByName(String taskId) {
        try {
            DispatchScheduledTask remove = scheduleTasks.remove(taskId);
            if (remove == null) return false;
            remove.cancel();
            return true;
        } catch (Exception e){
            log.error("取消任务失败",e);
            return false;
        }
    }

    public boolean removeMFTaskByName(String taskId) {
        try {
            DispatchMFTask removed = mfTasks.remove(taskId);
            if (removed == null) return false;
            removed.cancel();
            return true;
        } catch (Exception e){
            log.error("取消任务失败",e);
            return false;
        }
    }

    public boolean executeMFTask(String taskId) {
        DispatchMFTask task = mfTasks.get(taskId);
        if (task == null) return false;
        task.run();
        return true;
    }
}
