import junit.framework.Assert.assertEquals
import org.junit.Test

class PayloadTest {
    @Test
    fun payload() {
        val notification = Notification("Hello", "World")
        val data = Data(mapOf("is" to "awesome", "my" to "data"))
        val payload = Payload(
            listOf("id1", "id2"),
            notification,
            data
        )
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
