package me.wonsik.order.demo.order.jpa.ch10

import com.querydsl.core.NonUniqueResultException
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.menu.QMenu
import me.wonsik.order.demo.order.domain.order.Order
import me.wonsik.order.demo.order.domain.order.OrderStatus
import me.wonsik.order.demo.order.domain.order.QOrder
import me.wonsik.order.demo.order.domain.order.QOrderMenu
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import me.wonsik.order.demo.order.domain.user.QUser
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class QueryDslTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)


    private val entitiesSize = 5

    override suspend fun beforeEach(testCase: TestCase) {
        tx {
            makeUsers(entitiesSize)
                .forEach { persist(it) }
            makeRestaurantsAndMenus(entitiesSize)
                .forEach {persist(it)}
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tx {
            createQuery("delete from User u").executeUpdate()
            createQuery("delete from Menu m").executeUpdate()
            createQuery("delete from Restaurant r").executeUpdate()
        }
    }

    init {
        "WHERE" {
            tx {
                val factory = JPAQueryFactory(this)
                val qUser = QUser.user
                val users = factory.from(qUser)
                    .where(qUser.name.eq("name1").and(qUser.age.eq(10).or(qUser.email.eq("example1@example.com"))))
                    .orderBy(qUser.name.desc())
                    .fetch()

                users shouldHaveSize 1
            }
        }

        "결과 조회" - {
            "fetch" {
                tx {
                    val factory = JPAQueryFactory(this)
                    val qUser = QUser.user
                    val users = factory.from(qUser)
                        .orderBy(qUser.name.desc())
                        .fetch()    // 리스트 조회

                    users shouldHaveSize entitiesSize
                }
            }

            "fetchOne" {
                tx {
                    val factory = JPAQueryFactory(this)
                    val qUser = QUser.user
                    val user = factory.from(qUser)
                        .where(qUser.name.eq("NOT EXIST"))
                        .fetchOne() // 단건 조회 or null or NonUniqueResultException

                    user shouldBe null
                }
            }

            "fetchOne - Non Unique" {
                shouldThrow<NonUniqueResultException> {
                    tx {
                        val factory = JPAQueryFactory(this)
                        val qUser = QUser.user
                        val user = factory.from(qUser)
                            .fetchOne() // 단건 조회 or null or NonUniqueResultException

                        user shouldBe null
                    }
                }
            }

            "fetchFirst" {
                tx {
                    val factory = JPAQueryFactory(this)
                    val qUser = QUser.user
                    val user = factory.from(qUser)
                        .where(qUser.name.eq("NOT EXIST"))
                        .fetchFirst()   // 첫번째 데이터 반환 or null

                    user shouldBe null
                }
            }
        }

        "페이징 & 정렬" {
            factory {
                val qUser = QUser.user
                val results = from(qUser)
                    .orderBy(qUser.name.asc(), qUser.age.desc())
                    .offset(2)
                    .limit(3)
                    .fetchResults()

                results.total shouldBe entitiesSize
                results.limit shouldBe 3
                results.offset shouldBe 2
                results.results shouldHaveSize 3
            }
        }

        "그룹" {
            factory {
                val qUser = QUser.user
                val results = from(qUser)
                    .groupBy(qUser.name)
                    .having(qUser.age.gt(10))
                    .fetch()

                println(results)
            }
        }
    }

    private inline fun factory(block: JPAQueryFactory.() -> Unit) =
        tx {
            val queryFactory = JPAQueryFactory(this)
            queryFactory.block()
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

    private fun makeUsers(size: Int) = (1..size).map { makeUser(it) }.toList()

    private fun makeUser(idx: Int) =
        User(
            null, "name$idx", birthDay = LocalDate.of(2002, 6, idx % 29),
            Sex.values()[idx % 2], email = "example${idx}@example.com", address
        )

    private fun makeRestaurantsAndMenus(size: Int) = (1..size).map { makeRestaurantAndMenu(it) }.toList()

    private fun makeRestaurantAndMenu(idx: Int): Restaurant =
        with(Restaurant(null, "restaurant[$idx]", address)) {
            makeMenu("menu_${idx}_1", "description1")
            makeMenu("menu_${idx}_2", "description2")
            this
        }

    private fun makeOrder(): Order {
        var result: Order? = null

        tx {
            val user = createQuery("SELECT u FROM User u", User::class.java)
                .setMaxResults(1)
                .singleResult

            val menu = createQuery("SELECT m FROM Menu m", Menu::class.java)
                .setMaxResults(1)
                .singleResult

            val order = Order(user = user, status = OrderStatus.PREPARING)
            order.addMenu(menu)

            persist(order)

            result = order
        }

        return result!!
    }
}

data class UserDto2(
    val username: String,
    val age: Int
)
