package com.example.nextup.data.local.entity

enum class WidgetStyle(
    val displayName: String,
    val description: String
) {
    GLASS_PILL(
        displayName = "Glass Pills",
        description = "Frosted time pills over your theme color or photo"
    ),
    MINIMAL_TYPE(
        displayName = "Big Editorial",
        description = "Oversized hero countdown typography"
    ),
    PROGRESS_BAR(
        displayName = "Progress Tracker",
        description = "Visual progress bar from start to target date"
    ),
    NEON_DARK(
        displayName = "AMOLED Neon",
        description = "Deep dark surface with glowing accent numbers"
    )
}
