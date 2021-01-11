package com.koofree.security

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationSuccessDto(
    val accessToken: String
)
