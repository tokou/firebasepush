import javafx.scene.control.CheckBox
import tornadofx.*

class MainView : View("Firebase Push") {

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
        button("Send")
    }
}

class FirebasePushApp : App(MainView::class)
