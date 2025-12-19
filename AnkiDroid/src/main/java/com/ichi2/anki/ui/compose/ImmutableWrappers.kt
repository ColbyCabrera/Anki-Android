package com.ichi2.anki.ui.compose

import androidx.compose.runtime.Immutable

@Immutable
data class ImmutableList<T>(val items: List<T>) : List<T> by items

@Immutable
data class ImmutableMap<K, V>(val items: Map<K, V>) : Map<K, V> by items

fun <T> List<T>.toImmutableList(): ImmutableList<T> = ImmutableList(this)
fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> = ImmutableMap(this)
