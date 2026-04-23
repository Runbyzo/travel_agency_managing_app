package ex.entities
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Tour(
    val name: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val price: Double,
    val description: String,
    val hotel: Hotel
){
}
