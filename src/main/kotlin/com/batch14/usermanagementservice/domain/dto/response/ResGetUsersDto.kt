package com.batch14.usermanagementservice.domain.dto.response

import java.io.Serializable

data class ResGetUsersDto(
    val id: Int,
    val email: String,
    val username: String,
    var roleId: Int? = null,
    var roleName: String? = null
): Serializable {
    companion object {
        private const val serialVersionUID: Long = -3554204184573678271L
    }
}
