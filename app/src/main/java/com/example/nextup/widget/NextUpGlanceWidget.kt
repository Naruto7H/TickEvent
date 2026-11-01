package com.example.nextup.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.nextup.MainActivity
import com.example.nextup.data.local.entity.EventEntity
import com.example.nextup.data.local.entity.WidgetStyle
import com.example.nextup.di.WidgetEntryPoint
import com.example.nextup.domain.model.CountdownTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NextUpGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = WidgetEntryPoint.getEventDao(context)
        val allEvents = withContext(Dispatchers.IO) {
            dao.getAllEventsOneShot()
        }

        val bitmapsByEventId: Map<Long, Bitmap> = withContext(Dispatchers.IO) {
            allEvents.mapNotNull { event ->
                val uriStr = event.backgroundImageUri
                if (!uriStr.isNullOrBlank()) {
                    decodeSampledBitmapFromUri(context, Uri.parse(uriStr), 600, 400)?.let { bmp ->
                        event.id to bmp
                    }
                } else null
            }.toMap()
        }

        provideContent {
            val prefs = currentState<Preferences>()
            val pinnedEventId = prefs[KEY_PINNED_EVENT_ID] ?: -1L
            val styleOverrideName = prefs[KEY_WIDGET_STYLE_OVERRIDE]
            val now = System.currentTimeMillis()

            val targetEvent: EventEntity? = allEvents.firstOrNull { it.id == pinnedEventId }
                ?: allEvents.firstOrNull { it.targetDate > now }
                ?: allEvents.firstOrNull()

            val effectiveStyle = styleOverrideName?.let {
                runCatching { WidgetStyle.valueOf(it) }.getOrNull()
            } ?: targetEvent?.widgetStyle ?: WidgetStyle.GLASS_PILL

            val bgBitmap = targetEvent?.let { bitmapsByEventId[it.id] }

            GlanceTheme {
                WidgetContent(
                    context = context,
                    event = targetEvent,
                    style = effectiveStyle,
                    backgroundBitmap = bgBitmap
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        event: EventEntity?,
        style: WidgetStyle,
        backgroundBitmap: Bitmap?
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (event != null) {
                putExtra(MainActivity.EXTRA_EVENT_ID, event.id)
            }
        }

        if (event == null) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .clickable(actionStartActivity(launchIntent))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TickEvent",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Tap to create your first countdown",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            return
        }

        val countdown = CountdownTime.fromTargetTimestamp(event.targetDate)
        val opacity = event.widgetOpacity.coerceIn(0.35f, 1.0f)
        val themeColor = Color(event.themeColor)
        val surfaceBgColor = when (style) {
            WidgetStyle.NEON_DARK -> Color(0xFF0E1117).copy(alpha = opacity)
            else -> themeColor.copy(alpha = opacity)
        }

        val totalDuration = (event.targetDate - event.createdAt).coerceAtLeast(1L)
        val elapsed = (System.currentTimeMillis() - event.createdAt).coerceIn(0L, totalDuration)
        val progressFraction = (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0.02f, 1.0f)
        val dateText = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
            .format(Date(event.targetDate))

        val widgetWidth = LocalSize.current.width
        val isCompact = widgetWidth < 210.dp

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(24.dp)
                .background(ColorProvider(surfaceBgColor))
                .clickable(actionStartActivity(launchIntent))
        ) {
            if (backgroundBitmap != null && style != WidgetStyle.NEON_DARK) {
                Image(
                    provider = ImageProvider(backgroundBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize()
                )
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(Color.Black.copy(alpha = 0.52f * opacity)))
                ) {}
            }

            when (style) {
                WidgetStyle.GLASS_PILL -> GlassPillLayout(
                    event = event,
                    countdown = countdown,
                    dateText = dateText,
                    progressFraction = progressFraction,
                    isCompact = isCompact
                )
                WidgetStyle.MINIMAL_TYPE -> MinimalTypeLayout(
                    event = event,
                    countdown = countdown,
                    dateText = dateText
                )
                WidgetStyle.PROGRESS_BAR -> ProgressTrackerLayout(
                    event = event,
                    countdown = countdown,
                    dateText = dateText,
                    progressFraction = progressFraction
                )
                WidgetStyle.NEON_DARK -> NeonDarkLayout(
                    event = event,
                    countdown = countdown,
                    accentColor = themeColor,
                    dateText = dateText,
                    isCompact = isCompact
                )
            }
        }
    }

    @Composable
    private fun GlassPillLayout(
        event: EventEntity,
        countdown: CountdownTime,
        dateText: String,
        progressFraction: Float,
        isCompact: Boolean
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(50.dp)
                        .background(ColorProvider(Color.White.copy(alpha = 0.22f)))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.category.displayName.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (event.widgetShowDate && !isCompact) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = dateText,
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            if (countdown.isCompleted) {
                CompletedBadge(accentColor = Color.White)
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlanceTimePill(
                        value = countdown.days,
                        unit = "DAYS",
                        bgColor = Color.Black.copy(alpha = 0.30f),
                        textColor = Color.White
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    GlanceTimePill(
                        value = countdown.hours,
                        unit = "HRS",
                        bgColor = Color.Black.copy(alpha = 0.30f),
                        textColor = Color.White
                    )
                    if (!isCompact || countdown.days == 0L) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        GlanceTimePill(
                            value = countdown.minutes,
                            unit = "MINS",
                            bgColor = Color.Black.copy(alpha = 0.30f),
                            textColor = Color.White
                        )
                    }
                }
            }

            if (event.widgetShowProgress && !countdown.isCompleted) {
                Spacer(modifier = GlanceModifier.height(10.dp))
                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .cornerRadius(4.dp),
                    color = ColorProvider(Color.White),
                    backgroundColor = ColorProvider(Color.White.copy(alpha = 0.25f))
                )
            }
        }
    }

    @Composable
    private fun MinimalTypeLayout(
        event: EventEntity,
        countdown: CountdownTime,
        dateText: String
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.title.uppercase(),
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            if (countdown.isCompleted) {
                CompletedBadge(accentColor = Color.White)
            } else {
                val heroNumber = if (countdown.days > 0) "${countdown.days}" else "${countdown.hours}"
                val heroUnit = if (countdown.days > 0) {
                    "DAYS • ${countdown.hours}h ${countdown.minutes}m LEFT"
                } else {
                    "HOURS • ${countdown.minutes}m LEFT"
                }
                Text(
                    text = heroNumber,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = heroUnit,
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (event.widgetShowDate) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.72f)),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    private fun ProgressTrackerLayout(
        event: EventEntity,
        countdown: CountdownTime,
        dateText: String,
        progressFraction: Float
    ) {
        val percent = (progressFraction * 100).toInt().coerceIn(1, 100)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
            if (event.widgetShowDate) {
                Text(
                    text = dateText,
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = GlanceModifier.height(10.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (countdown.isCompleted) "Completed" else "${countdown.days}d ${countdown.hours}h ${countdown.minutes}m remaining",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "$percent%",
                    style = TextStyle(
                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            LinearProgressIndicator(
                progress = if (countdown.isCompleted) 1f else progressFraction,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .cornerRadius(6.dp),
                color = ColorProvider(Color.White),
                backgroundColor = ColorProvider(Color.Black.copy(alpha = 0.30f))
            )
        }
    }

    @Composable
    private fun NeonDarkLayout(
        event: EventEntity,
        countdown: CountdownTime,
        accentColor: Color,
        dateText: String,
        isCompact: Boolean
    ) {
        val brightAccent = Color(
            red = (accentColor.red + 0.35f).coerceAtMost(1f),
            green = (accentColor.green + 0.35f).coerceAtMost(1f),
            blue = (accentColor.blue + 0.35f).coerceAtMost(1f),
            alpha = 1f
        )

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(50.dp)
                        .background(ColorProvider(brightAccent.copy(alpha = 0.18f)))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.category.displayName.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(brightAccent),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (event.widgetShowDate && !isCompact) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = dateText,
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.65f)),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            if (countdown.isCompleted) {
                CompletedBadge(accentColor = brightAccent)
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlanceTimePill(
                        value = countdown.days,
                        unit = "DAYS",
                        bgColor = brightAccent.copy(alpha = 0.16f),
                        textColor = brightAccent
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    GlanceTimePill(
                        value = countdown.hours,
                        unit = "HRS",
                        bgColor = brightAccent.copy(alpha = 0.16f),
                        textColor = brightAccent
                    )
                    if (!isCompact || countdown.days == 0L) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        GlanceTimePill(
                            value = countdown.minutes,
                            unit = "MINS",
                            bgColor = brightAccent.copy(alpha = 0.16f),
                            textColor = brightAccent
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CompletedBadge(accentColor: Color) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(12.dp)
                .background(ColorProvider(accentColor.copy(alpha = 0.22f)))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "TIME'S UP • TAP TO LAUNCH →",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    @Composable
    private fun GlanceTimePill(
        value: Long,
        unit: String,
        bgColor: Color,
        textColor: Color
    ) {
        Column(
            modifier = GlanceModifier
                .cornerRadius(14.dp)
                .background(ColorProvider(bgColor))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%02d".format(value),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = unit,
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.80f)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    private fun decodeSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }.getOrNull()
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    companion object {
        val KEY_PINNED_EVENT_ID = longPreferencesKey("pinned_event_id")
        val KEY_WIDGET_STYLE_OVERRIDE = stringPreferencesKey("widget_style_override")
    }
}
