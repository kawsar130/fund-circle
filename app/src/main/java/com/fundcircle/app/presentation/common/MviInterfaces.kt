package com.fundcircle.app.presentation.common

/**
 * Marker interface for MVI Screen UI State (Single Source of Truth).
 */
interface UiState

/**
 * Marker interface for User Actions / Intents.
 */
interface UiAction

/**
 * Marker interface for One-Shot Side Effects (Navigation, Snackbars, Dialogs).
 */
interface UiEffect
