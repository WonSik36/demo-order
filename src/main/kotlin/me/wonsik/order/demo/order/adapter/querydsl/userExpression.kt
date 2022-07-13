package me.wonsik.order.demo.order.adapter.querydsl

import com.querydsl.core.annotations.QueryDelegate
import com.querydsl.core.types.dsl.BooleanExpression
import me.wonsik.order.demo.order.domain.user.QUser
import me.wonsik.order.demo.order.domain.user.User
import java.time.LocalDate


/**
 * @author 정원식 (wonsik.cheung)
 */
@QueryDelegate(User::class)
fun isOlderThan(user: QUser, age: Long): BooleanExpression {
    val leastBirthDay = LocalDate.now().minusYears(age + 1)

    return user.birthDay.loe(leastBirthDay)
}

@QueryDelegate(User::class)
fun isOlderThanOrEqualTo(user: QUser, age: Long): BooleanExpression {
    val leastBirthDay = LocalDate.now().minusYears(age)

    return user.birthDay.loe(leastBirthDay)
}

@QueryDelegate(User::class)
fun hasAge(user: QUser, age: Long): BooleanExpression {
    val maxBirthDay = LocalDate.now().minusYears(age + 1)
    val leastBirthDay = LocalDate.now().minusYears(age)

    return user.birthDay.gt(maxBirthDay).and(user.birthDay.loe(leastBirthDay))
}