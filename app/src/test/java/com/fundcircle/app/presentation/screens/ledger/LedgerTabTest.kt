package com.fundcircle.app.presentation.screens.ledger

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LedgerTabTest {

    /** LedgerScreen maps the segmented control's index straight to `LedgerTab.entries[index]`. */
    @Test
    fun tabOrderMatchesTheSegmentedControl() {
        assertThat(LedgerTab.entries).containsExactly(LedgerTab.DEPOSITS, LedgerTab.EXPENSES).inOrder()
    }
}
