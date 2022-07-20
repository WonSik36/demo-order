package me.wonsik.order.demo.order.jpa.ch10

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.NonUniqueResultException
import com.querydsl.core.types.Projections
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldMatchEach
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.wonsik.order.demo.order.adapter.presentation.dto.QUserDto
import me.wonsik.order.demo.order.adapter.presentation.dto.UserDto
import me.wonsik.order.demo.order.adapter.querydsl.QueryDslConfig
import me.wonsik.order.demo.order.coroutines.flow.log
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.menu.QMenu
import me.wonsik.order.demo.order.domain.order.Order
import me.wonsik.order.demo.order.domain.order.OrderStatus
import me.wonsik.order.demo.order.domain.order.QOrder
import me.wonsik.order.demo.order.domain.order.QOrderMenu
import me.wonsik.order.demo.order.domain.restaurant.QRestaurant
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import me.wonsik.order.demo.order.domain.user.QUser
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
@Import(QueryDslConfig::class)
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
                    .where(qUser.name.eq("name1").and(qUser.birthDay.eq(LocalDate.of(2022, 7, 12)).or(qUser.email.eq("example1@example.com"))))
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
                    .orderBy(qUser.name.asc(), qUser.birthDay.asc())
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
                    .having(qUser.birthDay.lt(LocalDate.now()))
                    .fetch()

                println(results)
                results shouldHaveSize entitiesSize
            }
        }

        "조인" - {
            "Join" {
                var order = makeOrder()

                factory {
                    // 1. select Order
                    // 2. select User
                    // 3. select OrderMenu, Menu, Restaurant
                    val selectedOrder = select(QOrder.order)
                        .from(QOrder.order)
                        .join(QOrder.order.user, QUser.user)
                        .leftJoin(QOrder.order.orderMenus, QOrderMenu.orderMenu)
                        .leftJoin(QOrderMenu.orderMenu.menu, QMenu.menu)
                        .fetchFirst()

                    selectedOrder.status shouldBe OrderStatus.PREPARING
                    selectedOrder.user.name shouldBe "name1"
                    selectedOrder.orderMenus shouldHaveSize 1
                    selectedOrder.orderMenus[0].count shouldBe 1
                }

                tx {
                    log("Before Merge")
                    // 1. select Order
                    // 2. select User
                    // 3. select OrderMenu, Menu, Restaurant
                    order = merge(order)
                    log("After Merge")
                    remove(order)
                }
            }

            "On" {
                var order = makeOrder()

                factory {
                    val selectedOrder = select(QOrder.order)
                        .from(QOrder.order)
                        .join(QOrder.order.user, QUser.user)
                        .on(QUser.user.birthDay.lt(LocalDate.of(1900, 1, 1)))
                        .fetchFirst()

                    selectedOrder shouldBe null
                }

                tx {
                    log("Before Merge")
                    // 1. select Order
                    // 2. select User
                    // 3. select OrderMenu, Menu, Restaurant
                    order = merge(order)
                    log("After Merge")
                    remove(order)
                }
            }

            "Fetch Join" {
                var order = makeOrder()

                factory {
                    // select Order, User, OrderMenu, Menu, Restaurant
                    val selectedOrder = select(QOrder.order)
                        .from(QOrder.order)
                        .join(QOrder.order.user, QUser.user).fetchJoin()
                        .join(QOrder.order.orderMenus, QOrderMenu.orderMenu).fetchJoin()
                        .join(QOrderMenu.orderMenu.menu, QMenu.menu).fetchJoin()
                        .join(QMenu.menu.restaurant, QRestaurant.restaurant).fetchJoin()
                        .fetchFirst()

                    selectedOrder.status shouldBe OrderStatus.PREPARING
                    selectedOrder.user.name shouldBe "name1"
                    selectedOrder.orderMenus shouldHaveSize 1
                    selectedOrder.orderMenus[0].count shouldBe 1
                }

                tx {
                    log("Before Merge")
                    // 1. select Order
                    // 2. select User
                    // 3. select OrderMenu, Menu, Restaurant
                    order = merge(order)
                    log("After Merge")
                    remove(order)
                }
            }

            "Theta Join" {
                factory {
                    val menus = select(QMenu.menu)
                        .from(QMenu.menu, QRestaurant.restaurant)
                        .where(QMenu.menu.restaurant.eq(QRestaurant.restaurant))
                        .fetch()

                    menus shouldHaveSize (2 * entitiesSize)
                }
            }
        }

        "서브 쿼리" {
            factory {
                val users = select(QUser.user)
                    .from(QUser.user)
                    .where(
                        QUser.user.sequence.`in`(
                            JPAExpressions.select(QUser.user.sequence).from(QUser.user).where(QUser.user.birthDay.lt(LocalDate.now()))
                        )
                    ).fetch()

                users shouldHaveSize entitiesSize
            }
        }

        "결과 반환" - {
            "Tuple" {
                factory {
                    val tuples = select(Projections.tuple(QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .from(QUser.user)
                        .fetch()

                    for (t in tuples) {
                        println("name: ${t[QUser.user.name]}, birthDay: ${t[QUser.user.birthDay]}")
                    }
                }
            }

            "Projection - Bean" {
                // 디폴트 생성자 존재
                // Setter 로 값 삽입
                factory {
                    val userDtos = select(Projections.bean(UserDto::class.java, QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .from(QUser.user)
                        .fetch()

                    for (dto in userDtos) {
                        println("name: ${dto.name}, birthDay: ${dto.birthDay}")
                    }
                }
            }

            "Projection - Field" {
                // 디폴트 생성자 존재
                // Field 에 값 삽입
                factory {
                    val userDtos = select(Projections.fields(UserDto::class.java, QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .from(QUser.user)
                        .fetch()

                    for (dto in userDtos) {
                        println("name: ${dto.name}, birthDay: ${dto.birthDay}")
                    }
                }
            }

            "Projection - 생성자" {
                // 생성자 존재
                // 순서가 맞아야함
                factory {
                    val userDtos = select(Projections.constructor(UserDto::class.java, QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .from(QUser.user)
                        .fetch()

                    for (dto in userDtos) {
                        println("name: ${dto.name}, birthDay: ${dto.birthDay}")
                    }
                }
            }

            "Projection - @QueryProjection" {
                // ConstructorExpression 사용
                // IDE 자동완성 기능 사용 가능
                factory {
                    val userDtos = select(QUserDto(QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .from(QUser.user)
                        .fetch()

                    for (dto in userDtos) {
                        println("name: ${dto.name}, birthDay: ${dto.birthDay}")
                    }
                }
            }

            "Distinct" {
                factory {
                    val userDtos = select(QUserDto(QUser.user.name, QUser.user.birthDay, QUser.user.sex))
                        .distinct()
                        .from(QUser.user)
                        .fetch()

                    for (dto in userDtos) {
                        println("name: ${dto.name}, birthDay: ${dto.birthDay}")
                    }
                }
            }
        }

        "수정, 삭제 배치 쿼리" - {
            "Update" {
                factory {
                    update(QUser.user)
                        .where(QUser.user.sequence.mod(2).eq(1))
                        .set(QUser.user.name, QUser.user.name.append("-odd-number"))
                        .execute()
                }

                factory {
                    val users = select(QUser.user)
                        .from(QUser.user)
                        .where(QUser.user.sequence.mod(2).eq(1))
                        .fetch()

                    users.forEach { u ->
                        u.name shouldContain "odd-number"
                    }
                }
            }

            "Delete" {
                factory {
                    delete(QUser.user)
                        .where(QUser.user.sequence.mod(2).eq(1))
                        .execute()
                }

                factory {
                    val users = select(QUser.user)
                        .from(QUser.user)
                        .where(QUser.user.sequence.mod(2).eq(1))
                        .fetch()

                    users shouldHaveSize 0
                }
            }
        }

        "동적 쿼리" {
            factory {
                val map = mutableMapOf<String, String>()
                map["name"] = "name"
                map["emailDomain"] = "example.com"

                val builder = with(BooleanBuilder()) {
                    if (map.containsKey("name")) {
                        this.and(QUser.user.name.eq(map["name"]))
                    }

                    if (map.containsKey("emailDomain")) {
                        this.and(QUser.user.name.contains(map["emailDomain"]))
                    }

                    this
                }

                val users = select(QUser.user)
                    .from(QUser.user)
                    .where(builder)
                    .fetch()

                users shouldHaveSize 0
            }
        }

        "메소드 위임" {
            factory {
                delete(QUser.user)
                    .execute()
            }

            tx {
                val userWithAge21_0 = makeUser(LocalDate.now().minusYears(21))
                val userWithAge20_9 = makeUser(LocalDate.now().minusYears(21).plusDays(1))
                val userWithAge20_0 = makeUser(LocalDate.now().minusYears(20))
                val userWithAge19_9 = makeUser(LocalDate.now().minusYears(20).plusDays(1))

                persist(userWithAge21_0)
                persist(userWithAge20_9)
                persist(userWithAge20_0)
                persist(userWithAge19_9)
            }

            factory {
                val count1 = select(QUser.user)
                    .from(QUser.user)
                    .where(QUser.user.hasAge(20))
                    .fetchCount()

                count1 shouldBe 2

                val count2 = select(QUser.user)
                    .from(QUser.user)
                    .where(QUser.user.isOlderThan(20))
                    .fetchCount()

                count2 shouldBe 1

                val count3 = select(QUser.user)
                    .from(QUser.user)
                    .where(QUser.user.isOlderThanOrEqualTo(20))
                    .fetchCount()

                count3 shouldBe 3
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

    private fun makeUser(birthDay: LocalDate) =
        User(
            null, "name", birthDay = birthDay,
            Sex.MALE, email = "example@example.com", address
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

    private fun log(message: String) {
        println("************************* $message *************************")
    }
}

data class UserDto2(
    val username: String,
    val age: Int
)
