package me.wonsik.order.demo.order.domain.test

import me.wonsik.order.demo.order.adapter.jpa.BooleanToYnConverter
import javax.persistence.*


/* Chapter 14 */
@Entity
@Convert(converter = BooleanToYnConverter::class, attributeName = "vip")
@NamedEntityGraph(name = "TestItem.withDiamonds", attributeNodes = [
    NamedAttributeNode("diamonds", subgraph = "duck"),
], subgraphs = [NamedSubgraph(name = "duck", attributeNodes = [
    NamedAttributeNode("duck")
])]
)
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
    val item: TestItem,
    @OneToOne(optional = true)
    @JoinColumn(name = "duck_sequence")
    val duck: Duck? = null
){}

@Entity
class Duck (
    @GeneratedValue
    @Id
    val sequence: Long?,
    @Column
    var name: String
) {
    @PrePersist
    fun prePersist() {
        println("Duck#prePersist sequence=${sequence}, name=${name}")
    }

    @PostPersist
    fun postPersist() {
        println("Duck#postPersist sequence=${sequence}, name=${name}")
    }

    @PostLoad
    fun postLoad() {
        println("Duck#postLoad sequence=${sequence}, name=${name}")
    }

    @PreUpdate
    fun preUpdate() {
        println("Duck#preUpdate sequence=${sequence}, name=${name}")
    }

    @PostUpdate
    fun postUpdate() {
        println("Duck#postUpdate sequence=${sequence}, name=${name}")
    }

    @PreRemove
    fun preRemove() {
        println("Duck#preRemove sequence=${sequence}, name=${name}")
    }

    @PostRemove
    fun postRemove() {
        println("Duck#postRemove sequence=${sequence}, name=${name}")
    }
}