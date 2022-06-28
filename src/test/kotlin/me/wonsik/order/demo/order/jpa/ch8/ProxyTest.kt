package me.wonsik.order.demo.order.jpa.ch8

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.hibernate.LazyInitializationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
class ProxyTest: FreeSpec() {
    override fun extensions() = listOf(SpringExtension)


    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)

    private var userSequence : Long? = null
    private var restaurantSequence : Long? = null
    private var menuSequence : Long? = null


    override suspend fun beforeEach(testCase: TestCase) {
        tx {
            val user = makeUser()
            persist(user)
            userSequence = user.sequence

            val restaurant = Restaurant(null, "restaurant", address)
            persist(restaurant)
            restaurantSequence = restaurant.sequence

            val menu = Menu(null, "menu", "description", restaurant)
            persist(menu)
            menuSequence = menu.sequence
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tx {
            createQuery("delete from User u").executeUpdate()
            createQuery("delete from Menu m").executeUpdate()
            createQuery("delete from Restaurant r").executeUpdate()
        }
        userSequence = null
        restaurantSequence = null
        menuSequence = null
    }

    init {
        "프록시" - {
            "find" {
                tx {
                    val targetMember = find(User::class.java, userSequence)
                    targetMember.isLoaded() shouldBe true
                }
            }

            "getReference" {
                tx {
                    val proxiedUser = getReference(User::class.java, userSequence)
                    proxiedUser.isLoaded() shouldBe false

                    proxiedUser.sequence
                    proxiedUser.isLoaded() shouldBe false

                    proxiedUser.name
                    proxiedUser.isLoaded() shouldBe true
                }
            }

            "이미 영속 상태에서 프록시 조회시, 엔티티 반환" {
                tx {
                    val targetMember = find(User::class.java, userSequence)
                    targetMember.isLoaded() shouldBe true

                    val proxiedUser = getReference(User::class.java, userSequence)
                    proxiedUser.isLoaded() shouldBe true
                }
            }

            "준영속 상태에서 초기화시" {
                var proxiedUser: User? = null
                tx {
                    proxiedUser = getReference(User::class.java, userSequence)
                }
                shouldThrow<LazyInitializationException> {
                    proxiedUser?.name
                }
            }

            "연관 관계 설정시 프록시로도 가능" {
                tx {
                    val proxiedRestaurant = getReference(Restaurant::class.java, restaurantSequence)
                    val menu = Menu(null, "menu", "description", proxiedRestaurant)
                    persist(menu)

                    proxiedRestaurant.isLoaded() shouldBe false
                }
            }
        }

        "즉시 로딩, 지연 로딩" - {
            "즉시 로딩" {
                tx {
                    val menu = find(Menu::class.java, menuSequence)

                    menu.restaurant.isLoaded() shouldBe true
                }
            }

            "지연 로딩" {
                tx {
                    val restaurant = find(Restaurant::class.java, restaurantSequence)

                    restaurant.menus.isLoaded() shouldBe false

                    restaurant.menus[0]
                    restaurant.menus.isLoaded() shouldBe true
                }
            }
        }

        "영속성 전이" - {
            "PERSIST" {
                tx {
                    val restaurant = Restaurant(null, "restaurant", address)
                    val menu = restaurant.makeMenu("menu", "new menu")

                    persist(restaurant)
                    restaurant.isLoaded() shouldBe true
                    menu.isLoaded() shouldBe true
                }
            }

            "REMOVE" {
                tx {
                    val restaurant = Restaurant(null, "restaurant", address)
                    val menu = restaurant.makeMenu("menu", "new menu")
                    persist(restaurant)

                    remove(restaurant)
                }

                tx {
                    val count = createQuery("select count(m) from Menu m")
                        .singleResult as Long

                    count shouldNotBe 2
                    count shouldBe 1
                }
            }
        }

        "고아 객체 제거" {
            tx {
                val restaurant = find(Restaurant::class.java, restaurantSequence)
                restaurant.menus.clear()
            }

            tx {
                val count = createQuery("select count(m) from Menu m")
                    .singleResult as Long

                count shouldNotBe 1
                count shouldBe 0
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

    private fun Any.isLoaded() = emf.persistenceUnitUtil.isLoaded(this)

    private fun makeUser() = makeUser(null)

    private fun makeUser(id: Long?) = User(id, "alice", birthDay = null, Sex.FEMALE, "", address)
}