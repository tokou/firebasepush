package com.github.tokou.firebasepush

import javafx.scene.text.FontWeight
import tornadofx.*

class Style : Stylesheet() {
    init {
        legend {
            fontWeight = FontWeight.BOLD
            fontSize = 20.px
        }
    }
}

class FirebasePushApp : App(MainView::class, Style::class)
