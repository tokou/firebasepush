package com.github.tokou.firebasepush

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.util.StringConverter
import tornadofx.*

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
        paddingAll = 10
        spacing = 10.0
        fieldset("Config", labelPosition = Orientation.VERTICAL) {
            spacing = 10.0
            field("Server Key") {
                textfield(initialServerKey) {
                    serverKeyField = this
                }
            }
            field("Tokens") {
                textarea(model.tokens, converter) {
                    tooltip("One token per line")
                    prefRowCount = 4
                    isWrapText = false
                }
            }
        }
        fieldset("Payload") {
            spacing = 10.0
            borderpane {
                left = vbox {
                    spacing = 10.0
                    paddingRight = 10.0
                    field {
                        checkbox("Notification", model.notification) {
                            notificationField = this
                        }
                    }
                    field {
                        checkbox("Data", model.data) {
                            dataField = this
                        }
                    }
                }
                center = vbox {
                    spacing = 10.0
                    fieldset {
                        spacing = 4.0
                        visibleWhen { notificationField.selectedProperty() }
                        field("Title") {
                            textfield(model.title)
                        }
                        field("Body") {
                            textfield(model.body)
                        }
                        checkbox("Sound", model.sound)
                    }
                    tableview<KeyValueViewModel>(model.values) {
                        tooltip("Edit values by double clicking")
                        visibleWhen { dataField.selectedProperty() }
                        isEditable = true
                        maxHeight = 170.0
                        bindSelected(model.selected)
                        columnResizePolicy = SmartResize.POLICY
                        column("Key", KeyValueViewModel::keyProperty).makeEditable().weightedWidth(1.0)
                        column("Value", KeyValueViewModel::valueProperty).makeEditable().weightedWidth(2.0)
                    }
                    hbox {
                        visibleWhen { dataField.selectedProperty() }
                        button("+") {
                            minWidth = 50.0
                            action {
                                model.values.add(KeyValueViewModel("key", "value"))
                            }
                        }
                        button("-") {
                            minWidth = 50.0
                            action {
                                model.values.remove(model.selected.get())
                            }
                        }
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
