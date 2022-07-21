package me.wonsik.order.demo.order.adapter.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import me.wonsik.order.demo.order.domain.test.TestDiamond
import me.wonsik.order.demo.order.domain.test.TestItem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityGraph
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

    /* Chapter 14 */
    @Bean("entity.graph.static")
    fun namedEntityGraph(): EntityGraph<TestItem> {
        @Suppress("UNCHECKED_CAST")
        return em.getEntityGraph("TestItem.withDiamonds") as EntityGraph<TestItem>
    }

    @Bean("entity.graph.dynamic")
    fun dynamicEntityGraph(): EntityGraph<TestItem> {
        val graph = em.createEntityGraph(TestItem::class.java)
        val subGraph = graph.addSubgraph<TestDiamond>("diamonds")
        subGraph.addAttributeNodes("duck")
        return graph
    }
}