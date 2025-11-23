package com.sim.darna.auth

// ✅ Représente les données envoyées au backend NestJS lors de l'inscription
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String,              // ex: "client", "collocator", "sponsor"
    val dateDeNaissance: String,   // format: "YYYY-MM-DD"
    val numTel: String,
    val gender: String,            // "Male" ou "Female"
    val image: String? = null
) {
    fun normalized(): RegisterRequest {
        return this.copy(
            role = this.role.lowercase().trim()
        )
    }
}

// ✅ Réponse du backend après inscription
data class RegisterResponse(
    val message: String,
    val user: UserDto? = null
)
