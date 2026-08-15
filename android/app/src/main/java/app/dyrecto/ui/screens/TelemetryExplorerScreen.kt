package app.dyrecto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dyrecto.debug.DeveloperState
import app.dyrecto.debug.TelemetryEvent
import app.dyrecto.debug.TelemetryEventStore
import app.dyrecto.domain.CameraConnectionState
import app.dyrecto.ui.util.formatTime

@Composable
fun TelemetryExplorerScreen(state: CameraConnectionState) {
    val devMode by DeveloperState.developerMode.collectAsState()

    if (!devMode) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Enable Developer Mode to use the Telemetry Explorer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Properties") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Events") },
            )
        }
        when (selectedTab) {
            0 -> PropertiesTab(state)
            1 -> EventsTab()
        }
    }
}

// ─────────────────────────────── Tab A: Properties ────────────────────────────

private enum class PropSort { BY_CODE, BY_LAST_UPDATED }
private enum class PropFilter { ALL, CHANGED, UNKNOWN }

@Composable
private fun PropertiesTab(state: CameraConnectionState) {
    val clipboard = LocalClipboardManager.current
    val propLastChanged by TelemetryEventStore.propLastChanged.collectAsState()
    val changedDuringSession by TelemetryEventStore.changedDuringSession.collectAsState()

    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(PropSort.BY_CODE) }
    var filter by remember { mutableStateOf(PropFilter.ALL) }

    val props = state.telemetry?.props?.values?.toList() ?: emptyList()
    val totalCount = props.size
    val decodedCount = props.count { it.decoded != null }

    val maxLastChanged by remember(propLastChanged) {
        derivedStateOf { propLastChanged.values.maxOrNull() }
    }

    val displayProps by remember(props, query, sort, filter, propLastChanged, changedDuringSession) {
        derivedStateOf {
            props
                .filter { prop ->
                    val matchesQuery = query.isBlank() ||
                        prop.label.contains(query, ignoreCase = true) ||
                        "0x%04X".format(prop.code).contains(query, ignoreCase = true)
                    val matchesFilter = when (filter) {
                        PropFilter.ALL -> true
                        PropFilter.CHANGED -> prop.code in changedDuringSession
                        PropFilter.UNKNOWN -> prop.decoded == null
                    }
                    matchesQuery && matchesFilter
                }
                .sortedWith(when (sort) {
                    PropSort.BY_CODE -> compareBy { it.code }
                    PropSort.BY_LAST_UPDATED -> compareByDescending { propLastChanged[it.code] ?: 0L }
                })
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Stats row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            StatChip("Total", totalCount.toString())
            StatChip("Decoded", decodedCount.toString())
            StatChip("Unknown", (totalCount - decodedCount).toString())
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by code or label") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )

        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = sort == PropSort.BY_CODE,
                    onClick = { sort = PropSort.BY_CODE },
                    label = { Text("By Code") },
                )
            }
            item {
                FilterChip(
                    selected = sort == PropSort.BY_LAST_UPDATED,
                    onClick = { sort = PropSort.BY_LAST_UPDATED },
                    label = { Text("By Last Updated") },
                )
            }
            item { Spacer(Modifier.padding(start = 8.dp)) }
            item {
                FilterChip(
                    selected = filter == PropFilter.ALL,
                    onClick = { filter = PropFilter.ALL },
                    label = { Text("All") },
                )
            }
            item {
                FilterChip(
                    selected = filter == PropFilter.CHANGED,
                    onClick = { filter = PropFilter.CHANGED },
                    label = { Text("Changed") },
                )
            }
            item {
                FilterChip(
                    selected = filter == PropFilter.UNKNOWN,
                    onClick = { filter = PropFilter.UNKNOWN },
                    label = { Text("Unknown") },
                )
            }
        }

        if (displayProps.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (state.telemetry == null) "No telemetry received yet."
                    else "No properties match the current filter.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                items(displayProps, key = { it.code }) { prop ->
                    val lastChanged = propLastChanged[prop.code]
                    val isRecent = maxLastChanged != null && lastChanged == maxLastChanged
                    PropRow(
                        code = prop.code,
                        label = prop.label,
                        decoded = prop.decoded,
                        rawValue = prop.rawValue,
                        dataType = prop.dataType,
                        lastChangedTs = lastChanged,
                        highlight = isRecent,
                        onCopyCode = {
                            clipboard.setText(AnnotatedString("0x%04X".format(prop.code)))
                        },
                        onCopyRaw = {
                            clipboard.setText(AnnotatedString(prop.rawValue ?: ""))
                        },
                        onCopyDecoded = {
                            clipboard.setText(AnnotatedString(prop.decoded ?: prop.rawValue ?: ""))
                        },
                        onCopyAll = {
                            clipboard.setText(AnnotatedString(
                                "Code=0x%04X Label=${prop.label} Raw=${prop.rawValue} " +
                                "Decoded=${prop.decoded} DataType=0x%04X".format(prop.code, prop.dataType)
                            ))
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PropRow(
    code: Int,
    label: String,
    decoded: String?,
    rawValue: String?,
    dataType: Int,
    lastChangedTs: Long?,
    highlight: Boolean,
    onCopyCode: () -> Unit,
    onCopyRaw: () -> Unit,
    onCopyDecoded: () -> Unit,
    onCopyAll: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val bg = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
             else MaterialTheme.colorScheme.surface

    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(bg, MaterialTheme.shapes.small)
            .combinedClickable(onClick = {}, onLongClick = { menuExpanded = true })
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "0x%04X".format(code),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    Text(
                        decoded ?: rawValue ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (decoded == null)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                    if (decoded != null && rawValue != null) {
                        Text(
                            "raw: $rawValue",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "type=0x%04X".format(dataType),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                )
                if (lastChangedTs != null) {
                    Text(
                        formatTime(lastChangedTs),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    )
                }
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("Copy Code") }, onClick = { onCopyCode(); menuExpanded = false })
            DropdownMenuItem(text = { Text("Copy Raw") }, onClick = { onCopyRaw(); menuExpanded = false })
            DropdownMenuItem(text = { Text("Copy Decoded") }, onClick = { onCopyDecoded(); menuExpanded = false })
            DropdownMenuItem(text = { Text("Copy All") }, onClick = { onCopyAll(); menuExpanded = false })
        }
    }
}

// ─────────────────────────────── Tab B: Events ────────────────────────────────

private enum class EventFilter { ALL, UNKNOWN }

@Composable
private fun EventsTab() {
    val clipboard = LocalClipboardManager.current
    val events by TelemetryEventStore.events.collectAsState()
    var filter by remember { mutableStateOf(EventFilter.ALL) }

    val displayEvents by remember(events, filter) {
        derivedStateOf {
            when (filter) {
                EventFilter.ALL -> events
                EventFilter.UNKNOWN -> events.filter { it.decodedValue == null }
            }
        }
    }

    // Group by batchId, newest batch first (higher batchId = newer).
    val batches by remember(displayEvents) {
        derivedStateOf {
            displayEvents
                .groupBy { it.batchId }
                .entries
                .sortedByDescending { it.key }
        }
    }

    Column(Modifier.fillMaxSize()) {
        LazyRow(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = filter == EventFilter.ALL,
                    onClick = { filter = EventFilter.ALL },
                    label = { Text("All (${events.size})") },
                )
            }
            item {
                FilterChip(
                    selected = filter == EventFilter.UNKNOWN,
                    onClick = { filter = EventFilter.UNKNOWN },
                    label = { Text("Unknown Only") },
                )
            }
        }

        if (displayEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (events.isEmpty()) "No telemetry events recorded yet."
                    else "No unknown-decoded events.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                batches.forEach { (batchId, batchEvents) ->
                    item(key = "batch_$batchId") {
                        BatchHeader(
                            batchId = batchId,
                            timestamp = batchEvents.first().timestamp,
                            count = batchEvents.size,
                        )
                    }
                    items(batchEvents, key = { "e_${it.batchId}_${it.propertyCode}" }) { event ->
                        EventRow(
                            event = event,
                            onCopy = {
                                clipboard.setText(AnnotatedString(
                                    "[Batch#${event.batchId}][${formatTime(event.timestamp)}] " +
                                    "0x%04X ${event.propertyLabel} ".format(event.propertyCode) +
                                    "${event.rawValueBefore ?: "—"} → ${event.rawValueAfter ?: "—"} " +
                                    "(${event.decodedValue ?: "no decode"}) [${event.eventType.name}]"
                                ))
                            },
                        )
                    }
                    item(key = "divider_$batchId") {
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchHeader(batchId: Int, timestamp: Long, count: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Refresh #$batchId",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "$count prop${if (count == 1) "" else "s"}  ·  ${formatTime(timestamp)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventRow(event: TelemetryEvent, onCopy: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    val badgeColor = if (event.eventType == TelemetryEvent.EventType.FIRST_SEEN)
        MaterialTheme.colorScheme.tertiary
    else
        MaterialTheme.colorScheme.primary

    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = { menuExpanded = true })
            .padding(horizontal = 4.dp, vertical = 5.dp),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "0x%04X".format(event.propertyCode),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(event.propertyLabel, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    event.eventType.name,
                    fontSize = 10.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                "${event.rawValueBefore ?: "—"} → ${event.rawValueAfter ?: "—"}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            if (event.decodedValue != null) {
                Text(
                    event.decodedValue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("Copy") }, onClick = { onCopy(); menuExpanded = false })
        }
    }
}

// ──────────────────────────────── Helpers ─────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
    }
}
