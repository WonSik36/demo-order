package me.wonsik.order.demo.order.domain.order

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import me.wonsik.order.demo.order.domain.common.BaseEntity
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.user.User
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long? = null,
    @ManyToOne @JoinColumn(name = "member_sequence", nullable = false) val user: User,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val orderMenus: MutableList<OrderMenu> = arrayListOf(),
    @Enumerated(EnumType.STRING) var status: OrderStatus
) : BaseEntity() {

    fun addMenu(menu: Menu, count: Int = 1) {
        val orderMenu = OrderMenu(order = this, menu =  menu, count = count)
        orderMenus.add(orderMenu)
    }

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    override fun toString() = kotlinToString(properties = toStringProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(Order::sequence)
        private val toStringProperties = arrayOf(Order::sequence, Order::status)
    }
}