package me.wonsik.order.demo.order.adapter.presentation.dto

import com.querydsl.core.annotations.QueryProjection
import me.wonsik.order.demo.order.domain.user.Sex
import java.time.LocalDate


/**
 * @author 정원식 (wonsik.cheung)
 */
data class UserDto @QueryProjection constructor(
    var name: String = "",
    var birthDay: LocalDate = LocalDate.now(),
    var sex: Sex = Sex.FEMALE
)