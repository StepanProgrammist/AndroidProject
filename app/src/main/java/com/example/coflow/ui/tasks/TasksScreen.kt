package com.example.coflow.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coflow.data.Task
import com.example.coflow.models.TaskStatus
import com.example.coflow.data.TasksTestData
import com.example.coflow.ui.components.*
import com.example.coflow.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.coflow.R

private enum class TaskGrouping { STATUS, DEADLINE }
private data class TaskGroup(val id: String, val title: String, val tasks: List<Task>)

@Composable
fun TasksScreen(onNavigateToCreateTask: () -> Unit, onNavigateToTaskDetail: (Int) -> Unit, onNavigateToProjects: () -> Unit) {
    var grouping by remember { mutableStateOf(TaskGrouping.STATUS) }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var collapsedGroups by remember { mutableStateOf(setOf("IN_PROGRESS")) }

    // Задача, для которой открыто подтверждение действия
    var pendingTask by remember { mutableStateOf<Task?>(null) }

    // Состояние уведомлений
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var snackbarColor by remember {
        mutableStateOf(Color(0xFF2860D9))
    }

    val scrollState = rememberScrollState()
    var previousScroll by remember { mutableStateOf(0) }
    var bottomVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.value) {
        val current = scrollState.value
        if (current > previousScroll + 2) bottomVisible = false
        else if (current < previousScroll - 2) bottomVisible = true
        previousScroll = current
    }

    val today = LocalDate.now()
    val filterNoneText = stringResource(R.string.filter_none)
    val filteredTasks = TasksTestData.tasks.filter { task ->
        if (selectedFilters.isEmpty() || selectedFilters.contains(filterNoneText)) true
        else {
            val isMineSelected = selectedFilters.contains(stringResource(R.string.filter_mine))
            val projectFilters = selectedFilters.filter { it.startsWith("Проект") }
            (isMineSelected && task.isMine) || projectFilters.any { task.projectName == it }
        }
    }

    val groups = when (grouping) {
        TaskGrouping.STATUS -> listOf(
            TaskGroup("TODO", stringResource(R.string.tasks_group_planned), filteredTasks.filter { it.status == TaskStatus.TODO }),
            TaskGroup("IN_PROGRESS", stringResource(R.string.tasks_group_active), filteredTasks.filter { it.status == TaskStatus.IN_PROGRESS }),
            TaskGroup("DONE", stringResource(R.string.tasks_group_completed), filteredTasks.filter { it.status == TaskStatus.DONE })
        )
        TaskGrouping.DEADLINE -> listOf(
            TaskGroup("TODAY", stringResource(R.string.tasks_group_today), filteredTasks.filter { it.dueDate == today }.sortedBy { it.dueDate }),
            TaskGroup("THIS_WEEK", stringResource(R.string.tasks_group_this_week), filteredTasks.filter { it.dueDate.isAfter(today) && !it.dueDate.isAfter(today.plusDays(6)) }.sortedBy { it.dueDate }),
            TaskGroup("NEXT_WEEK", stringResource(R.string.tasks_group_next_week), filteredTasks.filter { it.dueDate.isAfter(today.plusDays(6)) && !it.dueDate.isAfter(today.plusDays(13)) }.sortedBy { it.dueDate })
        )
    }

    val completedCount = filteredTasks.count { it.status == TaskStatus.DONE }
    val activeCount = filteredTasks.size - completedCount
    val filterButtonText = when {
        selectedFilters.isEmpty() || selectedFilters.contains(filterNoneText) -> filterNoneText
        selectedFilters.size == 1 -> selectedFilters.first()
        else -> "Выбрано: ${selectedFilters.size}"
    }

    Box(modifier = Modifier.fillMaxSize().background(coFlowTasksBackgroundBrush())) {

        //показывает уведомление сверху после действий свайпа
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp),
                snackbar = { data ->
                    Box(
                        modifier = Modifier
                            .widthIn(max = 220.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(snackbarColor)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.visuals.message,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            )

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = CoFlowSpacing.ScreenHorizontal)) {
            // ИСПРАВЛЕНО: Единый, чистый заголовок без стрелки назад, но с подзаголовком и колокольчиком
            CoFlowScreenHeader(
                title = stringResource(R.string.my_tasks_header),
                subtitle = "$activeCount ${stringResource(R.string.my_tasks_active)} · $completedCount ${stringResource(R.string.my_tasks_completed)}",
                trailingContent = {
                    NotificationBell()
                }
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.weight(1f).padding(bottom = 8.dp).clipToBounds().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groups.forEach { group ->
                    TaskGroupCard(
                        group = group,
                        expanded = group.id !in collapsedGroups,
                        onToggle = {
                            collapsedGroups =
                                if (group.id in collapsedGroups) {
                                    collapsedGroups - group.id
                                } else {
                                    collapsedGroups + group.id
                                }
                        },
                        onTaskClick = { task ->
                            onNavigateToTaskDetail(task.id)
                        },
                        onTaskSwipe = { task ->
                            pendingTask = task
                        }
                    )
                }
                Spacer(Modifier.height(18.dp))

                pendingTask?.let { task ->
                    val isTakingTask = task.status == TaskStatus.TODO

                    AlertDialog(
                        onDismissRequest = {
                            pendingTask = null
                        },
                        title = {
                            Text(
                                text = if (isTakingTask) {
                                    "Взять задачу?"
                                } else {
                                    "Завершить задачу?"
                                }
                            )
                        },
                        text = {
                            Text(
                                text = if (isTakingTask) {
                                    "Вы действительно хотите взять задачу «${task.title}» в работу?"
                                } else {
                                    "Вы действительно хотите отметить задачу «${task.title}» выполненной?"
                                }
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    snackbarColor = if (isTakingTask) {
                                        Color(0xFF2860D9)
                                    } else {
                                        Color(0xFF20A65A)
                                    }

                                    pendingTask = null

                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = if (isTakingTask) {
                                                "Вы успешно взяли задачу"
                                            } else {
                                                "Задача выполнена успешно"
                                            }
                                        )
                                    }
                                }
                            ) {
                                Text(if (isTakingTask) "Взять" else "Завершить")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    pendingTask = null
                                }
                            ) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(visible = bottomVisible) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.widthIn(max = 330.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        SortFilterPanel(
                            modifier = Modifier.weight(1f),
                            grouping = grouping,
                            onGroupingChange = { newGrouping -> grouping = newGrouping; collapsedGroups = emptySet() },
                            selectedFilters = selectedFilters,
                            filterButtonText = filterButtonText,
                            onFilterToggle = { option ->
                                if (option == filterNoneText) {
                                    selectedFilters = setOf(filterNoneText)
                                } else {
                                    val newSet = selectedFilters.toMutableSet()
                                    newSet.remove(filterNoneText)
                                    if (newSet.contains(option)) newSet.remove(option) else newSet.add(option)
                                    selectedFilters = newSet.toSet()
                                }
                            }
                        )
                        Spacer(Modifier.width(16.dp))

                        CoFlowFabButton(onClick = onNavigateToCreateTask, iconName = "task_plus")
                    }
                    Spacer(Modifier.height(7.dp))

                    CoFlowBottomNavigationBar(
                        items = listOf(
                            NavItem(icon = "tasks", title = stringResource(R.string.navigation_tasks), selected = true, onClick = {}),
                            NavItem(icon = "folder", title = stringResource(R.string.navigation_projects), selected = false, onClick = onNavigateToProjects),
                            NavItem(icon = "person", title = stringResource(R.string.navigation_profile), selected = false, onClick = { /* TODO: профиль */ })
                        )
                    )
                }
            }
        }

    }
}

@Composable
private fun TaskGroupCard(group: TaskGroup, expanded: Boolean, onToggle: () -> Unit, onTaskClick: (Task) -> Unit, onTaskSwipe: (Task) -> Unit
) {
    CoFlowGlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = group.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CoFlowColors.TextPrimary)
            Spacer(Modifier.width(7.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(CoFlowColors.BlueLight.copy(alpha = 0.38f)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                Text(text = group.tasks.size.toString(), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CoFlowColors.Blue)
            }
            Spacer(Modifier.weight(1f))
            Text(text = if (expanded) "⌄" else "›", color = CoFlowColors.Blue, fontSize = 17.sp)
        }

        if (expanded) {
            if (group.tasks.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = "Нет задач", fontSize = 11.sp, color = CoFlowColors.TextSecondary)
            } else {
                Spacer(Modifier.height(3.dp))
                group.tasks.forEachIndexed { index, task ->
                    TaskRow(
                        task = task,
                        onClick = { onTaskClick(task) },
                        onSwipe = onTaskSwipe
                    )
                    if (index < group.tasks.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CoFlowGlassStyles.BlueBorder.copy(alpha = 0.45f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onSwipe: (Task) -> Unit
) {
    var offsetX by remember(task.id) { mutableFloatStateOf(0f) }

    val swipeEnabled = task.isMine && task.status != TaskStatus.DONE

    val actionColor = when (task.status) {
        TaskStatus.TODO -> Color(0xFF2860D9)
        TaskStatus.IN_PROGRESS -> Color(0xFF20A65A)
        else -> Color.Transparent
    }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val maxSwipe = with(density) { 64.dp.toPx() }
    val swipeThreshold = with(density) { 42.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(actionColor.copy(alpha = 0.10f))
            .then(
                if (swipeEnabled) {
                    Modifier.pointerInput(task.id) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()

                                if (dragAmount < 0f || offsetX < 0f) {
                                    offsetX = (offsetX + dragAmount)
                                        .coerceIn(-maxSwipe, 0f)
                                }
                            },
                            onDragEnd = {
                                val shouldSwipe = offsetX <= -swipeThreshold
                                offsetX = 0f

                                if (shouldSwipe) {
                                    onSwipe(task)
                                }
                            },
                            onDragCancel = {
                                offsetX = 0f
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        // Цветная область и иконка под карточкой.
        // Иконка всегда по вертикальному центру и у правого края.
        if (swipeEnabled) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(actionColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.status == TaskStatus.TODO) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.size(27.dp)
                        ) {
                            drawCircle(
                                color = Color.White,
                                radius = size.width * 0.16f,
                                center = androidx.compose.ui.geometry.Offset(
                                    size.width / 2,
                                    size.height * 0.30f
                                ),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            drawArc(
                                color = Color.White,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    size.width * 0.18f,
                                    size.height * 0.43f
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    size.width * 0.64f,
                                    size.height * 0.48f
                                ),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Вся карточка, включая рамку, двигается целиком.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX
                }
                .clip(RoundedCornerShape(10.dp))
                .background(coFlowTasksBackgroundBrush())
                .border(
                    width = if (offsetX < -2f) 1.5.dp else 0.dp,
                    color = actionColor,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onClick() }
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = CoFlowColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (task.dueDate.isBefore(LocalDate.now()) &&
                    task.status != TaskStatus.DONE
                ) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.task_outdated),
                        fontSize = 8.sp,
                        color = CoFlowColors.Error
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.projectName,
                    fontSize = 10.sp,
                    color = CoFlowColors.TextSecondary,
                    maxLines = 1
                )

                Text(
                    text = task.dueDate.format(
                        DateTimeFormatter.ofPattern("dd.MM.yy")
                    ),
                    fontSize = 10.sp,
                    color = CoFlowColors.TextSecondary
                )
            }
        }
    }
}




// --- Внутренние компоненты для панели фильтров ---
@Composable
private fun SortFilterPanel(
    modifier: Modifier = Modifier,
    grouping: TaskGrouping,
    onGroupingChange: (TaskGrouping) -> Unit,
    selectedFilters: Set<String>,
    filterButtonText: String,
    onFilterToggle: (String) -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            var expanded by remember { mutableStateOf(false) }
            GlassControlSurface(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                Column {
                    Text(text = stringResource(R.string.grouping_header), fontSize = 9.sp, color = CoFlowColors.TextSecondary)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (grouping == TaskGrouping.STATUS) stringResource(R.string.grouping_status) else stringResource(R.string.grouping_deadline), fontSize = 11.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
                        Spacer(Modifier.width(3.dp))
                        Text("⌄", fontSize = 11.sp, color = CoFlowColors.Blue)
                    }
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(16.dp), containerColor = Color(0xFFF4F8FF).copy(alpha = 0.62f), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f))) {
                GlassGroupingOption(title = stringResource(R.string.grouping_status), selected = grouping == TaskGrouping.STATUS) { onGroupingChange(TaskGrouping.STATUS); expanded = false }
                GlassGroupingOption(title = stringResource(R.string.grouping_deadline), selected = grouping == TaskGrouping.DEADLINE) { onGroupingChange(TaskGrouping.DEADLINE); expanded = false }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            var expanded by remember { mutableStateOf(false) }
            GlassControlSurface(modifier = Modifier.fillMaxWidth().clickable { expanded = true }) {
                Column {
                    Text(text = stringResource(R.string.filter_header), fontSize = 9.sp, color = CoFlowColors.TextSecondary)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = filterButtonText, fontSize = 11.sp, color = CoFlowColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(3.dp))
                        Text("⌄", fontSize = 11.sp, color = CoFlowColors.Blue)
                    }
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(16.dp), containerColor = Color(0xFFF4F8FF).copy(alpha = 0.62f), tonalElevation = 0.dp, shadowElevation = 0.dp, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.82f))) {
                listOf(stringResource(R.string.filter_none), stringResource(R.string.filter_mine), "Проект 1", "Проект 2", "Проект 3").forEach { option ->
                    GlassFilterOption(title = option, selected = selectedFilters.contains(option)) { onFilterToggle(option) }
                }
            }
        }
    }
}

@Composable
private fun GlassControlSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(13.dp))
            .background(brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.30f), Color(0xFFDDEAFF).copy(alpha = 0.16f))))
            .border(width = 1.dp, brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.92f), CoFlowColors.Blue.copy(alpha = 0.14f))), shape = RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) { content() }
}

@Composable
private fun GlassGroupingOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) CoFlowColors.BlueLight.copy(alpha = 0.14f) else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
            if (selected) drawCircle(brush = Brush.linearGradient(colors = listOf(Color(0xFF6EA5FF), CoFlowColors.Blue), start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset(size.width, size.height)))
            else drawCircle(color = CoFlowGlassStyles.BlueBorder, style = Stroke(width = 1.2.dp.toPx()))
        }
        Spacer(Modifier.width(9.dp))
        Text(text = title, fontSize = 13.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
    }
}

@Composable
private fun GlassFilterOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (selected) CoFlowColors.BlueLight.copy(alpha = 0.14f) else Color.Transparent).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).clip(RoundedCornerShape(5.dp)).background(if (selected) CoFlowColors.Blue else Color.Transparent).border(width = 1.dp, color = if (selected) CoFlowColors.Blue else CoFlowGlassStyles.BlueBorder, shape = RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
            if (selected) Text("✓", fontSize = 10.sp, color = Color.White)
        }
        Spacer(Modifier.width(9.dp))
        Text(text = title, fontSize = 13.sp, color = CoFlowColors.TextPrimary, maxLines = 1)
    }
}

@Preview(name = "Tasks Screen - Phone", showBackground = true, widthDp = 390, heightDp = 844, showSystemUi = true)
@Composable
private fun TasksScreenPreview() {
    CoFlowTheme {
        TasksScreen(
            onNavigateToCreateTask = { println("🔥 PREVIEW: Нажата кнопка создания задачи (+)") },
            onNavigateToTaskDetail = { id -> println("PREVIEW: Нажата задача с ID: $id") },
            onNavigateToProjects = { println("PREVIEW: Нажата кнопка Проекты") }
        )
    }
}