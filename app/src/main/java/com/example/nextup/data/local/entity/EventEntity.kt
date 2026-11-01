package com.example.nextup.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "target_date")
    val targetDate: Long,

    @ColumnInfo(name = "category")
    val category: EventCategory,

    @ColumnInfo(name = "theme_color")
    val themeColor: Int,

    @ColumnInfo(name = "background_image_uri")
    val backgroundImageUri: String? = null,

    @ColumnInfo(name = "action_url")
    val actionUrl: String? = category.defaultActionUrl,

    @ColumnInfo(name = "action_package")
    val actionPackage: String? = category.defaultActionPackage,

    @ColumnInfo(name = "widget_style")
    val widgetStyle: WidgetStyle = WidgetStyle.GLASS_PILL,

    @ColumnInfo(name = "widget_opacity")
    val widgetOpacity: Float = 0.92f,

    @ColumnInfo(name = "widget_show_progress")
    val widgetShowProgress: Boolean = true,

    @ColumnInfo(name = "widget_show_date")
    val widgetShowDate: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
