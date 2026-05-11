package ex.entities
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@SerialName("Hotel")
data class Hotel(
    override val name: String,
    val location: String,
    val description: String,
    val mealPlan: MutableList<Meal?>,
    val stars: Int,
    val isAvailable: Boolean
): JsonEntity
