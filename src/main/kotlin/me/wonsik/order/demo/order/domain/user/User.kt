package me.wonsik.order.demo.order.domain.user

import au.com.console.kassava.kotlinEquals
import au.com.console.kassava.kotlinHashCode
import au.com.console.kassava.kotlinToString
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.common.BaseEntity
import java.time.LocalDate
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "user")
class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long? = null,
    @Column val name: String,
    @Column val birthDay: LocalDate?,
    @Enumerated(EnumType.STRING) val sex: Sex,
    @Column var email: String,
    @Embedded var address: Address
) : BaseEntity() {
    val age: Int
        get() = calcAge(LocalDate.now(), birthDay)

    fun isAdult(): Boolean = age >= ADULT_AGE

    override fun equals(other: Any?) = kotlinEquals(other = other, properties = equalsAndHashCodeProperties)

    override fun hashCode() = kotlinHashCode(properties = equalsAndHashCodeProperties)

    override fun toString() = kotlinToString(properties = toStringProperties)


    companion object {
        const val ADULT_AGE: Int = 18

        private fun calcAge(now: LocalDate, birthDay: LocalDate?): Int {
            birthDay ?: return 0

            val period = birthDay.until(now)

            return period.years
        }

        private val equalsAndHashCodeProperties = arrayOf(User::sequence)
        private val toStringProperties = arrayOf(User::sequence, User::name)
    }
}