package me.wonsik.order.demo.order.domain.user

import me.wonsik.order.demo.order.domain.common.BaseEntity
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@Entity
@Table(name = "user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val sequence: Long?,
    @Column val name: String,
    @Column val age: Int,
    @Enumerated(EnumType.STRING) val sex: Sex,
    @Column var email: String
) : BaseEntity() {

    fun isAdult(): Boolean = age >= ADULT_AGE


    companion object {
        const val ADULT_AGE: Int = 18
    }
}