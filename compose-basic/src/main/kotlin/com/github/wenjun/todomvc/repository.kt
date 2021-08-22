package com.github.wenjun.todomvc.repository

import androidx.compose.runtime.*
import com.github.wenjun.todomvc.Todo
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.util.*

enum class SearchFilter {
    All,
    Active,
    Finished
}

interface TodoRepository {
    suspend fun createNewTodo(name: String)
    suspend fun setTodoState(id: String, active: Boolean)
    suspend fun setFilter(filter: SearchFilter)

    @Composable
    fun storeState(): State<List<Todo>>

    @Composable
    fun filterState(): State<SearchFilter>

    @Composable
    fun todoLeftState():State<Int>
}

class DefaultTodoRepository : TodoRepository {
    private var store: PersistentList<Todo> = persistentListOf()
    private var currentFilter: SearchFilter = SearchFilter.All
    private var storeListeners: PersistentList<(List<Todo>) -> Unit> = persistentListOf()
    private var filterListeners: PersistentList<(SearchFilter) -> Unit> = persistentListOf()

    override suspend fun createNewTodo(name: String) {
        store = store.add(Todo(id = UUID.randomUUID().toString(), text = name, active = true))
        storeChanged()
    }

    override suspend fun setTodoState(id: String, active: Boolean) {

        when (val idx = store.indexOfFirst { it.id == id }) {
            -1 -> return
            else -> {
                val changed = store[idx].copy(active = active)
                store = store.set(idx, changed)
                storeChanged()
            }
        }
    }

    override suspend fun setFilter(filter: SearchFilter) {
        if (currentFilter != filter) {
            currentFilter = filter
            filterChanged()
            storeChanged()
        }
    }

    private fun filterChanged() {
        filterListeners.forEach { it(currentFilter) }
    }

    private fun storeChanged() {
        val newItems = filterred
        storeListeners.forEach { it(newItems) }
    }

    @Composable
    override fun storeState(): State<List<Todo>> {
        val state = remember { mutableStateOf(emptyList<Todo>()) }
        DisposableEffect(this) {
            val listener = { it: List<Todo> ->
                state.value = it
            }
            storeListeners = storeListeners.add(listener)
            onDispose { storeListeners = storeListeners.remove(listener) }
        }

        return state
    }

    @Composable
    override fun filterState(): State<SearchFilter> {
        val state = remember { mutableStateOf(currentFilter) }
        DisposableEffect(this) {
            val listener = { it: SearchFilter ->
                state.value = it
            }
            filterListeners = filterListeners.add(listener)
            onDispose { filterListeners = filterListeners.remove(listener) }
        }

        return state
    }

    @Composable
    override fun todoLeftState(): State<Int> {
        TODO("Not yet implemented")
    }

    private val filterred: List<Todo>
        get() = when (currentFilter) {
            SearchFilter.All -> store
            SearchFilter.Finished -> store.filterNot { it.active }
            SearchFilter.Active -> store.filter { it.active }
        }


}
