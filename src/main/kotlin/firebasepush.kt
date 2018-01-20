import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.util.StringConverter
import tornadofx.*

class Payload : JsonModel {
    val registrationIdsProperty = SimpleListProperty<String>()
    var registrationIds by registrationIdsProperty

    val notificationProperty = SimpleObjectProperty<Notification>()
    var notification by notificationProperty

    val dataProperty = SimpleObjectProperty<Data>()
    var data by dataProperty

    override fun toJSON(json: JsonBuilder) { with(json) {
        add("registration_ids", registrationIds)
        add("notification", notification)
        add("data", data)
    } }
}

class Notification : JsonModel {
    val titleProperty = SimpleStringProperty()
    var title by titleProperty

    val bodyProperty = SimpleStringProperty()
    var body by bodyProperty

    override fun toJSON(json: JsonBuilder) { with(json) {
        add("title", title)
        add("body", body)
    } }
}

class Data : JsonModel {
    val valuesProperty = SimpleMapProperty<String, String>()
    var values by valuesProperty

    override fun toJSON(json: JsonBuilder) { with(json) {
        values?.forEach { k, v -> add(k, v) }
    } }
}

class PayloadModel : ItemViewModel<Payload>(Payload()) {
    val tokens = bind { item?.registrationIdsProperty }
    val title = SimpleStringProperty()
    val body = SimpleStringProperty()
    val notification = SimpleBooleanProperty()
    val data = SimpleBooleanProperty()

    init {
        notification.addListener { _, o, n ->
            if (n && !o) item?.notification = Notification()
            if (!n && o) item?.notification = null
            item?.notification?.titleProperty?.bind(title)
            item?.notification?.bodyProperty?.bind(body)
        }
        data.addListener { _, o, n ->
            if (n && !o) item?.data = Data()
            if (!n && o) item?.data = null
        }
    }
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

    val model: PayloadModel by inject()

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
                }
            }
        }
        fieldset("Payload") {
            hbox {
                field {
                    checkbox("Data", model.data) {
                        dataField = this
                    }
                }
                vbox {
                    visibleWhen { dataField.selectedProperty() }
                    hbox {
                        field("Key") {
                            textfield()
                        }
                        field("Value") {
                            textfield()
                        }
                        button("+")
                    }
                }
            }
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
