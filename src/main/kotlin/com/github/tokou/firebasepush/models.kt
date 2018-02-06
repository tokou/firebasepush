package com.github.tokou.firebasepush

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
    private val body: String?,
    private val sound: String?
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        add("title", title)
        add("body", body)
        add("sound", sound)
    } }
}

data class Data(
    private val values: Map<String, String>
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        values.forEach { k, v -> add(k, v) }
    } }
}

data class Priority(
    val priority: String
)

sealed class PriorityValue {
    abstract val value: String

    object HIGH: PriorityValue() {
        override val value: String
            get() = "high"
    }

    object DEFAULT: PriorityValue() {
        override val value: String
            get() = "normal"
    }
}
