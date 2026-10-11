package com.wisp.finance.domain.model

data class RecurrenceRule(
    val interval: Int = 1,
    val unit: RecurrenceUnit,
) {

    init {
        require(interval > 0) {
            "Recurrence interval must be greater than zero."
        }
    }
}
