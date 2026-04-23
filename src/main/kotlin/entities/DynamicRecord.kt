import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DynamicRecord(
    val name: String,
    val fields: LinkedHashMap<String, JsonElement>
)
