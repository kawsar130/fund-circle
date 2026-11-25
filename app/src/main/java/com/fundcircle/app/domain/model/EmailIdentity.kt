package com.fundcircle.app.domain.model

import java.util.Locale

/**
 * Canonical form of an email address used as a circle identity.
 *
 * Circle membership (`memberEmails`), admin ownership (`adminEmail`) and member
 * self-service checks in `firestore.rules` all compare lower-cased emails, so every
 * email written to Firestore must go through this function first.
 */
fun String.normalizedEmail(): String = trim().lowercase(Locale.ROOT)
