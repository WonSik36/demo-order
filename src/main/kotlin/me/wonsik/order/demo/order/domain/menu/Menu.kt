package me.wonsik.order.demo.order.domain.menu

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import me.wonsik.order.demo.order.domain.common.BaseEntity
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "menu")
class Menu(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long? = null,
    @Column var name: String,
    @Column var description: String,
    @ManyToOne
    @JoinColumn(name = "restaurant_sequence", nullable = false)
    val restaurant: Restaurant
) : BaseEntity() {

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    override fun toString() = kotlinToString(properties = toStringProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(Menu::sequence)
        private val toStringProperties = arrayOf(Menu::sequence, Menu::name)
    }
}