package me.wonsik.order.demo.order.jpa.ch12

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.menu.MenuRepository
import me.wonsik.order.demo.order.domain.order.OrderRepository
import me.wonsik.order.demo.order.domain.restaurant.RestaurantRepository
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import me.wonsik.order.demo.order.domain.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class JpaRepositoryTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

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
    }

    private val address = Address("state", "city", "street", "subStreet", 12345)

    private fun makeUsers(size: Int) = (1..size).map { makeUser(it) }.toList()

    private fun makeUser(idx: Int) =
        User(
            null, "name$idx", birthDay = LocalDate.of(2002, 6, idx % 29),
            Sex.values()[idx % 2], email = "example${idx}@example.com", address
        )
}