package com.example.nextup.data.local.entity

enum class EventCategory(
    val displayName: String,
    val defaultActionUrl: String? = null,
    val defaultActionPackage: String? = null
) {
    EXAM(
        displayName = "Exam",
        defaultActionUrl = "https://upsc.gov.in"
    ),
    FINANCE(
        displayName = "Finance",
        defaultActionUrl = "https://www.incometax.gov.in"
    ),
    PERSONAL(
        displayName = "Personal"
    ),
    TATKAL(
        displayName = "Tatkal",
        defaultActionUrl = "https://www.irctc.co.in",
        defaultActionPackage = "cris.org.in.prs.ima"
    )
}
