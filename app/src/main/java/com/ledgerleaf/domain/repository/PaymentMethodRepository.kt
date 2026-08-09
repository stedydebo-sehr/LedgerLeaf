package com.ledgerleaf.domain.repository

import com.ledgerleaf.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun observeActivePaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun ensureSystemDefaults()
    suspend fun addCustomPaymentMethod(name: String): String
    suspend fun setCustomPaymentMethodActive(id: String, active: Boolean)
}
