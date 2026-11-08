package com.guilu.client.Web;

import com.guilu.Constants.ClientConstant;
import com.guilu.annotation.DynamicThreadPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

//Ob端请求调度器
@Slf4j
@RequiredArgsConstructor
public class WebDispatchClient {

    private final WebClient webClient;
    private final Semaphore semaphore;
    @DynamicThreadPool("webDispatchExecutor")
    private final ExecutorService dispatchPool = Executors.newFixedThreadPool(10);

    public List<CompletableFuture<WebClientResultInfo>> updateDispatch(List<ServiceInstance> instances, Object config, String header, String uri) {
        log.info("调度请求, instances={} header={}, uri={}",
                instances, header, uri);
        if (instances == null || instances.isEmpty()) {
            return List.of();
        }
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("POINT_HEADER_REQUEST 不能为空");
        }
        Consumer<HttpHeaders> headersConsumer =
                h -> h.set(ClientConstant.POINT_HEADER_REQUEST, header);

        return instances.stream()
                .map(inst -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return new WebClientResultInfo(false, null, "interrupted");
                    }
                    try {
                        Boolean ok = webClient.post()
                                .uri(uriBuilder -> uriBuilder
                                        .scheme("http")
                                        .host(inst.getHost())
                                        .port(inst.getPort())
                                        .path(uri)
                                        .build())
                                .headers(headersConsumer)
                                .bodyValue(config)
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .timeout(Duration.ofSeconds(2))
                                .block();
                        boolean success = Boolean.TRUE.equals(ok);
                        return success
                                ? new WebClientResultInfo(true, config, null)
                                : new WebClientResultInfo(false, config, "updater rejected");
                    } catch (Exception e) {
                        log.error("调度失败, instance={}, config={}",
                                inst.getHost() + ":" + inst.getPort(), config, e);
                        return new WebClientResultInfo(false, config, e.getMessage());
                    } finally {
                        semaphore.release();
                    }
                }, dispatchPool))
                .toList();
    }
}
