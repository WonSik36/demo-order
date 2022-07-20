package me.wonsik.order.demo.order.domain.test

import me.wonsik.order.demo.order.adapter.jpa.BooleanToYnConverter
import javax.persistence.*


/* Chapter 14 */
@Entity
@Convert(converter = BooleanToYnConverter::class, attributeName = "vip")
class TestItem(
    @GeneratedValue
    @Id
    val sequence: Long? = null,
    @OneToMany(mappedBy = "item")
    val albums: MutableCollection<TestAlbum>? = arrayListOf(),
    @OneToMany(mappedBy = "item")
    val books: MutableList<TestBook>? = arrayListOf(),
    @OneToMany(mappedBy = "item")
    val cherries: MutableSet<TestCherry>? = hashSetOf(),
    @OrderColumn(name = "orders")
    @OneToMany(mappedBy = "item")
    val diamonds: MutableList<TestDiamond>? = arrayListOf(),
    val vip: Boolean = false
) {}

@Entity
class TestAlbum (
    @GeneratedValue
    @Id
    val sequence: Long? = null,
    @ManyToOne
    @JoinColumn(name = "item_sequence")
    val item: TestItem
){}

@Entity
class TestBook (
    @GeneratedValue
    @Id
    val sequence: Long? = null,
    @ManyToOne
    @JoinColumn(name = "item_sequence")
    val item: TestItem
){}

@Entity
class TestCherry (
    @GeneratedValue
    @Id
    val sequence: Long? = null,
    @ManyToOne
    @JoinColumn(name = "item_sequence")
    val item: TestItem
){}

@Entity
class TestDiamond (
    @GeneratedValue
    @Id
    val sequence: Long? = null,
    @ManyToOne
    @JoinColumn(name = "item_sequence")
    val item: TestItem
){}

