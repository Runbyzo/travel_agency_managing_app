package ex.entities
import kotlinx.serialization.Serializable

@Serializable
data class Meal(
    val id: Int,
    val description: String,
    val schedule: String,
    val isAvailable: Boolean
    )
