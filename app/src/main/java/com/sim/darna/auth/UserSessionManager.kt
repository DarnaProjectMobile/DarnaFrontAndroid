package com.sim.darna.auth

object UserSessionManager {
    var currentToken: String? = null
    var currentRole: String? = null

    fun login(token: String, role: String) {
        currentToken = token
        currentRole = role
    }

    fun logout() {
        currentToken = null
        currentRole = null
    }

    fun isSponsor(): Boolean = currentRole == "sponsor"
}
