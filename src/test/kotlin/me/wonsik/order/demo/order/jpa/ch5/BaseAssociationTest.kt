package me.wonsik.order.demo.order.jpa.ch5

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class BaseAssociationTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)

    init {
        "단방향 연관관계" - {
            "저장" {
                tx {
                    val restaurant = Restaurant(null, "res1", address)
                    persist(restaurant)

                    val menu1 = Menu(null, "menu1", "description", restaurant)
                    val menu2 = Menu(null, "menu2", "description", restaurant)

                    persist(menu1)
                    persist(menu2)
                }
            }

            "조회" - {
                "객체 그래프 탐색" {
                    var sequence: Long? = null
                    tx {
                        val restaurant = Restaurant(null, "res1", address)
                        persist(restaurant)

                        val menu = Menu(null, "menu", "description", restaurant)
                        persist(menu)

                        sequence = menu.sequence
                    }

                    tx {
                        val menu = find(Menu::class.java, sequence)
                        menu.name shouldBe "menu"

                        val restaurant = menu.restaurant
                        restaurant.name shouldBe "res1"
                    }
                }

                "객체지향 쿼리 사용" {
                    var sequence: Long? = null
                    tx {
                        val restaurant = Restaurant(null, "res1", address)
                        persist(restaurant)

                        val menu = Menu(null, "menu", "description", restaurant)
                        persist(menu)

                        sequence = menu.sequence
                    }

                    tx {
                        val jpql = "select m from Menu m join fetch m.restaurant r where m.sequence=:sequence"

                        val resultList = createQuery(jpql, Menu::class.java)
                            .setParameter("sequence", sequence)
                            .resultList

                        resultList shouldHaveSize 1
                        resultList[0].name shouldBe "menu"
                        resultList[0].restaurant.name shouldBe "res1"
                    }
                }
            }

            "삭제" {
                tx {
                    val restaurant = Restaurant(null, "res1", address)
                    persist(restaurant)

                    val menu = Menu(null, "menu", "description", restaurant)
                    persist(menu)

                    remove(menu)
                    remove(restaurant)
                }
            }
        }

        "양방향 연관 관계" - {
            "저장" {
                var sequence: Long? = null
                tx {
                    val restaurant = Restaurant(null, "res1", address)
                    persist(restaurant)
                    sequence = restaurant.sequence

                    val menu1 = Menu(null, "menu1", "description", restaurant)
                    val menu2 = Menu(null, "menu2", "description", restaurant)

                    persist(menu1)
                    persist(menu2)

                    restaurant.menus shouldHaveSize 0
                }

                tx {
                    val restaurant = find(Restaurant::class.java, sequence)

                    restaurant.menus shouldHaveSize 2   // select query
                }
            }

            "연관 관계의 주인이 아닌곳에서 관계 설정시" {
                var sequence: Long? = null
                tx {
                    val restaurant = Restaurant(null, "res1", address)
                    persist(restaurant)
                    sequence = restaurant.sequence

                    val menu1 = Menu(null, "menu1", "description", restaurant)
                    val menu2 = Menu(null, "menu2", "description", restaurant)

                    persist(menu1)
                    persist(menu2)

                    restaurant.menus shouldHaveSize 0
                }

                tx {
                    val restaurant = find(Restaurant::class.java, sequence)

                    restaurant.menus shouldHaveSize 2   // select query
                }
            }
        }
    }

    private inline fun tx(logic: EntityManager.() -> Unit) {
        val em = emf.createEntityManager()  // thread-unsafe
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