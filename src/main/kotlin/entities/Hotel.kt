package ex.entities
import kotlinx.serialization.Serializable

@Serializable
data class Hotel(val name: String,
                 val location: String,
                 val description: String,
                 val mealPlan: List<Meal>,
                 val stars: Int,
                 val isAvailable: Boolean)
