package me.wonsik.order.demo.order.domain.user

import org.springframework.data.jpa.repository.JpaRepository


/**
 * @author 정원식 (wonsik.cheung)
 */
interface UserRepository: JpaRepository<User, Long> {
}