import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.util.StringConverter
import tornadofx.*

data class Payload(
    private val registrationIds: List<String>,
    private val notification: Notification?,
    private val data: Data?
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        add("registration_ids", registrationIds)
        add("notification", notification)
        add("data", data)
    } }
}

data class Notification(
    private val title: String?,
    private val body: String?
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        add("title", title)
        add("body", body)
    } }
}

data class Data(
    private val values: Map<String, String>
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        values.forEach { k, v -> add(k, v) }
    } }
}

class KeyValueModel(key: String, value: String) {
    val keyProperty = SimpleStringProperty(key)
    var key by keyProperty
    val valueProperty = SimpleStringProperty(value)
    var value by valueProperty

    override fun equals(other: Any?): Boolean = when (other) {
        is KeyValueModel -> if (key == null) false else other.key == key
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
    val data = SimpleBooleanProperty()
    val values = SimpleListProperty<KeyValueModel>(FXCollections.observableArrayList())
    val selected = SimpleObjectProperty<KeyValueModel>(KeyValueModel("", ""))

    override fun onCommit() {
        val payloadNotification = if (notification.value) Notification(title.get(), body.get()) else null
        val payloadData = if (data.value) Data(convertValues()) else null
        val registrationIds = tokens.get() ?: emptyList<String>()
        item = Payload(registrationIds, payloadNotification, payloadData)
    }

    private fun convertValues() = values.map { it.key to it.value }.toMap()
}

class MainView : View("Firebase Push") {

    val api: Rest by inject()

    lateinit var initialServerKey: String

    init {
        with(api) {
            baseURI = "https://fcm.googleapis.com/fcm/"
            with(engine) {
                requestInterceptor = {
                log.info("--> ${it.method} ${it.uri}\n${it.entity}")
                }
                responseInterceptor = {
                    log.info("<-- ${it.statusCode}\n${it.text()}")
                }
            }
        }
        preferences {
            initialServerKey = get("server_key", "")
        }
    }

    val model: PayloadViewModel by inject()

    var dataField: CheckBox by singleAssign()
    var notificationField: CheckBox by singleAssign()
    var serverKeyField: TextField by singleAssign()

    val statusProperty = SimpleStringProperty("")
    var status by statusProperty

    val converter = object : StringConverter<ObservableList<String>>() {
        override fun toString(`object`: ObservableList<String>?): String =
            `object`?.joinToString("\n") ?: ""

        override fun fromString(string: String?): ObservableList<String> =
            (string?.split("\n")?: emptyList()).observable()
    }

    override val root = form {
        fieldset("Config") {
            field("Server Key") {
                textfield(initialServerKey) {
                    serverKeyField = this
                }
            }
            field("Tokens") {
                textarea(model.tokens, converter) {
                    prefRowCount = 4
                    isWrapText = false
                    vgrow = Priority.ALWAYS
                }
            }
        }
        fieldset("Payload") {
            hbox {
                field {
                    checkbox("Notification", model.notification) {
                        notificationField = this
                    }
                }
                vbox {
                    visibleWhen { notificationField.selectedProperty() }
                    field("Title") {
                        textfield(model.title)
                    }
                    field("Body") {
                        textfield(model.body)
                    }
                }
            }
            hbox {
                field {
                    checkbox("Data", model.data) {
                        dataField = this
                    }
                }
                tableview<KeyValueModel>(model.values) {
                    visibleWhen { dataField.selectedProperty() }
                    isEditable = true
                    maxHeight = 200.0
                    bindSelected(model.selected)
                    column("Key", KeyValueModel::keyProperty).makeEditable()
                    column("Value", KeyValueModel::valueProperty).makeEditable()
                }
                vbox {
                    visibleWhen { dataField.selectedProperty() }
                    button("+") { action {
                        model.values.add(KeyValueModel("key", "value"))
                    } }
                    button("-") { action {
                        model.values.remove(model.selected.get())
                    } }
                }
            }
        }
        button("Send") {
            action {
                runLater { status = "" }
                model.commit {
                    runAsyncWithProgress {
                        runLater {
                            preferences {
                                put("server_key", serverKeyField.text)
                            }
                        }
                        api.post("send", model.item) {
                            it.addHeader("Content-Type", "application/json")
                            it.addHeader("Authorization", "key=${serverKeyField.text}")
                        }
                    } ui {
                        status = "${it.statusCode}: ${it.text()}"
                    }
                }
            }
        }
        label(statusProperty)
    }
}

class FirebasePushApp : App(MainView::class)
