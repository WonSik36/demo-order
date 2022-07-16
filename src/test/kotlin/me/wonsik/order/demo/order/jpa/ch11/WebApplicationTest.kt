package me.wonsik.order.demo.order.jpa.ch11

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * @author 정원식 (wonsik.cheung)
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebApplicationTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    init {
        "EntityManagerFactory 는 언제나 같은 빈을 반환한다." {
            val set = mutableSetOf<String>()
            for (it in 1..100) {
                set.add(getAutowiredEntityManagerFactory())
            }
            for (it in 1..100) {
                set.add(getPersistenceUnitEntityManagerFactory())
            }

            set shouldHaveSize 1
        }

        "EntityManager 는 매 요청마다 새로 생성된다." {
            val set = mutableSetOf<String>()
            for (it in 1..100) {
                set.add(getAutowiredEntityManager())
            }
            for (it in 1..100) {
                set.add(getPersistenceContextEntityManager())
            }

            set shouldHaveSize 200
        }
    }

    private fun getAutowiredEntityManagerFactory() = requestAndGetBody("autowiredEntityManagerFactory")

    private fun getPersistenceUnitEntityManagerFactory() = requestAndGetBody("persistenceUnitEntityManagerFactory")

    private fun getAutowiredEntityManager() = requestAndGetBody("autowiredEntityManager")

    private fun getPersistenceContextEntityManager() = requestAndGetBody("persistenceContextEntityManager")

    private fun requestAndGetBody(url: String) =
        mockMvc.perform(get("/test/$url"))
            .andDo(print())
            .andExpect(status().isOk)
            .andReturn()
            .response.contentAsString
}