package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.SiavashStudioViewModel
import com.example.ui.theme.*

@Composable
fun ProjectsScreen(
    viewModel: SiavashStudioViewModel,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val activeProjectId by viewModel.activeProjectId.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آرشیو پروژه‌های استودیو شخصی سیاوش",
                    color = GoldLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = StudioObsidian),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پروژه جدید", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(allProjects) { project ->
            val isSelected = project.id == activeProjectId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectProject(project.id)
                        viewModel.navigateTo(AppScreen.STUDIO)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GoldContainer else StudioCardDark
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GoldPrimary else StudioBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isSelected) GoldPrimary else StudioMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = project.name,
                                color = StudioCream,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "کوک: ${project.referenceTuning} • ریتم: ${project.bpm} BPM",
                                color = if (isSelected) GoldLight else StudioMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var selectedTuning by remember { mutableStateOf("A440 - دستگاه همایون و آواز اصفهان (کرن)") }
        var bpmText by remember { mutableStateOf("95") }

        val tunings = listOf(
            "A440 - دستگاه همایون و آواز اصفهان (کرن)",
            "A440 - دستگاه شور و آواز دشتی (میکروتونال)",
            "A440 - دستگاه چهارگاه و سه گاه (سری و کرن)",
            "A440 - دستگاه ماهور و راست پنجگاه",
            "A440 - ۱۲ گام استاندارد مساوی (12-TET)"
        )

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("ایجاد پروژه آوازی جدید", color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام پروژه موسیقی", color = StudioMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StudioCream,
                            unfocusedTextColor = StudioCream,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = StudioBorder
                        )
                    )

                    OutlinedTextField(
                        value = bpmText,
                        onValueChange = { bpmText = it },
                        label = { Text("سرعت ریتم (BPM)", color = StudioMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StudioCream,
                            unfocusedTextColor = StudioCream,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = StudioBorder
                        )
                    )

                    Text(text = "دستگاه / کوک مرجع میکروتونال:", color = GoldLight, fontSize = 11.sp)
                    tunings.forEach { tuning ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTuning = tuning }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (tuning == selectedTuning),
                                onClick = { selectedTuning = tuning },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = tuning, color = StudioCream, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createNewProject(
                        name = name,
                        tuning = selectedTuning,
                        bpm = bpmText.toIntOrNull() ?: 100
                    )
                    showCreateDialog = false
                    viewModel.navigateTo(AppScreen.STUDIO)
                }) {
                    Text("ایجاد پروژه", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("انصراف", color = StudioMuted)
                }
            },
            containerColor = StudioCardDark
        )
    }
}
