package com.example.nextup.ui.addedit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.WidgetStyle
import com.example.nextup.domain.model.IndianEventTemplate
import com.example.nextup.ui.viewmodel.EventViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PresetWidgetColors = listOf(
    0xFF6750A4.toInt(),
    0xFF1565C0.toInt(),
    0xFF00838F.toInt(),
    0xFF2E7D32.toInt(),
    0xFFE65100.toInt(),
    0xFFC62828.toInt(),
    0xFF283593.toInt(),
    0xFF455A64.toInt()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEventScreen(
    viewModel: EventViewModel,
    initialTemplate: IndianEventTemplate? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf(initialTemplate?.title ?: "") }
    var selectedCategory by rememberSaveable {
        mutableStateOf(initialTemplate?.category ?: EventCategory.PERSONAL)
    }
    var targetTimestamp by rememberSaveable {
        mutableLongStateOf(
            initialTemplate?.targetEpochMillisProvider?.invoke()
                ?: (System.currentTimeMillis() + 86_400_000L)
        )
    }
    var themeColor by rememberSaveable {
        mutableIntStateOf(initialTemplate?.defaultThemeColor ?: PresetWidgetColors.first())
    }
    var backgroundImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedWidgetStyle by rememberSaveable { mutableStateOf(WidgetStyle.GLASS_PILL) }
    var actionUrl by rememberSaveable {
        mutableStateOf(initialTemplate?.actionUrl ?: selectedCategory.defaultActionUrl ?: "")
    }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val persistEventAndNavigateBack = {
        viewModel.saveEvent(
            title = title,
            targetDate = targetTimestamp,
            category = selectedCategory,
            themeColor = themeColor,
            backgroundImageUri = backgroundImageUri,
            actionUrl = actionUrl.ifBlank { null },
            actionPackage = selectedCategory.defaultActionPackage,
            widgetStyle = selectedWidgetStyle,
            onSaved = { onNavigateBack() }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        persistEventAndNavigateBack()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            backgroundImageUri = it.toString()
        }
    }

    val zoneId = ZoneId.systemDefault()
    val currentLocalDateTime = Instant.ofEpochMilli(targetTimestamp).atZone(zoneId).toLocalDateTime()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (initialTemplate != null) "Customize Template" else "New Countdown")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g., UPSC Prelims, IRCTC Tatkal 10 AM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            if (actionUrl.isBlank()) {
                                actionUrl = category.defaultActionUrl.orEmpty()
                            }
                        },
                        label = { Text(category.displayName) }
                    )
                }
            }

            Text(
                text = "Target Date & Time",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(currentLocalDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(currentLocalDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
                }
            }

            Text(
                text = "Home Screen Widget Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WidgetStyle.entries.forEach { style ->
                    FilterChip(
                        selected = selectedWidgetStyle == style,
                        onClick = { selectedWidgetStyle = style },
                        label = { Text(style.displayName) }
                    )
                }
            }

            Text(
                text = "Card & Widget Theme Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PresetWidgetColors.forEach { colorInt ->
                    val isSelected = themeColor == colorInt
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { themeColor = colorInt },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Text(
                text = "Custom Background Image (Optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (backgroundImageUri == null) "Pick Gallery Image" else "Change Image")
                }

                if (backgroundImageUri != null) {
                    TextButton(onClick = { backgroundImageUri = null }) {
                        Text("Remove")
                    }
                }
            }

            OutlinedTextField(
                value = actionUrl,
                onValueChange = { actionUrl = it },
                label = { Text("Zero-Hour Action Link / URL (Optional)") },
                placeholder = { Text("https://www.irctc.co.in") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            persistEventAndNavigateBack()
                        }
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Countdown & Schedule Alerts", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = targetTimestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedUtcMillis ->
                            val pickedDate = Instant.ofEpochMilli(selectedUtcMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val updatedDateTime = LocalDateTime.of(
                                pickedDate,
                                currentLocalDateTime.toLocalTime()
                            )
                            targetTimestamp = updatedDateTime.atZone(zoneId).toInstant().toEpochMilli()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentLocalDateTime.hour,
            initialMinute = currentLocalDateTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Target Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedDateTime = LocalDateTime.of(
                            currentLocalDateTime.toLocalDate(),
                            LocalTime.of(timePickerState.hour, timePickerState.minute, 0)
                        )
                        targetTimestamp = updatedDateTime.atZone(zoneId).toInstant().toEpochMilli()
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
