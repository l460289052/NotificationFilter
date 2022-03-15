package com.example.notificationfilter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

val BaseTime: LocalTime = LocalTime.of(0, 0)
fun LocalDate.toLocalDateTime(): LocalDateTime = LocalDateTime.of(this, BaseTime)

fun joinToRegex(vararg args: String): String = args.joinToString("\n")
fun joinToSearch(vararg args: String): String = args.joinToString("\n").lowercase()