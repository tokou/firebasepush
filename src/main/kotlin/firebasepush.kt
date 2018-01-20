import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
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
    val dataProperty = SimpleMapProperty<String, String>()
    var values by dataProperty

    override fun toJSON(json: JsonBuilder) { with(json) {
        values.forEach { k, v -> add(k, v) }
    } }
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

    var dataField: CheckBox by singleAssign()
    var notificationField: CheckBox by singleAssign()
    var serverKeyField: TextField by singleAssign()

    val statusProperty = SimpleStringProperty("")
    var status by statusProperty

    override val root = form {
        fieldset("Config") {
            field("Server Key") {
                textfield(initialServerKey) {
                    serverKeyField = this
                }
            }
            field("Tokens") {
                textarea {
                    prefRowCount = 4
                    isWrapText = false
                }
            }
        }
        fieldset("Payload") {
            hbox {
                field {
                    checkbox("Data") {
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
                    checkbox("Notification") {
                        notificationField = this
                    }
                }
                vbox {
                    visibleWhen { notificationField.selectedProperty() }
                    field("Title") {
                        textfield()
                    }
                    field("Body") {
                        textfield()
                    }
                }
            }
        }
        button("Send") {
            action {
                runLater { status = "" }
                runAsyncWithProgress {
                    runLater { preferences {
                        put("server_key", serverKeyField.text)
                    } }
                    val payload = Payload()
                    api.post("send", payload) {
                        it.addHeader("Content-Type", "application/json")
                        it.addHeader("Authorization", "key=${serverKeyField.text}")
                    }
                } ui {
                    status = "${it.statusCode}: ${it.text()}"
                }
            }
        }
        label(statusProperty)
    }
}

class FirebasePushApp : App(MainView::class)
