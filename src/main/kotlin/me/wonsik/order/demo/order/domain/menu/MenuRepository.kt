package me.wonsik.order.demo.order.domain.menu

import org.springframework.data.jpa.repository.JpaRepository


/**
 * @author 정원식 (wonsik.cheung)
 */
interface MenuRepository: JpaRepository<Menu, Long> {
}