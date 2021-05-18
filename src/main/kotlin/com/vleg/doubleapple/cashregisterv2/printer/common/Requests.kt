package com.vleg.doubleapple.cashregisterv2.printer.common

data class OrderPrintRequestSplit(
    val inn: String,
    val address: String,
    val phone: String,
    val table: String,
    val open: String,
    val close: String,
    val summary: String,
    val discount: String,
    val forPayment: String,
    val employee: String,
    val sign: String,
    val preClose: String,
    val count: String,
    var payload: OrderPrintRequest
)