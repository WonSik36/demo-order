package me.wonsik.order.demo.order.jpa.ch2

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class EntityManagerTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)

    init {
        "예제 테스트" {
            val em = emf.createEntityManager()  // thread-unsafe
            val tx = em.transaction

            try {
                tx.begin()
                logic(em)
                tx.commit()
            } catch (e: Exception) {
                e.printStackTrace()
                tx.rollback()
            } finally {
                em.close()
            }
        }
    }

    private fun logic(em: EntityManager) {
        val user = User(null, "alice", birthDay = null, Sex.FEMALE, "", address)

        // 등록
        em.persist(user)

        // 수정
        val newMail = "newEmail@example.com"
        user.email = newMail

        // 한건 조회
        val findUser = em.find(User::class.java, user.sequence)
        findUser.email shouldBe newMail

        // 목록 조회
        val query = em.createQuery("select u from User u", User::class.java)
        val members = query.resultList
        members shouldHaveSize 1

        // 삭제
        em.remove(user)
        query.resultList shouldHaveSize 0
    }
}