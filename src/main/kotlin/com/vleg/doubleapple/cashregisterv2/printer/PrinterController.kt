package com.vleg.doubleapple.cashregisterv2.printer

import com.vleg.doubleapple.cashregisterv2.printer.common.OrderResponse
import com.vleg.doubleapple.cashregisterv2.printer.common.PrintRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import java.math.BigDecimal
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc

@RestController
class PrinterController {

    @PostMapping("/print")
    fun print(@RequestBody request: PrintRequest) {
        try {
            PrintServiceLookup.lookupDefaultPrintService()
                .createPrintJob()
                .print(
                    SimpleDoc(
                        createPrintableVersion(request.order).toByteArray(),
                        DocFlavor.BYTE_ARRAY.AUTOSENSE,
                        null
                    ),
                    null
                )
        } catch (e: Exception) {
            throw PrintException(UNABLE_TO_PRINT, e)
        }
    }


    private fun createPrintableVersion(order: OrderResponse): String {

        val header = HEADER_TEMPLATE.replace("{TABLE_NUMBER}", "${order.table?.id ?: 0L}")
        val openedAt = OPENED_AT_TEMPLATE.replace("{OPENED_DATE}", "${order.startDate}")
        val closedAt = CLOSED_AT_TEMPLATE.replace("{CLOSED_DATE}", "${order.endDate}")
        val positions = order.positions
            .foldIndexed("") { idx, acc, entry ->
                acc.plus(
                    POSITION_TEMPLATE.replace("{NUMBER}", "$idx")
                        .replace("{TITLE}", entry.product.name)
                        .replace("{PRICE}", "${entry.product.price}")
                        .replace("{COUNT}", "${entry.count}")
                        .replace("{SUM}", "${entry.amount}")
                        .plus("\n")
                        .plus(GAP_TEMPLATE)
                )
            }
        val summaryValue = order.positions.fold(BigDecimal.ZERO) { acc, pos -> acc.plus(pos.amount) }
        val summary = SUMMARY_TEMPLATE.replace("{SUMMARY_VALUE}", "$summaryValue")
            .replace("{DISCOUNT_VALUE}", "${order.discount}")
            .replace("{FINAL_SUM_VALUE}", "${order.amount}")
        return """
            |$header
            |
            |$GAP_TEMPLATE
            |
            |$openedAt
            |
            |$closedAt
            |
            |$positions
            |
            |$GAP_TEMPLATE
            |
            |$summary
        """.trimMargin()
    }

    companion object {

        private val HEADER_TEMPLATE = """
        |Double Apple
        |ИНН: 638204704301
        |CAFE
        |Московское шоссе, литЕ 110
        |Тел.: +79379892877
        |Столик № {TABLE_NUMBER}
        """.trimMargin()

        private val OPENED_AT_TEMPLATE = """
        |Открыт
        |{OPENED_DATE}
        """.trimMargin()

        private val CLOSED_AT_TEMPLATE = """
        |Закрыт
        |{CLOSED_DATE}
        """.trimMargin()

        private val POSITION_TEMPLATE = """
        |{NUMBER} {TITLE}
        |{PRICE} x {COUNT} шт. = {SUM}
        """.trimMargin()

        private val SUMMARY_TEMPLATE = """
        |Итого: {SUMMARY_VALUE}
        |Скидка: {DISCOUNT_VALUE}
        |К оплате: {FINAL_SUM_VALUE}
        |
        |Кассир {EMPLOYER_NAME}
        |Подпись 
        """.trimMargin()

        private val GAP_TEMPLATE = """
        |=============================
        """.trimMargin()

        private val UNABLE_TO_PRINT = "Не получилось распечатать чек. Обратитесь к администратору."
    }
}