package com.guilu.jwt;

import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.guilu.ThreadPoolOb.annotation.DynamicThreadPool;
import com.guilu.constants.JwtConstants;
import com.guilu.utils.MarkedRunnable;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
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
    /** 验签使用（公钥），校验 token 时使用 */
    private volatile JWTSigner jwtSigner;
    /** 签发使用（私钥），业务模块登录签发 token 时使用 */
    private volatile JWTSigner signSigner;
    private DiscoveryClient discoveryClient;

    @Value("${encrypt.key-store.location:classpath:MapleFall.jks}")
    private String keyStoreLocation;
    @Value("${encrypt.key-store.alias:tutorialspedia}")
    private String keyStoreAlias;
    @Value("${encrypt.key-store.password:maplefall123456}")
    private String keyStorePassword;

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
                    String location = keyStoreLocation.replace("classpath:", "");
                    try (InputStream is = new ClassPathResource(location).getInputStream()) {
                        ks.load(is, keyStorePassword.toCharArray());
                    }
                    Certificate cert = ks.getCertificate(keyStoreAlias);
                    if (cert == null) {
                        log.error("jks 中不存在别名 {} 的证书", keyStoreAlias);
                        sleep(1000);
                        continue;
                    }
                    PublicKey publicKey = cert.getPublicKey();
                    // 私钥用于签发 token，仅持有公钥的节点无法签发（降级为只校验）
                    Key key = ks.getKey(keyStoreAlias, keyStorePassword.toCharArray());
                    // 先写 signSigner 再写 jwtSigner：两者都是 volatile，
                    // 这样凡是看到 jwtSigner 已就绪的线程也一定能看到 signSigner，避免签发时空指针
                    if (key instanceof PrivateKey privateKey) {
                        signSigner = JWTSignerUtil.createSigner(JwtConstants.JWT_ALGORITHM, privateKey);
                    } else {
                        log.warn("keystore 别名 {} 无可用私钥，当前节点无法签发 token", keyStoreAlias);
                    }
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
