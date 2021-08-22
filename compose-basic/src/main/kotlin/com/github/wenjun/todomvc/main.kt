package com.github.wenjun.todomvc

import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.wenjun.todomvc.repository.DefaultTodoRepository
import com.github.wenjun.todomvc.repository.SearchFilter
import com.github.wenjun.todomvc.repository.TodoRepository
import kotlinx.coroutines.launch

//val testData = listOf(
//    Todo("1", "Go Shopping", true),
//    Todo("2", "Refactor Backend", false),
//    Todo("3", "Add new feature to frontend", true),
//    Todo("4", "Go Shopping", true),
//    Todo("5", "Refactor Backend", false),
//    Todo("6", "Add new feature to frontend", true),
//    Todo("7", "Add new feature to frontend", true),
//    Todo("8", "Go Shopping", true),
//    Todo("9", "Refactor Backend", false),
//)

@Composable
fun Item(modifier: Modifier = Modifier, todo: Todo) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(width = 32.dp, height = 32.dp)
                    .border(
                        border = BorderStroke(1.dp, styles.BorderHintColor),
                        shape = CircleShape
                    ).clickable {
                        scope.launch {
                            repository.setTodoState(todo.id, !todo.active)
                        }
                    },
                contentAlignment = Alignment.Center,

                ) {
                if (!todo.active)
                    Icon(
                        Icons.Rounded.Check, contentDescription = null,
                        modifier = Modifier.size(width = 24.dp, height = 24.dp),
                        tint = styles.SecondaryColor
                    )
            }
            Spacer(Modifier.width(16.dp))

            Text(
                todo.text,
                style = if (todo.active) styles.ActiveItemTextStyle else styles.CompletedItemTextStyle,
                modifier = Modifier.weight(1f),
                textDecoration = if (todo.active) TextDecoration.None else TextDecoration.LineThrough
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun CreateNewTodo(repository: TodoRepository) {
    var newItem by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            Icons.Rounded.KeyboardArrowDown, contentDescription = null,
            modifier = Modifier.size(width = 64.dp, height = 64.dp),
            tint = styles.BorderHintColor
        )
        TextField(
            newItem, onValueChange = {
                newItem = it
            },
            modifier = Modifier.weight(1f).onPreviewKeyEvent {
                when {
                    it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                        val value = newItem
                        newItem = ""
                        scope.launch {
                            repository.createNewTodo(value)
                        }
                        true
                    }
                    else -> false
                }
            },
            textStyle = styles.CreateItemTextStyle,
            placeholder = {
                Text(
                    "What needs to be done?",
                    style = styles.CreateItemHintTextStyle
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ItemsBody(modifier: Modifier, store: List<Todo>) {
    Box(modifier = modifier) {
        val listState = rememberLazyListState()

        LazyColumn(state = listState) {
            itemsIndexed(store) { index, todo ->
                key(todo.id) {
                    Item(
                        modifier = Modifier.padding(
                            start = 24.dp,
                            top = 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )
                            .fillMaxWidth(),
                        todo
                    )
                }
                if (index != store.size - 1)
                    Divider(color = styles.BorderHintColor, thickness = 1.dp)
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = listState,
            )
        )
//        VerticalScrollbar(adapter = rememberScrollbarAdapter(stateVertical),
//        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}

@Composable
fun StatusBarFilterButton(name: String, selected: Boolean, onClicked: () -> Unit) {
    Box(
        modifier = (if (selected) Modifier.border(
            1.dp, color = styles.BorderHintColor,
            shape = RoundedCornerShape(3.dp)
        ) else Modifier).padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClicked)
    ) {
        Text(
            name,
            style = styles.StatusTextStyle,
            color = styles.StatusColor
        )
    }
}

@Composable
fun StatusBar(repository: TodoRepository, modifier: Modifier = Modifier) {
    val filterState by repository.filterState()
    val store by repository.storeState()
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier.padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "3 items left",
                style = styles.StatusTextStyle,
                color = styles.StatusColor
            )
            Row(
                modifier = Modifier.weight(2f)
            ) {
                Spacer(modifier = modifier.weight(1f))
                StatusBarFilterButton("All", filterState == SearchFilter.All,
                    onClicked = {
                        scope.launch {
                            repository.setFilter(SearchFilter.All)
                        }
                    })
                Spacer(modifier = modifier.width(8.dp))
                StatusBarFilterButton("Active", filterState == SearchFilter.Active,
                    onClicked = {
                        scope.launch {
                            repository.setFilter(SearchFilter.Active)
                        }
                    }
                )
                Spacer(modifier = modifier.width(8.dp))
                StatusBarFilterButton("Finished", filterState == SearchFilter.Finished,
                    onClicked = {
                        scope.launch {
                            repository.setFilter(SearchFilter.Finished)
                        }
                    })
            }
            Spacer(modifier = modifier.weight(1f))
        }
    }
}

val repository = DefaultTodoRepository()

fun main() = Window(title = "TodoMVC") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(styles.BackgroundColor)
    ) {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("todos", style = styles.TitleTextStyle)
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .drawColoredShadow(
                        offsetX = 0.dp, offsetY = 35.dp, shadowRadius = 60.dp, color = Color.Black,
                        alpha = 0.2f,
                    ),
            ) {
                Column(
                    Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CreateNewTodo(repository)
                    Divider(
                        color = styles.BorderHintColor,
                        thickness = 1.dp
                    )
                    val store by repository.storeState()
                    ItemsBody(modifier = Modifier.fillMaxWidth().weight(1f), store)
                    Divider(
                        color = styles.BorderHintColor,
                        thickness = 1.dp
                    )

                    StatusBar(repository)
                }
            }
        }
    }
}
