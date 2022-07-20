package me.wonsik.order.demo.order.jpa.ch14

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.adapter.querydsl.QueryDslConfig
import me.wonsik.order.demo.order.domain.test.TestAlbum
import me.wonsik.order.demo.order.domain.test.TestBook
import me.wonsik.order.demo.order.domain.test.TestCherry
import me.wonsik.order.demo.order.domain.test.TestItem
import org.hibernate.collection.internal.PersistentBag
import org.hibernate.collection.internal.PersistentList
import org.hibernate.collection.internal.PersistentSet
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
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
}