package com.ledgerleaf.feature.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFiltersTest {
    @Test fun emptyFiltersCannotSearch() = assertFalse(SearchFilters().canSearch)
    @Test fun notesAloneCanSearch() = assertTrue(SearchFilters(notesQuery = "fuel").canSearch)
    @Test fun structuredFilterCanSearch() = assertTrue(SearchFilters(favoritesOnly = true).canSearch)
}
