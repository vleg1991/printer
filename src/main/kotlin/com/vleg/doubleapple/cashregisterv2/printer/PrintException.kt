package com.vleg.doubleapple.cashregisterv2.printer

import java.lang.RuntimeException

class PrintException : RuntimeException {

    val details: Map<String, Any>?

    constructor() {
        this.details = null
    }

    constructor(message: String, cause: Throwable, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace) {
        this.details = null
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.details = null
    }

    constructor(message: String) : super(message) {
        this.details = null
    }

    constructor(message: String, details: Map<String, Any>) : super(message) {
        this.details = details
    }

    constructor(
        message: String,
        details: Map<String, Any>,
        enableSuppression: Boolean = true,
        writableStackTrace: Boolean = true
    ) : super(message, null, enableSuppression, writableStackTrace) {
        this.details = details
    }

    constructor(cause: Throwable) : super("", cause) {
        this.details = null
    }

    constructor(cause: Throwable, details: Map<String, Any>) : super("", cause) {
        this.details = details
    }

    constructor(details: Map<String, Any>) : super("") {
        this.details = details
    }

    override fun toString() = super.toString() +
            if (details == null) "" else " $details"

    companion object {

        private val serialVersionUID = -8954648201687210995L
    }
}