package ru.aslanisl.telegramtest

data class Chart (
    val title: String,
    val values: List<Long>,
    val type: String,
    val name: String?,
    val color: String?
)