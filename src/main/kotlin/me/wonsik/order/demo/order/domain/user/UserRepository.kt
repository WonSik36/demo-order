package me.wonsik.order.demo.order.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate


/**
 * @author 정원식 (wonsik.cheung)
 */
interface UserRepository: JpaRepository<User, Long> {

    // 메소드 이름으로 쿼리 생성
    fun findByNameContains(name: String): List<User>

    // @NamedQuery
    fun findDistinctNames(): List<String>

    // @Query
    @Query("select u from User u where u.birthDay < :birthDay")
    fun findUserByBirthDayBefore(@Param("birthDay") birthDay: LocalDate): List<User>



    @Modifying(clearAutomatically = true)
    @Query("update User u set u.name = :name where u.sequence in :sequences")
    fun changeUserNameWhereSequenceIn(@Param("sequences") sequences: List<Long>, @Param("name") name: String): Int

    fun findUserByEmail(email: String): User
}