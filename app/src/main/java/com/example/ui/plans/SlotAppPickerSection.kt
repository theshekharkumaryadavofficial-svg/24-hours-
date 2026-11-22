package com.example.ui.plans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plans.InstalledAppInfo
import com.example.ui.theme.CrimsonBorderLight
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CrimsonSoftTint
import com.example.ui.theme.JetBlack
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LightTrack
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PureWhite

@Composable
fun SlotAppPickerSection(
    installedApps: List<InstalledAppInfo>,
    isLoadingApps: Boolean,
    selectedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSelectedOnly by remember { mutableStateOf(false) }

    val filteredApps = remember(installedApps, searchQuery, showSelectedOnly, selectedPackages) {
        val q = searchQuery.trim().lowercase()
        installedApps.filter { app ->
            val matchesFilter = !showSelectedOnly || app.packageName in selectedPackages
            val matchesQuery = q.isEmpty() ||
                app.label.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = LightTrack.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, CrimsonBorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ALLOWED APPS FOR THIS SLOT",
                        style = MaterialTheme.typography.labelMedium,
                        color = JetBlack,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(shape = RoundedCornerShape(50), color = CrimsonPrimary) {
                    Text(
                        text = "${selectedPackages.size} ON",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = PureWhite,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Turn ON only the apps allowed during this daily slot. All other apps (even newly installed apps) will open the Full-Screen Radha Naam Jap Blocker.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps (Netflix, YouTube, PDF...)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = MutedInk, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slot_app_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = CrimsonBorderLight,
                    cursorColor = CrimsonPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppFilterChip(
                    label = "All Apps (${installedApps.size})",
                    selected = !showSelectedOnly,
                    onClick = { showSelectedOnly = false },
                    modifier = Modifier.weight(1f)
                )
                AppFilterChip(
                    label = "Allowed (${selectedPackages.size})",
                    selected = showSelectedOnly,
                    onClick = { showSelectedOnly = true },
                    modifier = Modifier.weight(1f)
                )
                if (selectedPackages.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CrimsonSoftTint,
                        border = BorderStroke(1.dp, CrimsonBorderLight),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onClearAll)
                    ) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoadingApps -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CrimsonPrimary, modifier = Modifier.size(28.dp))
                    }
                }
                filteredApps.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (showSelectedOnly) {
                                "No apps enabled yet — all apps will be blocked with Radha Naam Jap."
                            } else {
                                "No matching installed apps found."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .testTag("slot_installed_apps_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isAllowed = app.packageName in selectedPackages
                            InstalledAppToggleRow(
                                app = app,
                                isAllowed = isAllowed,
                                onToggle = { onTogglePackage(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) JetBlack else PureWhite,
        border = BorderStroke(1.dp, if (selected) JetBlack else CrimsonBorderLight)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) PureWhite else JetBlack,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InstalledAppToggleRow(
    app: InstalledAppInfo,
    isAllowed: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(14.dp),
        color = if (isAllowed) CrimsonSoftTint else PureWhite,
        border = BorderStroke(
            width = if (isAllowed) 1.5.dp else 1.dp,
            color = if (isAllowed) CrimsonPrimary else LightTrack
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Surface(
                    shape = CircleShape,
                    color = JetBlack,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            color = PureWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = JetBlack,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isAllowed) "ALLOWED IN THIS SLOT" else app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAllowed) CrimsonPrimary else MutedInk,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isAllowed,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = CrimsonPrimary,
                    uncheckedThumbColor = JetBlack,
                    uncheckedTrackColor = LightTrack
                )
            )
        }
    }
}
