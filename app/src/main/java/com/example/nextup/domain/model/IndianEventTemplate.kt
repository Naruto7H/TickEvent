package com.example.nextup.domain.model

import com.example.nextup.data.local.entity.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId

data class IndianEventTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: EventCategory,
    val defaultThemeColor: Int,
    val targetEpochMillisProvider: () -> Long,
    val actionUrl: String? = category.defaultActionUrl,
    val actionPackage: String? = category.defaultActionPackage
)

object IndianTemplatesCatalog {

    val templates: List<IndianEventTemplate> = listOf(
        IndianEventTemplate(
            id = "irctc_tatkal_ac",
            title = "IRCTC AC Tatkal Booking (10:00 AM)",
            subtitle = "Next 10:00 AM window • Opens IRCTC Rail Connect",
            category = EventCategory.TATKAL,
            defaultThemeColor = 0xFF1565C0.toInt(),
            targetEpochMillisProvider = { nextTimeOccurrence(hour = 10, minute = 0) }
        ),
        IndianEventTemplate(
            id = "irctc_tatkal_non_ac",
            title = "IRCTC Non-AC Tatkal Booking (11:00 AM)",
            subtitle = "Next 11:00 AM window • Opens IRCTC Rail Connect",
            category = EventCategory.TATKAL,
            defaultThemeColor = 0xFF00838F.toInt(),
            targetEpochMillisProvider = { nextTimeOccurrence(hour = 11, minute = 0) }
        ),
        IndianEventTemplate(
            id = "upsc_cse_prelims",
            title = "UPSC CSE Prelims Exam",
            subtitle = "May 24 at 09:30 AM • Opens UPSC Portal",
            category = EventCategory.EXAM,
            defaultThemeColor = 0xFF6A1B9A.toInt(),
            targetEpochMillisProvider = {
                nextAnnualDateOccurrence(month = Month.MAY, dayOfMonth = 24, hour = 9, minute = 30)
            }
        ),
        IndianEventTemplate(
            id = "itr_filing_deadline",
            title = "ITR Filing Deadline (AY)",
            subtitle = "July 31 at 11:59 PM • Opens Income Tax e-Filing",
            category = EventCategory.FINANCE,
            defaultThemeColor = 0xFF2E7D32.toInt(),
            targetEpochMillisProvider = {
                nextAnnualDateOccurrence(month = Month.JULY, dayOfMonth = 31, hour = 23, minute = 59)
            }
        ),
        IndianEventTemplate(
            id = "diwali_festival",
            title = "Diwali Celebration",
            subtitle = "Festival of Lights • 06:00 PM Muhurat",
            category = EventCategory.PERSONAL,
            defaultThemeColor = 0xFFE65100.toInt(),
            targetEpochMillisProvider = {
                nextAnnualDateOccurrence(month = Month.NOVEMBER, dayOfMonth = 8, hour = 18, minute = 0)
            }
        ),
        IndianEventTemplate(
            id = "gate_exam",
            title = "GATE Examination",
            subtitle = "February 1 at 09:00 AM",
            category = EventCategory.EXAM,
            defaultThemeColor = 0xFF283593.toInt(),
            targetEpochMillisProvider = {
                nextAnnualDateOccurrence(month = Month.FEBRUARY, dayOfMonth = 1, hour = 9, minute = 0)
            },
            actionUrl = "https://gate2026.iitg.ac.in"
        )
    )

    private fun nextTimeOccurrence(hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var candidate = LocalDateTime.of(now.toLocalDate(), LocalTime.of(hour, minute))
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.atZone(zone).toInstant().toEpochMilli()
    }

    private fun nextAnnualDateOccurrence(
        month: Month,
        dayOfMonth: Int,
        hour: Int,
        minute: Int
    ): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        var candidate = LocalDateTime.of(
            LocalDate.of(now.year, month, dayOfMonth),
            LocalTime.of(hour, minute)
        )
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusYears(1)
        }
        return candidate.atZone(zone).toInstant().toEpochMilli()
    }
}
