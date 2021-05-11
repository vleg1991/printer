package com.vleg.doubleapple.cashregisterv2.printer.common

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.ZonedDateTime

data class CategoryResponse(
    val id: Long,
    val name: String
)

data class DiscountResponse(
    val discountCode: String,
    val amount: BigDecimal
)

data class EmployeeResponse(
    val id: Long,
    val name: String,
    val password: String
)

data class OrderResponse(
    val id: Long,
    val workingDay: WorkingDayResponse,
    val positions: Set<PositionResponse>,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime?,
    val table: TableResponse?,
    val status: OrderStatus,
    val market: Market,
    val discount: DiscountResponse?,
    val amount: BigDecimal
)

enum class OrderStatus {
    OPENED,
    CLOSED,
    OPENED_REMOVED,
    CLOSED_REMOVED
}

enum class Market{
    DABL,
    PIRANIYA
}

data class PositionResponse(
    val id: Long,
    val orderId: Long,
    val workingDayId: Long,
    val product: ProductResponse,
    val count: Long,
    // Собирать сумму позиции из цен продуктов
    val amount: BigDecimal
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val categoryId: Long,
    val cipherCode: BigInteger
)

data class TableResponse(
    val id: Long,
    val market: Market
)

data class VerifyCodeResponse(
    val isCodeAcceptable: Boolean,
    val availableDiscountAmount: BigDecimal
)

data class WorkingDayResponse(
    val id: Long,
    val date: LocalDate,
    val employee: EmployeeResponse,
    val status: WorkingDayStatus
)

enum class WorkingDayStatus {
    OPENED,
    CLOSED
}
