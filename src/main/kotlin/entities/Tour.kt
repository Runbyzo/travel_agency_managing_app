package ex.entities
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tour(
    override val name: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val price: Double,
    val description: String,
    val hotelName: String
): JsonEntity {
}
