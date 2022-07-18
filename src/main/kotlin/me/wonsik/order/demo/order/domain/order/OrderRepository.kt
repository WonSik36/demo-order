package me.wonsik.order.demo.order.domain.order

import com.querydsl.jpa.impl.JPAQueryFactory
import me.wonsik.order.demo.order.domain.menu.QMenu
import me.wonsik.order.demo.order.domain.restaurant.QRestaurant
import me.wonsik.order.demo.order.domain.user.QUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.util.*


/**
 * @author 정원식 (wonsik.cheung)
 */
interface OrderRepository : JpaRepository<Order, Long>, QuerydslPredicateExecutor<Order>, OrderRepositoryCustom {
}

/* 사용자 정의 리포지토리 구현 */
// 사용자 정의 인터페이스 정의
interface OrderRepositoryCustom {
    fun findFetchedOrderById(sequence: Long): Optional<Order>

    fun findFetchedOrderByIdContains(sequences: List<Long>): List<Order>
}

// 사용자 정의 인터페이스 구현 클래스 작성
// 타겟 리포지터리 인터페이스 이름 + Impl
class OrderRepositoryImpl(private val factory: JPAQueryFactory) : OrderRepositoryCustom, QuerydslRepositorySupport(Order::class.java) {

    // QuerydslRepositorySupport
    override fun findFetchedOrderById(sequence: Long): Optional<Order> {
         val order = from(QOrder.order)
            .join(QOrder.order.user, QUser.user).fetchJoin()
            .join(QOrder.order.orderMenus, QOrderMenu.orderMenu).fetchJoin()
            .join(QOrderMenu.orderMenu.menu, QMenu.menu).fetchJoin()
            .join(QMenu.menu.restaurant, QRestaurant.restaurant).fetchJoin()
            .where(QOrder.order.sequence.eq(sequence))
            .fetchOne()

        return Optional.ofNullable(order)
    }

    // JPAQueryFactory
    override fun findFetchedOrderByIdContains(sequences: List<Long>): List<Order> {
        return factory.select(QOrder.order)
            .from(QOrder.order)
            .join(QOrder.order.user, QUser.user).fetchJoin()
            .join(QOrder.order.orderMenus, QOrderMenu.orderMenu).fetchJoin()
            .join(QOrderMenu.orderMenu.menu, QMenu.menu).fetchJoin()
            .join(QMenu.menu.restaurant, QRestaurant.restaurant).fetchJoin()
            .where(QOrder.order.sequence.`in`(sequences))
            .fetch()
    }
}

