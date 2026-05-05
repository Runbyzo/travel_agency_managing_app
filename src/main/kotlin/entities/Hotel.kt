package ex.entities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Hotel(
    override val name: String,
    val location: String,
    val description: String,
    val mealPlan: List<Meal>,
    val stars: Int,
    val isAvailable: Boolean
): JsonEntity
