package me.wonsik.order.demo.order.adapter.datasource

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource


/**
 * @author 정원식 (wonsik.cheung)
 */
@Configuration
class DataSourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    fun dataSource(): DataSource = HikariDataSource()
}