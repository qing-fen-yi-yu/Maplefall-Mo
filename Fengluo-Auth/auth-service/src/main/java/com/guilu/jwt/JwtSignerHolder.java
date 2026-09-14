package com.guilu.jwt;

import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.guilu.ThreadPoolOb.annotation.DynamicThreadPool;
import com.guilu.constants.JwtConstants;
import com.guilu.utils.MarkedRunnable;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
@Component
public class JwtSignerHolder {
    private volatile JWTSigner jwtSigner;
    private DiscoveryClient discoveryClient;

    public JwtSignerHolder(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @DynamicThreadPool(name = "AuthFetchJwkThread")
    private final ExecutorService ses = new ThreadPoolExecutor(
            1,
            1,
            10,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1),
            r -> new Thread(r, "AuthFetchJwkThread")
    );

    @PostConstruct
    public void init(){
        // 尝试获取jwk秘钥
        ses.submit(new MarkedRunnable(new JwkTask(discoveryClient)));
    }

    public void shutdown(){
        ses.shutdown();
        log.debug("销毁加载秘钥线程 AuthFetchJwkThread");
    }
    public static void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    class JwkTask implements Runnable{
        private final DiscoveryClient discoveryClient;

        public JwkTask(DiscoveryClient discoveryClient) {
            this.discoveryClient = discoveryClient;
        }

        @Override
        public void run() {
            while (jwtSigner == null) {
                try {
                    KeyStore ks = KeyStore.getInstance("JKS");
                    try (InputStream is = getClass().getClassLoader().getResourceAsStream("MapleFall.jks")) {
                        ks.load(is, "maplefall123456".toCharArray());
                    }
                    String alias = "tutorialspedia";  // 和 yml 保持一致，或从配置注入
                    Certificate cert = ks.getCertificate(alias);
                    if (cert == null) {
                        log.error("jks 中不存在别名 {} 的证书", alias);
                        sleep(1000);
                        continue;
                    }
                    PublicKey publicKey = cert.getPublicKey();
                    jwtSigner = JWTSignerUtil.createSigner(JwtConstants.JWT_ALGORITHM, publicKey);
                } catch (Exception e) {
                    log.error("获取jwk秘钥失败", e);
                    sleep(1000);
                }
            }
            // 关闭线程池
            shutdown();
        }
    }
}
