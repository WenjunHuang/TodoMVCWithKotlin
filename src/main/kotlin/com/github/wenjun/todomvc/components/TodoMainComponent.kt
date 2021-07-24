package com.github.wenjun.todomvc.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.instancekeeper.getOrCreate
import com.arkivanov.decompose.value.Value
import com.github.wenjun.todomvc.Todo


class TodoMainComponent(context:ComponentContext): ComponentContext by context {
//    val models:Value<Model>

    data class Model(
        val items:List<Todo>,
        val text:String
    )
}
