package com.example.coflow

import android.os.Bundle
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coflow.ui.auth.AuthRoute
import com.example.coflow.ui.tasks.ChooseAssigneeScreen
import com.example.coflow.ui.tasks.ChooseDateScreen
import com.example.coflow.ui.tasks.ChooseProjectScreen
import com.example.coflow.ui.tasks.CreateTaskRoute
import com.example.coflow.ui.tasks.TaskDetailScreen
import com.example.coflow.ui.tasks.TasksScreen
import com.example.coflow.ui.tasks.EditTaskScreen
import com.example.coflow.ui.tasks.EditTaskViewModel
import com.example.coflow.ui.theme.CoFlowTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.coflow.ui.tasks.EditTaskRoute

object Screen {
    const val TASKS = "tasks"
    const val CREATE_TASK = "create_task"
    const val CHOOSE_ASSIGNEE = "choose_assignee"
    const val CHOOSE_PROJECT = "choose_project"
    const val CHOOSE_DATE = "choose_date"

    const val TASK_DETAIL = "task_detail/{taskId}"
    const val EDIT_TASK = "edit_task/{taskId}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CoFlowTheme {
                var isAuthenticated by remember { mutableStateOf(true) }
                var showDeleteConfirmation by remember { mutableStateOf(false) }

                if (isAuthenticated) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.TASKS) {
                        composable(Screen.TASKS) {
                            TasksScreen(
                                onNavigateToCreateTask = { navController.navigate(Screen.CREATE_TASK) },
                                onNavigateToTaskDetail = { taskId -> navController.navigate("task_detail/$taskId") }
                            )
                        }

                        // Экран просмотра задачи
                        composable(
                            route = Screen.TASK_DETAIL,
                            arguments = listOf(androidx.navigation.navArgument("taskId") { type = androidx.navigation.NavType.IntType })
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId")
                            // Ищем задачу в тестовых данных по ID
                            val task = com.example.coflow.data.TasksTestData.tasks.find { it.id == taskId }

                            if (task != null) {
                                TaskDetailScreen(
                                    task = task,
                                    onBackClick = { navController.popBackStack() },
                                    onEditClick = {
                                        navController.navigate("edit_task/${task.id}")
                                    }
                                )
                            }
                        }


                        composable(
                            route = Screen.EDIT_TASK,
                            arguments = listOf(
                                androidx.navigation.navArgument("taskId") {
                                    type = androidx.navigation.NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId")
                            val task = com.example.coflow.data.TasksTestData.tasks
                                .find { it.id == taskId }

                            if (task != null) {
                                EditTaskRoute(
                                    task = task,
                                    savedStateHandle = backStackEntry.savedStateHandle,
                                    onBackClick = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToAssignees = {
                                        navController.navigate(Screen.CHOOSE_ASSIGNEE)
                                    },
                                    onNavigateToProject = {
                                        navController.navigate(Screen.CHOOSE_PROJECT)
                                    },
                                    onNavigateToDate = {
                                        navController.navigate(Screen.CHOOSE_DATE)
                                    }
                                )
                            }
                        }




                        composable(Screen.CREATE_TASK) {
                            CreateTaskRoute(
                                onBackClick = { navController.popBackStack() },
                                onTaskCreated = { formData ->
                                    // TODO: Сохранение задачи через TasksRepository
                                    navController.popBackStack()
                                },
                                onNavigateToAssignees = { navController.navigate(Screen.CHOOSE_ASSIGNEE) },
                                onNavigateToProject = { navController.navigate(Screen.CHOOSE_PROJECT) },
                                onNavigateToDate = { navController.navigate(Screen.CHOOSE_DATE) }
                            )
                        }

                        // Экран выбора исполнителей
                        composable(Screen.CHOOSE_ASSIGNEE) {
                            ChooseAssigneeScreen(
                                onBackClick = { navController.popBackStack() },
                                onConfirmSelection = { selectedAssignees ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(
                                            "selected_task_assignees",
                                            selectedAssignees.map { it.name }.toTypedArray()
                                        )

                                    navController.popBackStack()
                                }
                            )

                        }

                        // Экран выбора проекта
                        composable(Screen.CHOOSE_PROJECT) {
                            ChooseProjectScreen(
                                onBackClick = { navController.popBackStack() },
                                onConfirmSelection = { selectedProject ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selected_task_project", selectedProject)

                                    navController.popBackStack()
                                }
                            )
                        }

                        //экран выбора даты
                        composable(Screen.CHOOSE_DATE) {
                            ChooseDateScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onConfirmSelection = { selectedDate ->
                                    // Возвращаем выбранную дату в предыдущий экран.
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("selected_task_date", selectedDate)

                                    navController.popBackStack()
                                }
                            )
                        }


                    }
                } else {
                    AuthRoute(onAuthSuccess = { isAuthenticated = true })
                }
                if (showDeleteConfirmation) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteConfirmation = false
                        },
                        title = {
                            Text("Удаление задачи")
                        },
                        text = {
                            Text("Вы действительно хотите удалить эту задачу?")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation = false

                                    android.widget.Toast.makeText(
                                        this@MainActivity,
                                        "хихи, я не удалю",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            ) {
                                Text("Удалить точно")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation = false
                                }
                            ) {
                                Text("Отмена")
                            }
                        }
                    )
                }
            }
        }
    }
}