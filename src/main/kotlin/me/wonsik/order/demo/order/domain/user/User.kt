package me.wonsik.order.demo.order.domain.user

import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.common.BaseEntity
import java.time.LocalDate
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long?,
    @Column val name: String,
    @Column val birthDay: LocalDate?,
    @Enumerated(EnumType.STRING) val sex: Sex,
    @Column var email: String,
    @Embedded var address: Address
) : BaseEntity() {
    val age: Int = calcAge(LocalDate.now(), birthDay)

    fun isAdult(): Boolean = age >= ADULT_AGE


    companion object {
        const val ADULT_AGE: Int = 18

        private fun calcAge(now: LocalDate, birthDay: LocalDate?): Int {
            birthDay ?: return 0

            val period = birthDay.until(now)

            return period.years
        }
    }
}