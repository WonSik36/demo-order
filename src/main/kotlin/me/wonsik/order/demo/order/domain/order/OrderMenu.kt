package me.wonsik.order.demo.order.domain.order

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import me.wonsik.order.demo.order.domain.common.BaseEntity
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(
    name = "order_menu",
    indexes = [
        Index(columnList = "order_sequence, menu_sequence")
    ]
)
class OrderMenu(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long? = null,
    @ManyToOne @JoinColumn(name = "order_sequence", nullable = false) val order: Order,
    @ManyToOne @JoinColumn(name = "menu_sequence", nullable = false) val menu: Menu,
    @Column val count: Int
) : BaseEntity() {

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    override fun toString() = kotlinToString(properties = toStringProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(OrderMenu::sequence)
        private val toStringProperties = arrayOf(OrderMenu::sequence, OrderMenu::count)
    }
}