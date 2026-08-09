package com.ledgerleaf.domain.model

data class PaymentMethod(
    val id: String,
    val name: String,
    val isSystem: Boolean,
    val isActive: Boolean
)
