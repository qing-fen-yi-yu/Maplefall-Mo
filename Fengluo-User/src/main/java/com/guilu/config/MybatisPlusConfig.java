package com.guilu.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 * <p>
 * 分页插件必须显式注册：{@code IService.page(...)} 没有它也能跑，但不会给 SQL 拼
 * {@code LIMIT}，返回的是<b>全部</b>匹配行且 {@code total} 恒为 0。
 * 前端看到 total=0 会直接判成「无数据」，这类故障不报错、只静默出错，很难排查。
 * </p>
 * <p>
 * 注意依赖：MyBatis-Plus 3.5.9 起 {@link PaginationInnerInterceptor} 已从
 * {@code mybatis-plus-extension} 拆到 {@code mybatis-plus-jsqlparser}，且不再传递依赖，
 * 因此 pom 里要显式引入该模块（见 Fengluo-User/pom.xml）。
 * </p>
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 页码越界时返回空集合，而不是静默回到第一页：
        // 返回首页会让前端的页码与内容对不上，属于「看起来正常」的错误数据。
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);
        // 如需限制单页上限，可另加 pagination.setMaxLimit(500L)；
        // 当前不加，避免大 pageSize 请求被静默截断。
        return interceptor;
    }
}
