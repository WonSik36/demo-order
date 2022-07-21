package me.wonsik.order.demo.order.jpa.ch14

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.adapter.querydsl.QueryDslConfig
import me.wonsik.order.demo.order.domain.test.*
import org.hibernate.collection.internal.PersistentBag
import org.hibernate.collection.internal.PersistentList
import org.hibernate.collection.internal.PersistentSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import javax.persistence.EntityGraph
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
@Import(QueryDslConfig::class)
class CollectionTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @PersistenceUnit
    private lateinit var emf: EntityManagerFactory

    @Autowired
    @Qualifier("entity.graph.static")
    private lateinit var staticEntityGraph: EntityGraph<TestItem>

    @Autowired
    @Qualifier("entity.graph.dynamic")
    private lateinit var dynamicEntityGraph: EntityGraph<TestItem>

    init {
        "Collections" - {
            "Collection - Null" {
                tx {
                    val item = TestItem(albums = null)
                    item.albums?.javaClass shouldBe null

                    persist(item)
                    item.albums?.javaClass shouldBe null
                }
            }

            "Collection - Not Null" {
                tx {
                    val item = TestItem()
                    item.albums?.javaClass shouldBe ArrayList::class.java

                    persist(item)
                    item.albums?.javaClass shouldBe PersistentBag::class.java
                }
            }

            "List - Null" {
                tx {
                    val item = TestItem(books = null)
                    item.books?.javaClass shouldBe null

                    persist(item)
                    item.books?.javaClass shouldBe null
                }
            }

            "List - Not Null" {
                tx {
                    val item = TestItem()
                    item.books?.javaClass shouldBe ArrayList::class.java

                    persist(item)
                    item.books?.javaClass shouldBe PersistentBag::class.java
                }
            }

            "Set - Null" {
                tx {
                    val item = TestItem(cherries = null)
                    item.cherries?.javaClass shouldBe null

                    persist(item)
                    item.cherries?.javaClass shouldBe null
                }
            }

            "Set - Not Null" {
                tx {
                    val item = TestItem()
                    item.cherries?.javaClass shouldBe HashSet::class.java

                    persist(item)
                    item.cherries?.javaClass shouldBe PersistentSet::class.java
                }
            }

            "@OrderColumn + List - Null" {
                tx {
                    val item = TestItem(diamonds = null)
                    item.diamonds?.javaClass shouldBe null

                    persist(item)
                    item.diamonds?.javaClass shouldBe null
                }
            }

            "@OrderColumn + List - Not Null" {
                tx {
                    val item = TestItem()
                    item.diamonds?.javaClass shouldBe ArrayList::class.java

                    persist(item)
                    item.diamonds?.javaClass shouldBe PersistentList::class.java
                }
            }

            "지연 로딩된 컬렉션 초기화" - {
                "Collection" {
                    var sequence: Long? = null
                    tx {
                        val item = TestItem()
                        persist(item)
                        sequence = item.sequence
                    }

                    tx {
                        val item = find(TestItem::class.java, sequence)
                        val album = TestAlbum(null, item)
                        persist(item)
                        persist(album)

                        item.albums?.add(album) // albums 를 조회하지 않음
                    }
                }

                "List" {
                    var sequence: Long? = null
                    tx {
                        val item = TestItem()
                        persist(item)
                        sequence = item.sequence
                    }

                    tx {
                        val item = find(TestItem::class.java, sequence)
                        val book = TestBook(null, item)
                        persist(item)
                        persist(book)

                        item.books?.add(book)   // books 를 조회하지 않음
                    }
                }

                "Set" {
                    var sequence: Long? = null
                    tx {
                        val item = TestItem()
                        persist(item)
                        sequence = item.sequence
                    }

                    tx {
                        val item = find(TestItem::class.java, sequence)
                        val cherry = TestCherry(null, item)
                        persist(item)
                        persist(cherry)

                        item.cherries?.add(cherry)  // cherries 를 조회함
                    }
                }
            }
        }

        "Converter" - {
            "True" {
                var sequence: Long? = null
                tx {
                    val item = TestItem(vip = true)
                    persist(item)
                    sequence = item.sequence
                }

                tx {
                    val item = find(TestItem::class.java, sequence)
                    item.vip shouldBe true
                }
            }

            "False" {
                var sequence: Long? = null
                tx {
                    val item = TestItem(vip = false)
                    persist(item)
                    sequence = item.sequence
                }

                tx {
                    val item = find(TestItem::class.java, sequence)
                    item.vip shouldBe false
                }
            }
        }

        "EntityListener" {
            var duck : Duck? = null

            tx {
                duck = Duck(null, "Duck")

                // PrePersist, sequence=null
                persist(duck)
                // PostPersist
            }

            tx {
                val newDuck = merge(duck)
                // PostLoad

                // PreUpdate
                newDuck?.name = "hello"
                flush()
                // PostUpdate

                // PreRemove
                remove(newDuck)
                flush()
                // PreRemove
            }
        }

        "EntityGraph" - {
            "NamedEntityGraph" {
                var sequence: Long? = null
                tx {
                    val duck = Duck(null, "Duck")
                    persist(duck)

                    val item = TestItem(null)
                    persist(item)
                    sequence = item.sequence

                    val diamond = TestDiamond(null, item, duck)
                    persist(diamond)

                    item.diamonds?.add(diamond)
                }

                tx {
                    val hints = hashMapOf<String, Any>("javax.persistence.fetchgraph" to staticEntityGraph)
                    val item = find(TestItem::class.java, sequence, hints)

                    item.cherries?.isLoaded() shouldBe false
                    item.diamonds?.isLoaded() shouldBe true
                    item.diamonds?.get(0)?.duck?.isLoaded() shouldBe true
                }
            }

            "동적 엔티티 그래프" {
                var sequence: Long? = null
                tx {
                    val duck = Duck(null, "Duck")
                    persist(duck)

                    val item = TestItem(null)
                    persist(item)
                    sequence = item.sequence

                    val diamond = TestDiamond(null, item, duck)
                    persist(diamond)

                    item.diamonds?.add(diamond)
                }

                tx {
                    val hints = hashMapOf<String, Any>("javax.persistence.fetchgraph" to dynamicEntityGraph)
                    val item = find(TestItem::class.java, sequence, hints)

                    item.cherries?.isLoaded() shouldBe false
                    item.diamonds?.isLoaded() shouldBe true
                    item.diamonds?.get(0)?.duck?.isLoaded() shouldBe true
                }
            }
        }
    }


    private inline fun tx(logic: EntityManager.() -> Unit) {
        val em = emf.createEntityManager()
        val tx = em.transaction

        try {
            tx.begin()
            logic(em)
            tx.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            tx.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    private fun Any.isLoaded() = emf.persistenceUnitUtil.isLoaded(this)
}