# 枫落 FengLuo

**面向 Spring Boot 服务的动态线程池管理与可观测性项目。**

枫落围绕业务线程池的注册、运行状态查看和参数调整，逐步建设配置管理、异常观测与运维管理能力。当前开发重点是把单 JVM 内的线程池管理做完整，再接入配置中心和多实例管理。

> 项目处于开发阶段。目前可以尝试本地线程池注册、查询与动态调参；远程配置下发、实例生效反馈和完整管理平台尚未完成。模块目录的存在不代表相应功能已交付。

## 项目解决什么问题

业务服务通常包含邮件发送、异步处理、后台任务等线程池。线程池创建后，需要明确它的名称、当前运行状态、参数调整方式，以及服务关闭时由谁负责释放资源。

枫落当前提供以下本地能力：

- **统一注册**：发现明确标注的线程池 Bean、字段和无参方法，保存实际对象引用。
- **原地调参**：按名称调整核心线程数、最大线程数和存活时间，业务继续使用原线程池。
- **运行状态查询**：获取线程数、活跃数、队列长度、完成任务数等本地快照。
- **队列容量调整**：自定义队列支持扩缩容，缩容保留已有任务。
- **生命周期管理**：清理已终止线程池的注册记录，并在容器关闭时执行关闭与等待。

在此基础上，项目计划提供“配置发布 → 实例应用 → 结果反馈 → 状态查询”的管理流程。任务调度、通知与搜索是平台扩展方向。

## 当前能力与进度

| 领域 | 当前情况                                      |
|---|-------------------------------------------|
| 动态线程池 | 已实现本地注册、重名检查、原地调参、状态快照、队列容量调整和关闭管理        |
| Spring 接入 | 已实现注解扫描与自动配置；保留观测启动器的兼容导入入口               |
| 观测能力 | 已实现异常处理、JVM 补充指标、GC 事件和线程池指标的 Micrometer SDK 与 Starter 自动配置 |
| 账号与权限 | 已有用户、角色、菜单、用户角色关系及 JWT/Redis 认证授权代码       |
| 网关 | 已有请求标识透传、路径检查和鉴权，当前用户服务路由为 `/maple/**`    |
| 配置管理 | 已有线程池配置与快照的数据模型、Mapper 和基础服务代码，尚未完成远程应用流程 |
| 任务调度 | 已有任务定义、执行记录、分组与关联的数据模型及基础代码，调度执行链路待建设     |
| 搜索 | todo Elasticsearch 接入与索引初始化代码，业务检索能力待完善   |
| 监控中心与通知 | 模块已规划，集中管理与通知流程待建设                        |

## 项目结构

```text
FengLuo/
├── Fengluo_DynamicTP/              # 动态线程池、注册管理与调参实现
├── Fengluo-Observability-Starter/   # 异常处理、Actuator/Prometheus、监控自动配置
├── Fengluo-Config/                 # 线程池配置与配置快照管理
├── Fengluo-Monitor/                # 监控模块集合
│   ├── Fengluo-Monitor-SDK/        # Micrometer MeterBinder、GC 订阅与线程池定时采集
│   └── Fengluo-Monitor-Service/    # MXBean 封装、诊断接口及统一线程池快照适配
├── Fengluo-Task/                   # 任务定义、分组与执行记录
├── Fengluo-Notice/                 # 通知模块，待建设
├── Fengluo-Search/                 # Elasticsearch 搜索服务基础
├── Fengluo-User/                   # 账号、用户、角色与菜单
├── Fengluo-Auth/auth-service/      # 共享认证授权组件
├── Geteway/                       # 网关，沿用当前目录拼写
├── FengLuo-Api/                   # 服务间调用基础依赖
├── Fengluo-Common/                # 统一响应、异常与公共工具
├── sql/                           # 本地数据库脚本，当前被 Git 忽略
└── pom.xml                        # Maven 聚合构建与版本管理
```

## 快速体验动态线程池

### 1. 环境与构建

准备 JDK 17、Maven 3.x。在仓库根目录构建模块并安装到本地 Maven 仓库：

```shell
mvn -pl Fengluo-Observability-Starter -am install
```

本地线程池管理不需要启动 MySQL、Redis 或 Nacos。线程池实现位于 Fengluo_DynamicTP，Bean 自动配置由 Observability-Starter 提供；当前依赖链尚不是独立的纯 JDK Core。

### 2. 在 Spring Boot 应用中引入依赖

```xml
<dependency>
    <groupId>com.fengluo</groupId>
    <artifactId>Fengluo-Observability-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

当前示例使用本地安装的 SNAPSHOT 构件，不假设已发布到公共 Maven 仓库。

启用自动注册：

```yaml
monitor:
  threadpool:
    dynamic:
      enabled: true
```

### 3. 注册业务线程池

在业务配置类中声明：

```java
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration(proxyBeanMethods = false)
public class BusinessPoolConfiguration {
    @DynamicThreadPool(name = "field_TP")
    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    1,
                    1,
                    20,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10)
            );

    @DynamicThreadPool(name = "dynamicTP1")
    public ExecutorService createTP() {
        return Executors.newFixedThreadPool(1);
    }
    @Bean("dynamicTP2")
    @DynamicThreadPool(name = "dynamicTP2")
    public ExecutorService createTP2() {
        return Executors.newFixedThreadPool(2);
    }
    //以dynamicTP3注册Bean与动态线程池
    @Bean
    @DynamicThreadPool(name = "dynamicTP3")
    public ExecutorService createTP3() {
        return Executors.newFixedThreadPool(2);
    }
}
```

显式 Bean 名为空时，依次使用 `@DynamicThreadPool(name)`、DynamicPool 内部名称、自动生成的 `dynamic_tp_N`。回退名称通过 Spring 别名暴露，原方法名保留。需要按名称注入时，建议显式填写 Bean 名。

### 4. 查询与调整

在线程池完成注册后调用：

```java
// 与业务提交任务时使用的是同一个线程池对象。
var executor = PoolInquirer.of().getPool("dynamicTP2");

// 核心线程数 4、最大线程数 12、存活时间 60 秒、队列容量 200。
PoolUpdater.of()
    .update("dynamicTP2", 4, 12, 60, TimeUnit.SECONDS, null);

// 返回本地状态快照，目前不自动发送到远程监控平台。
// Spring 应用中可直接注入 PoolReporter；手动创建时传入 Environment。
var snapshot = new PoolReporter(environment).snapshot();
```

示例沿用前文注册的 `dynamicTP2`，其固定容量队列不支持动态缩放，所以队列容量传 `null` 保持不变。使用自定义 CapacityBlockingQueue 时可传入新容量；缩容不删除已有任务，数量降到新容量以下后才继续接收入队。

## 接入可观测性 Starter

先在仓库根目录安装 Starter 及其依赖：

```shell
mvn -pl Fengluo-Observability-Starter -am install
```

业务应用引入以下依赖；构件名称中的 `starter` 为小写：

```xml
<dependency>
    <groupId>com.fengluo</groupId>
    <artifactId>Fengluo-Observability-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Starter 包含 Actuator 和 Prometheus Registry。基础 JVM、Buffer、类加载、线程及 CPU 指标沿用 Spring Boot 自动配置；SDK 使用 `fengluo.*` 前缀注册补充指标。Prometheus 使用当前 `prometheusmetrics` 实现，不再同时引入旧版 simpleclient Registry。

```yaml
monitor:
  metrics:
    enabled: true
    jvm-enabled: true
    gc-enabled: true
    executor-enabled: true
    executor-refresh-interval: 10s
  threadpool:
    dynamic:
      enabled: true
  exception:
    enabled: true
  jvm:
    extras:
      enabled: true
  banner:
    enabled: true
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

上述 YAML 适用于已有 Web 服务。启动后访问 `/actuator/prometheus`，若服务配置了 context-path，需在路径前加上该前缀。例如用户服务为 `/maple/actuator/prometheus`。可在 Prometheus 文本中搜索 `fengluo_`；端点访问权限由业务应用的安全配置决定。

### 配置开关

| 配置项 | 默认值 | 作用 |
| --- | --- | --- |
| `monitor.metrics.enabled` | `true` | 控制 SDK 补充指标的自动配置 |
| `monitor.metrics.jvm-enabled` | `true` | 内存池峰值/GC 后用量、阈值、进程内存和 CPU 时间、死锁数量 |
| `monitor.metrics.gc-enabled` | `true` | 订阅 GC 通知并记录带原因和动作的事件 Timer |
| `monitor.metrics.executor-enabled` | `true` | 存在 PoolReporter Bean 时，采集已注册的存活线程池 |
| `monitor.metrics.executor-refresh-interval` | `10s` | 线程池快照与发现间隔，至少 `1ms` |
| `monitor.threadpool.dynamic.enabled` | `false` | 启用注解注册、调参和 PoolReporter 等线程池管理组件 |
| `monitor.jvm.extras.enabled` | `true` | 原有 micrometer-jvm-extras 进程扩展指标 |
| `monitor.exception.enabled` | `true` | 存在 MeterRegistry 的 Servlet 应用启用异常处理；切面另需 AOP 支持 |
| `monitor.banner.enabled` | `true` | 启动时打印 classpath 中的 OB 横幅 |

关闭 `monitor.metrics.enabled` 只关闭 SDK 自动配置；Actuator 基础指标、原有进程扩展指标和异常处理由各自配置控制。应用可定义同类型的 Binder Bean 替换默认实现。

`monitor.metrics.*` 通过 `MonitorMetricsProperties` 类型化绑定，配置元数据提供 IDE 提示。例如 `executor-refresh-interval: 10s` 对应 Java 的 `Duration.ofSeconds(10)`；配置统一使用上表的短横线命名。

### 自动配置与 Bean 归属

OB 模块通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 加载以下五个正式入口。业务应用引入 Starter 后无需扫描其配置包或手动导入配置类。

| 自动配置类（`com.guilu.config` 包） | 注册职责 | 条件与顺序 |
| --- | --- | --- |
| `DynamicPoolAutoConfiguration` | 线程池注册后处理器、PoolRegister、PoolInquirer、PoolUpdater、PoolReporter、PoolLifecycle 和更新监听器 | `monitor.threadpool.dynamic.enabled=true` |
| `MonitorMetricsAutoConfiguration` | JvmSupplementMetrics、GcEventMetrics、ExecutorPoolMetrics 和 MonitorMetricsProperties | 在线程池配置之后加载；总开关与各指标开关分别生效，Executor Binder 另需 PoolReporter |
| `JvmExtrasAutoConfiguration` | ProcessMemoryMetrics、ProcessThreadMetrics、缺失时的 JvmInfoMetrics | 在 Boot 的 JVM 配置之后加载，受 `monitor.jvm.extras.enabled` 控制 |
| `GlobalExceptionAutoConfiguration` | GlobalExceptionHandler 和条件化的 GlobalExceptionAspect | 在 Registry 与 AOP 配置之后加载；需要 Servlet 环境及 MeterRegistry，切面另需 AOP 自动代理器且 `spring.aop.auto` 未关闭 |
| `ObservabilityAutoConfiguration` | 启动横幅 ApplicationRunner | 受 `monitor.banner.enabled` 控制，从 classpath 读取资源 |

各功能 Bean 使用 `@ConditionalOnMissingBean`，应用提供同类型 Bean 时默认注册退让。线程池管理配置保留 `ThreadPoolUpdater`、`ThreadPoolInquirer`、`ThreadPoolReporter` 等既有 Bean 别名。

`ThreadRegisterConfiguration` 与 `MonitorMetricsConfiguration` 为已弃用的显式导入兼容入口，分别转交线程池和指标正式配置；它们不再出现在自动配置清单中。新接入直接使用 Starter 和配置开关即可。禁用 `spring.aop.auto` 后，符合条件的异常处理器仍可注册，但不创建默认异常切面。

### 补充指标清单

| Micrometer 名称 | 类型、单位及标签 |
| --- | --- |
| `fengluo.jvm.memory.pool.peak` / `.collection` | Gauge，字节；`pool`、`area`、`value` 标签；value 为 init/used/committed/max |
| `fengluo.jvm.memory.pool.threshold.supported` / `.bytes` / `.count` / `.exceeded` | Gauge；`pool`、`kind` 标签；kind 为 usage/collection；bytes 单位为字节，其余为状态或累计次数 |
| `fengluo.system.memory.total` / `.free` | Gauge，字节 |
| `fengluo.process.memory.virtual` | Gauge，字节 |
| `fengluo.process.cpu.time` | Gauge，进程累计 CPU 秒数 |
| `fengluo.jvm.threads.deadlocked` | Gauge，死锁线程数量 |
| `fengluo.jvm.gc.event` | Timer；`collector`、`cause`、`action` 标签；记录 GC 事件次数和耗时 |
| `fengluo.executor.size` / `.core` / `.max` / `.largest` / `.active` | Gauge，线程数；`pool` 标签 |
| `fengluo.executor.queue.size` / `.queue.remaining` | Gauge，队列任务数及剩余容量；`pool` 标签 |
| `fengluo.executor.tasks` / `.completed` | Gauge，任务累计数量快照；`pool` 标签 |

不支持或未知的数值输出为 NaN，阈值为 0 表示阈值检查关闭。采集不修改内存阈值、不重置峰值，也不自动开启线程 CPU/锁争用监控。GC Timer 仅统计订阅后的事件，输入毫秒由 Micrometer 转换为 Registry 的时间单位；Prometheus 输出为秒。

线程池指标复用 `PoolReporter` 和 `DynamicPoolSnapshot`：初始化时读取一次，之后定时发现新注册池并更新快照，移除不再上报的池对应指标。采集器只读线程池状态；关闭时停止自身采集线程并移除线程池指标，GC Binder 关闭时解除通知订阅。线程池自身的关闭仍由原有 PoolLifecycle 管理。

逐线程 CPU 时间、锁等待、死锁堆栈及完整 GC 前后内存详情保留在 Monitor-Service 诊断接口中，不自动注册线程 ID 或堆栈标签。GC 并发阶段耗时拆分需另接 JFR/GC 日志；任务执行和排队耗时需上层任务装饰器记录，当前 Binder 不包装业务任务。

### 手动使用 Monitor-SDK

只需要手动绑定指标时，引入 `com.fengluo:Fengluo-Monitor-SDK:1.0-SNAPSHOT`，无需启用 Starter 自动配置。以已有 `MeterRegistry registry` 和 `PoolReporter reporter` 为例：

```java
new JvmSupplementMetrics().bindTo(registry);
GcEventMetrics gc = new GcEventMetrics();
gc.bindTo(registry);
ExecutorPoolMetrics pools = new ExecutorPoolMetrics(reporter::snapshot, Duration.ofSeconds(10));
pools.bindTo(registry);
```

手动模式只绑定补充指标，基础 JVM 指标由调用方自行选择 Micrometer Binder。SDK 不负责创建 Registry、发布 HTTP 端点或推送远程平台。Starter 模式由 Spring 自动绑定并关闭 Binder，无需再次手动调用。

手动模式需在应用退出时调用 `pools.close()` 和 `gc.close()`，释放定时采集线程和 GC 通知订阅。Binder 不负责关闭传入的 Registry 或业务线程池。

### 构建与验证

JDK 17 下运行以下命令，验证采集层、SDK 和 Starter：

```shell
mvn -pl Fengluo-Observability-Starter -am test
```

测试覆盖线程池的动态发现、快照更新、指标删除和重新注册，多 Registry 绑定与关闭，GC 事件时间单位，以及自动配置开关、Bean 替换和 Prometheus 抓取输出。

## 启动业务服务

账号与网关服务需要另行准备环境：

1. 准备 Nacos、MySQL 和 Redis，检查各模块 `bootstrap.yml` / `bootstrap-dev.yml`。
2. 在 Nacos 中准备配置引用的共享配置
3. 准备用户、角色、菜单及关联表，初始化管理员和 `/maple/...` 权限，确认权限缓存已发布。
4. 编译所需服务：`mvn -pl Geteway,Fengluo-User -am compile`。
5. 在 IDE 中先运行 `com.guilu.UserApplication`，再运行 `com.guilu.GateWayApplication`。

| 入口 | 默认地址 |
|---|---|
| 网关 | `http://localhost:10000` |
| 用户服务 | `http://localhost:10010` |
| 经网关登录 | `POST http://localhost:10000/maple/user/login` |
| 直连用户服务登录 | `POST http://localhost:10010/maple/user/login` |

上述路径依赖用户服务配置的 `/maple` context-path；网关保留此前缀。地址和端口以实际配置为准。登录需要验证码，不能只提交用户名和密码。



## 当前限制与后续方向

- 注册表使用 JVM 级静态状态，尚不支持多个独立 ApplicationContext 隔离。
- Spring 接入和核心实现仍在同一模块，后续计划拆分 Core 与 Starter。
- 已注册线程池会参与模块的关闭管理，外部共享线程池应明确生命周期所有权。
- ThreadPoolTaskExecutor 适配的是底层线程池；外层包装器配置字段不随底层修改自动同步。
- 本地调参不代表跨实例同步；Nacos 下发、配置版本、实例应用结果与失败补偿待实现。
- 任务调度引擎集成、通知投递、集中监控和业务日志检索仍需继续建设。

后续优先完善线程池管理的接入体验与可靠性，再实现远程配置闭环；性能与长时间稳定性需要独立验证。
