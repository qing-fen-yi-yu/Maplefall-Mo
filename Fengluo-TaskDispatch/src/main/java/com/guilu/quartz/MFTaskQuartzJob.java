package com.guilu.quartz;

import com.guilu.manager.TaskManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MFTaskQuartzJob implements Job {
    public static final String TASK_ID = "taskId";
    public static final String TASK_MANAGER = "fengluoTaskManager";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            TaskManager taskManager = (TaskManager) context.getScheduler().getContext().get(TASK_MANAGER);
            String taskId = context.getMergedJobDataMap().getString(TASK_ID);
            if (taskManager == null || !taskManager.executeMFTask(taskId)) {
                throw new JobExecutionException("MFTask not registered: " + taskId);
            }
        } catch (JobExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new JobExecutionException("Failed to execute MFTask", e);
        }
    }
}
