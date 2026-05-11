package ex.entities

import kotlinx.serialization.Serializable

@Serializable
sealed interface JsonEntity {
    val name: String
}