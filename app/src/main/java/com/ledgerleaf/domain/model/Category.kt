package com.ledgerleaf.domain.model

enum class CategorySelectionMode { NONE, SINGLE, MULTI }
data class Subcategory(val id: String, val categoryId: String, val name: String, val isSystem: Boolean)
data class Category(val id: String, val name: String, val isSystem: Boolean, val selectionMode: CategorySelectionMode)
