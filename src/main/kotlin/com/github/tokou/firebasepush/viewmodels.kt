package com.github.tokou.firebasepush

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class KeyValueViewModel(key: String, value: String) {
    val keyProperty = SimpleStringProperty(key)
    var key by keyProperty
    val valueProperty = SimpleStringProperty(value)
    var value by valueProperty

    override fun equals(other: Any?): Boolean = when (other) {
        is KeyValueViewModel -> if (key == null) false else other.key == key
        else -> false
    }

    override fun hashCode(): Int {
        return keyProperty.hashCode()
    }
}

class PayloadViewModel : ItemViewModel<Payload>() {
    val tokens = SimpleListProperty<String>()
    val title = SimpleStringProperty()
    val body = SimpleStringProperty()
    val notification = SimpleBooleanProperty()
    val sound = SimpleBooleanProperty()
    val data = SimpleBooleanProperty()
    val values = SimpleListProperty<KeyValueViewModel>(FXCollections.observableArrayList())
    val selected = SimpleObjectProperty<KeyValueViewModel>(KeyValueViewModel("", ""))

    override fun onCommit() {
        val soundValue = if (sound.get()) "default" else null
        val payloadNotification = if (notification.value) Notification(title.get(), body.get(), soundValue) else null
        val payloadData = if (data.value) Data(convertValues()) else null
        val registrationIds = tokens.get() ?: emptyList<String>()
        item = Payload(registrationIds, payloadNotification, payloadData)
    }

    private fun convertValues() = values.map { it.key to it.value }.toMap()
}
