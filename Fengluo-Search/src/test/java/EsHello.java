import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class EsHello {
    private RestHighLevelClient restHighLevelClient;
    //初始化方法
    @BeforeEach
    public void init() {
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://192.168.150.104:9200"))
        );
    }
    @Test
    void test(){
        System.out.println(restHighLevelClient);
    }
    @AfterEach
    public void close() throws IOException {
        this.restHighLevelClient.close();
    }
}
