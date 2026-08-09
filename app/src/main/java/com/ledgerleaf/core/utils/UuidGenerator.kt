package com.ledgerleaf.core.utils

import java.util.UUID

object UuidGenerator {
    fun newId(): String = UUID.randomUUID().toString()
}
