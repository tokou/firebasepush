package com.github.tokou.firebasepush

import tornadofx.*

data class Payload(
    private val registrationIds: List<String>,
    private val notification: Notification?,
    private val mutableContent: Boolean?,
    private val data: Data?
) : JsonModel {
    override fun toJSON(json: JsonBuilder) { with(json) {
        add("registration_ids", registrationIds)
        add("notification", notification)
        add("mutable_content", mutableContent)
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
