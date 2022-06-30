package me.wonsik.order.demo.order.domain.restaurant

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.common.BaseEntity
import me.wonsik.order.demo.order.domain.menu.Menu
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "restaurant")
class Restaurant(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long? = null,
    @Column val name: String,
    @Embedded val address: Address,
    @OneToMany(
        mappedBy = "restaurant",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true
    )
    val menus: MutableList<Menu> = arrayListOf()
) : BaseEntity() {

    fun makeMenu(name: String, description: String): Menu {
        val menu = Menu(null, name, description, this)
        menus.add(menu)
        return menu
    }


    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    override fun toString() = kotlinToString(properties = toStringProperties)

    companion object {
        private val equalsAndHashCodeProperties = arrayOf(Restaurant::sequence)
        private val toStringProperties = arrayOf(Restaurant::sequence, Restaurant::name)
    }
}