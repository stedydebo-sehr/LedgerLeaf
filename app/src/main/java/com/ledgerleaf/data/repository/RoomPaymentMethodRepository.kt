package com.ledgerleaf.data.repository

import com.ledgerleaf.core.utils.UuidGenerator
import com.ledgerleaf.data.local.dao.PaymentMethodDao
import com.ledgerleaf.data.local.entity.PaymentMethodEntity
import com.ledgerleaf.domain.model.PaymentMethod
import com.ledgerleaf.domain.repository.PaymentMethodRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomPaymentMethodRepository @Inject constructor(
    private val dao: PaymentMethodDao
) : PaymentMethodRepository {

    override fun observeActivePaymentMethods(): Flow<List<PaymentMethod>> =
        dao.observeActive().map { items -> items.map { it.toDomain() } }

    override suspend fun ensureSystemDefaults() {
        if (dao.countAll() != 0) return
        val now = System.currentTimeMillis()
        dao.insertAll(
            listOf(
                PaymentMethodEntity("system-payment-cash", "Cash", true, true, 0, now),
                PaymentMethodEntity("system-payment-upi", "UPI", true, true, 10, now),
                PaymentMethodEntity("system-payment-debit-card", "Debit Card", true, true, 20, now),
                PaymentMethodEntity("system-payment-credit-card", "Credit Card", true, true, 30, now),
                PaymentMethodEntity("system-payment-bank-transfer", "Bank Transfer", true, true, 40, now),
                PaymentMethodEntity("system-payment-other", "Other", true, true, 50, now)
            )
        )
    }

    override suspend fun addCustomPaymentMethod(name: String): String {
        val cleaned = name.trim()
        require(cleaned.isNotBlank()) { "Payment method name is required." }
        val id = UuidGenerator.newId()
        dao.insert(PaymentMethodEntity(id, cleaned, false, true, 1000, System.currentTimeMillis()))
        return id
    }

    override suspend fun setCustomPaymentMethodActive(id: String, active: Boolean) {
        dao.setCustomActive(id, active)
    }

    private fun PaymentMethodEntity.toDomain() =
        PaymentMethod(id = id, name = name, isSystem = isSystem, isActive = isActive)
}
