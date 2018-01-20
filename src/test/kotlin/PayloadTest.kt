import javafx.collections.FXCollections
import junit.framework.Assert.assertEquals
import org.junit.Test

class PayloadTest {
    @Test
    fun payload() {
        val payload = Payload()
        payload.registrationIds = FXCollections.observableArrayList("id1", "id2")
        val notification = Notification()
        notification.title = "Hello"
        notification.body = "World"
        val data = Data()
        data.values = FXCollections.observableHashMap()
        data.values["my"] = "data"
        data.values["is"] = "awesome"
        payload.notification = notification
        payload.data = data

        val json = payload.toJSON().toString()
        val expected = """
        {
            "registration_ids":["id1","id2"],
            "notification":{
                "title":"Hello",
                "body":"World"
            },
            "data":{
                "is":"awesome",
                "my":"data"
            }
        }
        """.replace("\\s+".toRegex(), "")
        assertEquals(expected, json)
    }
}
