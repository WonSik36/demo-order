package me.wonsik.order.demo.order.jpa.ch10

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.Menu
import me.wonsik.order.demo.order.domain.order.Order
import me.wonsik.order.demo.order.domain.order.OrderStatus
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.lang.IllegalArgumentException
import java.time.LocalDate
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class JpqlTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)

    private val entitiesSize = 20

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
        "기본" - {
            "TypedQuery" - {
                "resultList" {
                    tx {
                        val typedQuery: TypedQuery<User> = createQuery("SELECT u FROM User u", User::class.java)

                        val users = typedQuery.resultList   // 복수개

                        users shouldHaveSize entitiesSize
                    }
                }

                "singleResult" {
                    tx {
                        val typedQuery: TypedQuery<User> =
                            createQuery("SELECT u FROM User u WHERE u.name = :name", User::class.java)  // 이름 기준 파라미터 바인딩
                                .setParameter("name", "name1")

                        val user = typedQuery.singleResult  // 1개

                        user.name shouldBe "name1"
                    }
                }

                "singleResult - 결과가 없는 경우 에러 발생" {
                    tx {
                        val typedQuery: TypedQuery<User> =
                            createQuery("SELECT u FROM User u WHERE u.name = ?1", User::class.java) // 위치 기준 파라미터 바인딩
                                .setParameter(1, "unknown")

                        shouldThrow<NoResultException> { typedQuery.singleResult }
                    }
                }

                "singleResult - 복수개인 경우 에러 발생" {
                    tx {
                        val typedQuery: TypedQuery<User> = createQuery("SELECT u FROM User u", User::class.java)

                        shouldThrow<NonUniqueResultException> { typedQuery.singleResult }
                    }
                }

                "페이징" {
                    tx {
                        // new 명령어
                        // ORDER BY
                        val sql = "SELECT new me.wonsik.order.demo.order.jpa.ch10.UserDto(u.name, u.birthDay) FROM User u ORDER BY u.sequence DESC"
                        val query: TypedQuery<UserDto> = createQuery(sql, UserDto::class.java)
                            .setFirstResult(10) // start
                            .setMaxResults(5)   // limit

                        val resultList = query.resultList

                        resultList shouldHaveSize 5

                        resultList.map { result ->
                            println("userDto: $result")
                        }
                    }
                }
            }

            "Query" - {
                "Single" {
                    tx {
                        val query: Query = createQuery("SELECT u.address FROM User u")  // 임베디드 타입 프로젝션

                        val resultList = query.resultList

                        resultList shouldHaveSize entitiesSize

                        resultList.map { result ->
                            result as Address
                            println("address: $result")
                        }
                    }
                }

                "Multiple" - {
                    "스칼라 타입 프로젝션" {
                        tx {
                            val query: Query = createQuery("SELECT u.name, u.birthDay FROM User u")

                            val resultList = query.resultList

                            resultList shouldHaveSize entitiesSize

                            resultList.map { result ->
                                result as Array<*>
                                println("username: ${result[0]}, birthDay: ${result[1]}")
                            }
                        }
                    }

                    "엔티티 타입 프로젝션" {
                        tx {
                            val query: Query = createQuery("SELECT r, r.menus FROM Restaurant r")

                            val resultList = query.resultList
                            val result = resultList[0] as Array<*>

                            result[0].shouldBeInstanceOf<Restaurant>()
                            val restaurant = result[0] as Restaurant
                            restaurant.isLoaded() shouldBe true // 엔티티는 영속성 컨텍스트에 존재

                            result[1].shouldBeInstanceOf<Menu>()
                            val menu = result[1] as Menu
                            menu.isLoaded() shouldBe true   // 엔티티는 영속성 컨텍스트에 존재
                        }
                    }

                    "집합" {
                        tx {
                            val query: Query = createQuery("""
                                    SELECT
                                    COUNT(u),
                                    COUNT(DISTINCT u.sequence),
                                    SUM(u.sequence),
                                    AVG(u.sequence),
                                    MAX(u.sequence),
                                    MIN(u.sequence)
                                    FROM User u
                                """)

                            val results = query.singleResult as Array<*>
                            println("COUNT: ${results[0]}, SUM: ${results[1]}, AVG: ${results[2]}, MAX: ${results[3]}, MIN: ${results[4]}")
                        }
                    }

                    "GROUP BY, HAVING" {
                        tx {
                            val query: Query = createQuery("""
                                SELECT MAX(u.sequence), COUNT(u.sequence) as cnt 
                                FROM User u
                                GROUP BY u.name
                                ORDER BY cnt
                            """)

                            val results = query.resultList[0] as Array<*>
                            println("Max: ${results[0]}, Count: ${results[1]}")
                        }
                    }
                }
            }
        }

        "조인" - {
            "내부 조인" {
                tx {
                    val menus = createQuery("SELECT m, r FROM Menu m INNER JOIN m.restaurant r WHERE r.name LIKE 'restaurant%'")
                        .resultList

                    menus shouldHaveSize 40
                }
            }

            "외부 조인" {
                val name = "left join restaurant"

                tx {
                    val restaurant = Restaurant(null, name , address)

                    persist(restaurant)
                }

                tx {
                    val restaurant = createQuery("SELECT r, m FROM Restaurant r LEFT JOIN r.menus m WHERE r.name = :name")
                        .setParameter("name", name)
                        .resultList

                    restaurant.size shouldBe 1
                }
            }

            "세타 조인" {
                tx {
                    val resultList = createQuery("SELECT u.name, r.name, u.address FROM User u, Restaurant r WHERE u.address = r.address")
                        .resultList

                    resultList shouldHaveSize (entitiesSize * entitiesSize)

                    resultList.subList(0, 5)
                        .forEach { result ->
                            result as Array<*>
                            println("user name: ${result[0]}, restaurant name: ${result[1]}, address: ${result[2]}")
                        }
                }
            }

            "JOIN ON" {
                tx {
                    val maxRestaurantSequence = createQuery("SELECT Max(r.sequence) FROM Restaurant r")
                        .singleResult as Long
                    val middleSequence = maxRestaurantSequence - (entitiesSize * 3 / 2)

                    // 조인 대상 필터링
                    val menuNameAndRestaurantNames = createQuery("SELECT m.name, r.name FROM Menu m LEFT JOIN m.restaurant r ON r.sequence > :sequence")
                        .setParameter("sequence", middleSequence)
                        .resultList

                    menuNameAndRestaurantNames shouldHaveSize 40
                    // menu - null
                    // menu - restaurant
                }
            }

            "페치 조인" - {
                "엔티티 페치 조인" {
                    tx {
                        val maxMenuSeq = createQuery("SELECT Max(m.sequence) FROM Menu m")
                            .singleResult as Long

                        val menu = createQuery("SELECT m FROM Menu m INNER JOIN FETCH m.restaurant r WHERE m.sequence = :sequence", Menu::class.java)
                            .setParameter("sequence", maxMenuSeq)
                            .singleResult

                        menu.isLoaded() shouldBe true
                        menu.restaurant.isLoaded() shouldBe true
                    }
                }

                "컬렉션 페치 조인" {
                    tx {
                        val maxResSeq = createQuery("SELECT Max(r.sequence) FROM Restaurant r")
                            .singleResult as Long

                        val restaurants = createQuery("SELECT r FROM Restaurant r INNER JOIN FETCH r.menus m WHERE r.sequence = :sequence", Restaurant::class.java)
                            .setParameter("sequence", maxResSeq)
                            .resultList

                        restaurants shouldHaveSize 2

                        for (rest in restaurants) {
                            println("restaurant: $rest, menu: ${rest.menus}")
                            rest.isLoaded() shouldBe true
                            rest.menus.isLoaded() shouldBe true
                        }
                    }
                }

                "컬렉션 페치 조인 DISTINCT" {
                    tx {
                        val maxResSeq = createQuery("SELECT Max(r.sequence) FROM Restaurant r")
                            .singleResult as Long

                        val restaurants = createQuery("SELECT DISTINCT r FROM Restaurant r INNER JOIN FETCH r.menus m WHERE r.sequence = :sequence", Restaurant::class.java)
                            .setParameter("sequence", maxResSeq)
                            .resultList

                        restaurants shouldHaveSize 1

                        for (rest in restaurants) {
                            println("restaurant: $rest, menu: ${rest.menus}")
                            rest.isLoaded() shouldBe true
                            rest.menus.isLoaded() shouldBe true
                        }
                    }
                }
            }
        }

        "경로 탐색" - {
            "상태 필드 경로 탐색" {
                tx {
                    val jpql = "select u.name, u.birthDay from User u"
                    val results = createQuery(jpql).resultList

                    for (r in results) {
                        r as Array<*>
                        println("name: ${r[0]}, birthDay: ${r[1]}")
                    }
                }
            }

            "단일 값 연관 경로 탐색" {
                tx {
                    val jpql = "select m.restaurant from Menu m"
                    val results = createQuery(jpql).resultList

                    for (r in results) {
                        println("restaurant: $r")
                    }
                }

                tx {
                    // 경로 표현식에 의한 묵시적 조인 -> 내부 조인
                    val jpql = "select m.restaurant.address.city from Menu m"
                    val results = createQuery(jpql).resultList

                    for (r in results) {
                        println("city: $r")
                    }
                }
            }

            "컬렉션 값 연관 경로 탐색" {
                tx {
                    val jpql = "select r.menus from Restaurant r"
                    val results = createQuery(jpql).resultList

                    for (r in results) {
                        println("menus: $r")
                    }
                }

                shouldThrow<IllegalArgumentException> {
                    tx {
                        // 컬렉션에서 경로 탐색은 불가능
                        val jpql = "select r.menus.name from Restaurant r"
                        val results = createQuery(jpql).resultList

                        for (r in results) {
                            println("name: $r")
                        }
                    }
                }

                tx {
                    // 조인을 이용해 새로운 별칭 획득 후 탐색 가능
                    val jpql = "select m.name from Restaurant r join r.menus m"
                    val results = createQuery(jpql).resultList

                    for (r in results) {
                        println("name: $r")
                    }
                }
            }
        }

        "서브 쿼리" - {
            "기본" {
                val order = makeOrder()

                tx {
                    val query = "select u from User u where (select count(o) from Order o where u = o.user) > 0"
                    val members = createQuery(query).resultList

                    for (m in members) {
                        println(m)
                    }
                }

                tx {
                    createQuery("delete from OrderMenu om").executeUpdate()
                    createQuery("delete from Order o").executeUpdate()
                }
            }

            "EXISTS" {
                val order = makeOrder()

                tx {
                    val query = "select u from User u where exists (select o from Order o where u = o.user)"
                    val members = createQuery(query).resultList

                    for (m in members) {
                        println(m)
                    }
                }

                tx {
                    createQuery("delete from OrderMenu om").executeUpdate()
                    createQuery("delete from Order o").executeUpdate()
                }
            }

            "ALL|ANY|SOME" {
                tx {
                    val query = "select u from User u where u.birthDay >= all (select u2.birthDay from User u2)"
                    val members = createQuery(query).resultList

                    for (m in members) {
                        println(m)
                    }
                }
            }

            "IN" {
                tx {
                    val query = "select u from User u where u in (select u2 from User u2 where day(u2.birthDay) = 6)"
                    val members = createQuery(query).resultList

                    for (m in members) {
                        println(m)
                    }
                }
            }
        }

        "벌크 연산" - {
            "Update" {
                tx {
                    val count = createQuery("update User u set u.name = 'bulk'")
                        .executeUpdate()

                    count shouldBe entitiesSize
                }

                tx {
                    val users = createQuery("select u from User u", User::class.java)
                        .resultList

                    users.forEach {
                        it.name shouldBe  "bulk"
                    }
                }
            }

            "Delete" {
                tx {
                    val count = createQuery("delete from User u")
                        .executeUpdate()

                    count shouldBe entitiesSize
                }
            }

            "주의 사항" - {
                "MisMatch" {
                    tx {
                        val users = createQuery("select u from User u", User::class.java)
                            .resultList

                        users.forEach {
                            it.email = "hello@example.com"
                        }
                        // 쿼리 실행전 플러시

                        // 영속성 컨텍스트 무시, DB 에 바로 요청
                        createQuery("update User u set u.name = 'bulk'")
                            .executeUpdate()

                        users.forEach {
                            it.name shouldNotBe "bulk"
                        }

                        val user = find(User::class.java, users[0].sequence)
                        user.name shouldNotBe "bulk"
                    }

                    tx {
                        val users = createQuery("select u from User u", User::class.java)
                            .resultList

                        users.forEach {
                            it.name shouldBe "bulk"
                        }
                    }
                }

                "refresh" {
                    tx {
                        val users = createQuery("select u from User u", User::class.java)
                            .resultList

                        // 영속성 컨텍스트 무시, DB 에 바로 요청
                        createQuery("update User u set u.name = 'bulk'")
                            .executeUpdate()

                        users.forEach {
                            refresh(it) // DB 에서 다시 조회
                            it.name shouldBe "bulk"
                        }
                    }
                }

                "벌크 연산 먼저 실행" {
                    tx {
                        // 영속성 컨텍스트 무시, DB 에 바로 요청
                        createQuery("update User u set u.name = 'bulk'")
                            .executeUpdate()

                        val users = createQuery("select u from User u", User::class.java)
                            .resultList

                        users.forEach {
                            refresh(it) // DB 에서 다시 조회
                            it.name shouldBe "bulk"
                        }
                    }
                }

                "clear" {
                    tx {
                        val users = createQuery("select u from User u", User::class.java)
                            .resultList

                        // 영속성 컨텍스트 무시
                        createQuery("update User u set u.name = 'bulk'")
                            .executeUpdate()

                        val user1 = find(User::class.java, users[0].sequence)
                        user1.name shouldNotBe "bulk"

                        // 영속성 초기화
                        clear()

                        val user2 = find(User::class.java, users[0].sequence)
                        user2.name shouldBe "bulk"
                    }
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

data class UserDto(
    val username: String,
    val birthDay: LocalDate
)
