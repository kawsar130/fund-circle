package com.fundcircle.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fundcircle.app.R
import com.fundcircle.app.domain.model.Currency
import com.fundcircle.app.domain.model.MemberRole
import com.fundcircle.app.domain.model.MemberStatus
import com.fundcircle.app.presentation.theme.BadgeTextStyle
import com.fundcircle.app.presentation.theme.DesignTokens
import com.fundcircle.app.presentation.theme.FundCircleTheme
import com.fundcircle.app.presentation.theme.TABULAR_DIGITS

/**
 * Standardized atomic badges for Fund Circle adhering to Material 3 tokens,
 * full-pill radius (9999.dp), and WCAG semantic accessibility.
 */

@Composable
fun RoleBadge(
    role: MemberRole,
    modifier: Modifier = Modifier
) {
    val isRoleAdmin = role == MemberRole.ADMIN
    val text = if (isRoleAdmin) {
        stringResource(R.string.badge_role_admin)
    } else {
        stringResource(R.string.badge_role_stakeholder)
    }
    val a11yDesc = if (isRoleAdmin) {
        stringResource(R.string.badge_role_admin_cd)
    } else {
        stringResource(R.string.badge_role_stakeholder_cd)
    }
    val containerColor = if (isRoleAdmin) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isRoleAdmin) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = text,
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LiveBadge(
    modifier: Modifier = Modifier
) {
    val a11yDesc = stringResource(R.string.badge_status_live_cd)
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = stringResource(R.string.badge_status_live),
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CircleStatusBadge(
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val text = if (isLocked) {
        stringResource(R.string.badge_status_locked)
    } else {
        stringResource(R.string.badge_status_active)
    }
    val a11yDesc = if (isLocked) {
        stringResource(R.string.badge_status_locked_cd)
    } else {
        stringResource(R.string.badge_status_active_cd)
    }
    val containerColor = if (isLocked) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isLocked) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = text,
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MemberStatusBadge(
    status: MemberStatus,
    modifier: Modifier = Modifier
) {
    val (textRes, a11yRes, containerColor, contentColor) = when (status) {
        MemberStatus.ACTIVE -> Quadruple(
            R.string.badge_status_active,
            R.string.badge_status_active_cd,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        MemberStatus.INACTIVE -> Quadruple(
            R.string.badge_status_inactive,
            R.string.badge_status_inactive_cd,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        MemberStatus.PENDING -> Quadruple(
            R.string.badge_status_pending,
            R.string.badge_status_pending_cd,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        MemberStatus.LEFT -> Quadruple(
            R.string.badge_status_left,
            R.string.badge_status_left_cd,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    val a11yDesc = stringResource(a11yRes)
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = stringResource(textRes),
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ReversedBadge(
    modifier: Modifier = Modifier
) {
    val a11yDesc = stringResource(R.string.badge_reversed_pill_cd)
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = stringResource(R.string.badge_reversed_pill),
            style = BadgeTextStyle.copy(fontFeatureSettings = TABULAR_DIGITS),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CurrencyLockBadge(
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val text = stringResource(R.string.badge_currency_locked, currency.name, currency.symbol)
    val a11yDesc = stringResource(R.string.badge_currency_locked_cd, currency.name)
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = DesignTokens.PillShape,
        modifier = modifier.semantics { contentDescription = a11yDesc }
    ) {
        Text(
            text = text,
            style = BadgeTextStyle,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(name = "Atomic Badges Light", showBackground = true)
@Composable
private fun AtomicBadgesLightPreview() {
    FundCircleTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleBadge(role = MemberRole.ADMIN)
                RoleBadge(role = MemberRole.MEMBER)
                LiveBadge()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleStatusBadge(isLocked = false)
                CircleStatusBadge(isLocked = true)
                ReversedBadge()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MemberStatusBadge(status = MemberStatus.ACTIVE)
                MemberStatusBadge(status = MemberStatus.INACTIVE)
                MemberStatusBadge(status = MemberStatus.LEFT)
            }
            CurrencyLockBadge(currency = Currency.BDT)
        }
    }
}

@Preview(name = "Atomic Badges Dark", showBackground = true)
@Composable
private fun AtomicBadgesDarkPreview() {
    FundCircleTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleBadge(role = MemberRole.ADMIN)
                RoleBadge(role = MemberRole.MEMBER)
                LiveBadge()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleStatusBadge(isLocked = false)
                CircleStatusBadge(isLocked = true)
                ReversedBadge()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MemberStatusBadge(status = MemberStatus.ACTIVE)
                MemberStatusBadge(status = MemberStatus.INACTIVE)
                MemberStatusBadge(status = MemberStatus.LEFT)
            }
            CurrencyLockBadge(currency = Currency.BDT)
        }
    }
}
