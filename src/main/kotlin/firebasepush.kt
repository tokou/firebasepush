import tornadofx.*

class MainView : View("Firebase Push") {

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
                    checkbox("Notification")
                }
                vbox {
                    field("Title") {
                        textfield()
                    }
                    field("Body") {
                        textfield()
                    }
                }
            }
            hbox {
                field {
                    checkbox("Data")
                }
                vbox {
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
        }
        button("Send")
    }
}

class FirebasePushApp : App(MainView::class)
