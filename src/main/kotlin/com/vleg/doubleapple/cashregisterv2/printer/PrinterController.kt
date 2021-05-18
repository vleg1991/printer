package com.vleg.doubleapple.cashregisterv2.printer

import com.vleg.doubleapple.cashregisterv2.printer.common.OrderPrintRequestSplit
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.print.DocFlavor
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc


@RestController
class PrinterController {

    @PostMapping("/print")
    fun print(@RequestBody request: OrderPrintRequestSplit) {
        try {
            PrintServiceLookup.lookupDefaultPrintService()
                .createPrintJob()
                .print(
                    SimpleDoc(
                        createPrintableVersion(request).toByteArray(Charset.forName("Cp866")),
                        DocFlavor.BYTE_ARRAY.AUTOSENSE,
                        null
                    ),
                    null
                )
        } catch (e: Exception) {
            throw PrintException(UNABLE_TO_PRINT, e)
        }
    }


    private fun createPrintableVersion(req: OrderPrintRequestSplit): String {

        val order = req.payload
        val header = HEADER_TEMPLATE
            .replace("{INN}", req.inn)
            .replace("{ADDRESS}", req.address)
            .replace("{PHONE}", req.phone)
            .replace("{TABLE}", req.table)
            .replace("{TABLE_NUMBER}", "${order.table?.id ?: 0L}")
        val openedAt = OPENED_AT_TEMPLATE
            .replace("{OPEN}", req.open)
            .replace("{OPENED_DATE}", order.startDate.withZoneSameInstant(ZoneId.of("GMT+04:00")).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")))
        val closedAt = CLOSED_AT_TEMPLATE
            .replace("{CLOSE}", req.close)
            .replace("{CLOSED_DATE}",
                order.endDate?.withZoneSameInstant(ZoneId.of("GMT+04:00"))?.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")) ?: req.preClose
            )
        val positions = order.positions
            .foldIndexed("") { idx, acc, entry ->
                acc.plus(
                    POSITION_TEMPLATE.replace("{NUMBER}", "${idx+1}")
                        .replace("{TITLE}", entry.product.name)
                        .replace("{PRICE}", "${entry.product.price}")
                        .replace("{COUNT_VALUE}", "${entry.count}")
                        .replace("{COUNT}", req.count)
                        .replace("{SUM}", "${entry.amount}")
                        .plus("\n")
                )
            }
            .ifEmpty { "Позиций пока нет" }
        val summaryValue = order.positions.fold(BigDecimal.ZERO) { acc, pos -> acc.plus(pos.amount) }
        val summary = SUMMARY_TEMPLATE
            .replace("{SUMMARY}", req.summary)
            .replace("{SUMMARY_VALUE}", "$summaryValue")

        val discount = if (order.discount?.amount ?: 0L != 0L) {
            DISCOUNT_TEMPLATE
                .replace("{DISCOUNT}", req.discount)
                .replace("{DISCOUNT_VALUE}", "${order.discount?.amount ?: 0}")
        } else {
            ""
        }
        val footer = FOOTER_TEMPLATE
            .replace("{FOR_PAYMENT}", req.forPayment)
            .replace("{FINAL_SUM_VALUE}", "${order.amount}")
            .replace("{EMPLOYEE}", req.employee)
            .replace("{EMPLOYER_NAME}", order.workingDay.employee.name)
            .replace("{SIGN}", req.sign)
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
            |$GAP_TEMPLATE
            |
            |$summary
            |$discount
            |$footer
            |
            |
            |
            |
            |
            |
            |
            |
            |
            |
        """.trimMargin()
    }

    companion object {

        private val HEADER_TEMPLATE = """
        |Double Apple
        |{INN}: 638204704301
        |CAFE
        |{ADDRESS}
        |{PHONE}: +79379892877
        |{TABLE} {TABLE_NUMBER}
        """.trimMargin()

        private val OPENED_AT_TEMPLATE = """
        |{OPEN}
        |{OPENED_DATE}
        """.trimMargin()

        private val CLOSED_AT_TEMPLATE = """
        |{CLOSE}
        |{CLOSED_DATE}
        """.trimMargin()

        private val POSITION_TEMPLATE = """
        |{NUMBER} {TITLE}
        |{PRICE} x {COUNT_VALUE} {COUNT} = {SUM}
        """.trimMargin()

        private val SUMMARY_TEMPLATE = "{SUMMARY}: {SUMMARY_VALUE}"

        private val DISCOUNT_TEMPLATE = "{DISCOUNT}: {DISCOUNT_VALUE}"

        private val FOOTER_TEMPLATE = """
        |{FOR_PAYMENT}: {FINAL_SUM_VALUE}
        |
        |{EMPLOYEE} {EMPLOYER_NAME}
        |{SIGN} 
        """.trimMargin()

        private val GAP_TEMPLATE = """
        |=============================
        """.trimMargin()

        private val UNABLE_TO_PRINT = "Не получилось распечатать чек. Обратитесь к администратору."
    }
}
