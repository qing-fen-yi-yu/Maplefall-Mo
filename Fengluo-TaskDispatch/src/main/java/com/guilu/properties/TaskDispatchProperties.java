package com.guilu.properties;

import com.guilu.enums.TaskDispatchType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties("task.dispatch")
public class TaskDispatchProperties {
    private boolean enabled = true;
    private List<TaskDispatchType> type = new ArrayList<>(List.of(TaskDispatchType.MFTASK));
}
