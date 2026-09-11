package com.guilu.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Override
    public ClientConfiguration clientConfiguration() {
        // 构建客户端配置
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder =
                ClientConfiguration.builder()
                        .connectedTo(elasticsearchUris);

        // 如果配置了用户名密码，添加认证
        if (username != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        // 配置连接超时等参数
        return builder
                .withConnectTimeout(5000)
                .withSocketTimeout(60000)
                .build();
    }

    /**
     * 配置 ElasticsearchClient Bean
     * 用于高级操作
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建低级客户端
        RestClient restClient = RestClient.builder(
                        HttpHost.create(elasticsearchUris))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    // 如果配置了用户名密码，添加认证
                    if (username != null && !username.isEmpty()) {
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(
                                AuthScope.ANY,
                                new UsernamePasswordCredentials(username, password)
                        );
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    return httpClientBuilder;
                })
                .build();

        // 使用Jackson作为JSON处理器
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}
