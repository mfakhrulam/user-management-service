package com.batch14.usermanagementservice.domain.dto.response

import java.sql.Timestamp

data class ResDeletedUserDto(
    val id: Int,
    val username: String,
    val deletedAt: Timestamp
)
