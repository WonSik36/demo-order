package me.wonsik.order.demo.order.jpa.ch12

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.wonsik.order.demo.order.adapter.querydsl.QueryDslConfig
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.MenuRepository
import me.wonsik.order.demo.order.domain.order.Order
import me.wonsik.order.demo.order.domain.order.OrderRepository
import me.wonsik.order.demo.order.domain.order.OrderStatus
import me.wonsik.order.demo.order.domain.order.QOrder
import me.wonsik.order.demo.order.domain.restaurant.Restaurant
import me.wonsik.order.demo.order.domain.restaurant.RestaurantRepository
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import me.wonsik.order.demo.order.domain.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDate
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceContext
import javax.persistence.PersistenceUnit


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
@Import(QueryDslConfig::class)
internal class JpaRepositoryTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @PersistenceUnit
    private lateinit var emf: EntityManagerFactory
    @PersistenceContext
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var restaurantRepository: RestaurantRepository
    @Autowired
    private lateinit var menuRepository: MenuRepository
    @Autowired
    private lateinit var orderRepository: OrderRepository


    init {
        "쿼리 메소드" - {
            "메소드 명으로 생성" {
                val users = makeUsers(5)
                userRepository.saveAll(users)

                val results = userRepository.findByNameContains("na")
                results shouldHaveSize 5
            }

            "@NamedQuery" {
                val users = makeUsers(5)
                userRepository.saveAll(users)

                val results = userRepository.findDistinctNames()
                results shouldHaveSize 5
            }

            "@Query" {
                val users = makeUsers(5)
                userRepository.saveAll(users)

                val results = userRepository.findUserByBirthDayBefore(LocalDate.of(2002, 6, 3))
                results shouldHaveSize 2
            }
        }



        "벌크성 수정 쿼리" {
            userRepository.deleteAllInBatch()

            val users = makeUsers(5)
            userRepository.saveAll(users)

            val sequences = users.map { it.sequence }.filterNotNull()

            val count = userRepository.changeUserNameWhereSequenceIn(sequences, "Hello")
            count shouldBe 5
            // Persistence Context Cleared

            users.forEach { it.name shouldNotBe "Hello"}

            users.mapNotNull { userRepository.findById(it.sequence!!).orElse(null) }
                .forEach { it.name shouldBe "Hello" }
        }

        "반환 타입" - {
            "값이 없는 경우 - EmptyResultDataAccessException" {
                shouldThrow<EmptyResultDataAccessException> {
                    userRepository.findUserByEmail("example@example.com")
                }
            }

            "단건" {
                val users = makeUsers(1)
                userRepository.saveAll(users)

                val user = userRepository.findUserByEmail("example@example.com")

                user shouldNotBe null
            }

            "다건인 경우 - IncorrectResultSizeDataAccessException" {
                val users = makeUsers(2)
                userRepository.saveAll(users)

                shouldThrow<IncorrectResultSizeDataAccessException> {
                    userRepository.findUserByEmail("example@example.com")
                }
            }
        }

        "페이징과 정렬" {
            val users = makeUsers(25)
            userRepository.saveAll(users)

            val pageable: Pageable = PageRequest.of(1, 10, Sort.by(Sort.Order.desc( "name"), Sort.Order.asc("birthDay")))

            val page = userRepository.findAll(pageable)

            page.number shouldBe 1                              // 현재 페이지
            page.size shouldBe 10                               // 페이지 크기
            page.totalPages shouldBe 3                          // 전체 페이지 수
            page.numberOfElements shouldBe 10                   // 현재 페이지에 나올 데이터 수
            page.totalElements shouldBe 25                      // 전체 데이터 수

            page.hasPrevious() shouldBe true                    // 이전 페이지 존재 여부
            page.isFirst shouldBe false                         // 첫번째 페이지 여부
            page.hasNext() shouldBe true                        // 다음 페이지 존재 여부
            page.isLast shouldBe false                          // 마지막 페이지 여부

            val next: Pageable = page.nextPageable()            // 다음 페이지 객체, 현재 마지막 페이지인 경우 => Pageable.unpaged()
            val prev: Pageable = page.previousPageable()        // 이전 페이지 객체, 현재 첫번째 페이지인 경우 => Pageable.unpaged()

            val retrievedUsers: List<User> = page.content       // 조회된 데이터
            page.hasContent() shouldBe true                     // 조회된 데이터 존재여부
            val sort: Sort = page.sort                          // 정렬 정보
            sort.getOrderFor("name") shouldNotBe null
        }



        "QueryDsl" - {
            "QuerydslPredicateExecutor" {
                val order = makeOrder()

                em.clear()

                val result = orderRepository.findOne(QOrder.order.status.eq(OrderStatus.PREPARING)).get()
                result.user.isLoaded() shouldBe true
                result.orderMenus.isLoaded() shouldBe false
            }

            "QuerydslRepositorySupport" {
                val order = makeOrder()

                em.clear()

                val result = orderRepository.findFetchedOrderById(order.sequence!!).get()
                result.user.isLoaded() shouldBe true
                result.orderMenus.isLoaded() shouldBe true
                result.orderMenus[0].menu.isLoaded() shouldBe true
                result.orderMenus[0].menu.restaurant.isLoaded() shouldBe true
            }

            "JPAQueryFactory" {
                val order = makeOrder()

                em.clear()

                val result = orderRepository.findFetchedOrderByIdContains(listOf(order.sequence!!))[0]
                result.user.isLoaded() shouldBe true
                result.orderMenus.isLoaded() shouldBe true
                result.orderMenus[0].menu.isLoaded() shouldBe true
                result.orderMenus[0].menu.restaurant.isLoaded() shouldBe true
            }
        }
    }

    private val address = Address("state", "city", "street", "subStreet", 12345)

    private fun makeUsers(size: Int) = (1..size).map { makeUser(it) }.toList()

    private fun makeUser(idx: Int) =
        User(
            null, "name$idx", birthDay = LocalDate.of(2002, 6, idx % 29),
            Sex.values()[idx % 2], email = "example@example.com", address
        )

    private fun makeRestaurantsAndMenus(size: Int) = (1..size).map { makeRestaurantAndMenu(it) }.toList()

    private fun makeRestaurantAndMenu(idx: Int): Restaurant =
        with(Restaurant(null, "restaurant[$idx]", address)) {
            makeMenu("menu_${idx}_1", "description1")
            makeMenu("menu_${idx}_2", "description2")
            this
        }

    private fun makeOrder(): Order {
        val user = makeUser(1)
        userRepository.saveAndFlush(user)

        val restaurant = makeRestaurantAndMenu(1)
        restaurantRepository.saveAndFlush(restaurant)

        val order = Order(user = user, status = OrderStatus.PREPARING)
        order.addMenu(restaurant.menus[0])
        orderRepository.saveAndFlush(order)

        return order
    }

    private fun Any.isLoaded() = emf.persistenceUnitUtil.isLoaded(this)
}