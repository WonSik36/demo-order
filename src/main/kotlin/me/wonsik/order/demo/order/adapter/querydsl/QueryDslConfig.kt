package me.wonsik.order.demo.order.adapter.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


/**
 * @author 정원식 (wonsik.cheung)
 */
@Configuration
class QueryDslConfig {
    @PersistenceContext
    private lateinit var em: EntityManager

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(em)
    }
}