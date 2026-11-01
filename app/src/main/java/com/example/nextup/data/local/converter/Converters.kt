package com.example.nextup.data.local.converter

import androidx.room.TypeConverter
import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.WidgetStyle

class Converters {

    @TypeConverter
    fun fromCategory(category: EventCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): EventCategory =
        runCatching { EventCategory.valueOf(value) }.getOrDefault(EventCategory.PERSONAL)

    @TypeConverter
    fun fromWidgetStyle(style: WidgetStyle): String = style.name

    @TypeConverter
    fun toWidgetStyle(value: String): WidgetStyle =
        runCatching { WidgetStyle.valueOf(value) }.getOrDefault(WidgetStyle.GLASS_PILL)
}
