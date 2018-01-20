import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.CheckBox
import tornadofx.*

class Payload : JsonModel {
    val registrationIdsProperty = SimpleListProperty<String>()
    var registrationIds by registrationIdsProperty

    val notificationProperty = SimpleObjectProperty<Notification>()
    var notification by notificationProperty

    val dataProperty = SimpleObjectProperty<Data>()
    var data by dataProperty
}

class Notification : JsonModel {
    val titleProperty = SimpleStringProperty()
    var title by titleProperty

    val bodyProperty = SimpleStringProperty()
    var body by bodyProperty
}

class Data : JsonModel {
    val dataProperty = SimpleMapProperty<String, String>()
    var values by dataProperty
}

class MainView : View("Firebase Push") {

    val api: Rest by inject()

    init {
        api.baseURI = "https://fcm.googleapis.com/fcm/"
        api.engine.requestInterceptor = {
            log.info("--> ${it.method} ${it.uri}\n${it.entity}")
        }
        api.engine.responseInterceptor = {
            log.info("<-- ${it.statusCode}\n${it.text()}")
        }
    }

    var dataField: CheckBox by singleAssign()
    var notificationField: CheckBox by singleAssign()

    override val root = form {
        fieldset("Config") {
            field("Server Key") {
                textfield()
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
                val payload = Payload()
                val key = "SERVER_KEY"
                api.post("send", payload) {
                    it.addHeader("Content-Type", "application/json")
                    it.addHeader("Authorization", "key=$key")
                }
            }
        }
    }
}

class FirebasePushApp : App(MainView::class)
