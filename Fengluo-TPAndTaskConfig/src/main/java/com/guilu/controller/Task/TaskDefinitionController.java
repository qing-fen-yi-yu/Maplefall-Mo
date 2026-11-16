package com.guilu.controller.Task;


import com.guilu.domain.Result;
import com.guilu.domain.po.TaskDefinition;
import com.guilu.service.ITaskDefinitionService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 任务定义 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@RestController
@RequestMapping("/taskDefinition")
@RequiredArgsConstructor
public class TaskDefinitionController {
    private final ITaskDefinitionService taskDefinitionService;

    @PostMapping("/add")
    @ApiOperation("新增任务")
    public Result<Void> addTaskDefinition(@RequestBody TaskDefinition taskDefinition) {
        taskDefinitionService.addTaskDefinition(taskDefinition);
        return Result.success();
    }
    @GetMapping("/getById/{id}")
    @ApiOperation("根据Id获取任务详细信息")
    public Result<TaskDefinition> getTaskDefinition(@PathVariable Long id){
        return Result.success(taskDefinitionService.getById(id));
    }
    @DeleteMapping("del/{id}")
    @ApiOperation("根据Id删除任务(同时删除任务组中的任务)")
    public Result<Void> deleteTaskDefinitionById(@PathVariable Long id){
        taskDefinitionService.removeById(id);
        return Result.success();
    }
    @ApiOperation("根据任务Id执行任务")
    @PostMapping("run/task/{id}")
    public Result<String> runTask(@PathVariable Long id){
        return Result.success(taskDefinitionService.runTask(id));
    }
}
