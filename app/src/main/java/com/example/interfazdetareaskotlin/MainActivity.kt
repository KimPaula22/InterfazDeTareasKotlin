package com.example.interfazdetareaskotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.interfazdetareaskotlin.ui.theme.InterfazDeTareasKotlinTheme

data class Task(
    val title: String,
    var isCompleted: Boolean = false,
    var priority: Priority = Priority.MEDIUM
)

enum class Priority(val color: androidx.compose.ui.graphics.Color) {
    HIGH(Color.Red),
    MEDIUM(Color.Yellow),
    LOW(Color.Green)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterfazDeTareasKotlinTheme {
                TaskApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp() {
    val tasks = remember {
        mutableStateListOf(
            Task("Comprar alimentos", false, Priority.HIGH),
            Task("Llamar a mamá", true, Priority.MEDIUM),
            Task("Leer un libro", false, Priority.LOW)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Tareas") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Dividir las tareas según su estado
        val pendingTasks = tasks.filter { !it.isCompleted }
        val completedTasks = tasks.filter { it.isCompleted }

        Column(modifier = Modifier.padding(innerPadding)) {
            // Sección de tareas pendientes
            Text(
                text = "Pendientes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn {
                items(pendingTasks) { task ->
                    TaskCard(task = task) { updatedTask ->
                        updateTask(tasks, task, updatedTask) // Actualizar la lista
                    }
                }
            }

            Divider(modifier = Modifier.padding(8.dp))

            // Sección de tareas completadas
            Text(
                text = "Completadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn {
                items(completedTasks) { task ->
                    TaskCard(task = task) { updatedTask ->
                        updateTask(tasks, task, updatedTask) // Actualizar la lista
                    }
                }
            }
        }
    }
}

// Función para actualizar las tareas
fun updateTask(tasks: MutableList<Task>, oldTask: Task, updatedTask: Task) {
    val index = tasks.indexOf(oldTask)
    if (index != -1) {
        tasks[index] = updatedTask // Actualiza la tarea en la lista
    }
}

@Composable
fun TaskCard(task: Task, onTaskUpdated: (Task) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Badge(
                modifier = Modifier.size(24.dp),
                containerColor = task.priority.color
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Prioridad: ${task.priority.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (task.isCompleted) "Marcar como pendiente" else "Marcar como completada") },
                    onClick = {
                        val updatedTask = task.copy(isCompleted = !task.isCompleted)
                        expanded = false
                        onTaskUpdated(updatedTask) // Notificar el cambio con la nueva tarea
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cambiar prioridad") },
                    onClick = {
                        expanded = false
                        showPriorityDialog = true
                    }
                )
            }
        }
    }

    if (showPriorityDialog) {
        PriorityDialog(
            currentPriority = task.priority,
            onPrioritySelected = { newPriority ->
                val updatedTask = task.copy(priority = newPriority)
                showPriorityDialog = false
                onTaskUpdated(updatedTask)
            },
            onDismiss = { showPriorityDialog = false }
        )
    }
}

@Composable
fun PriorityDialog(
    currentPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar prioridad") },
        text = {
            Column {
                Priority.values().forEach { priority ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onPrioritySelected(priority) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(
                            containerColor = priority.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(priority.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TaskAppPreview() {
    InterfazDeTareasKotlinTheme {
        TaskApp()
    }
}