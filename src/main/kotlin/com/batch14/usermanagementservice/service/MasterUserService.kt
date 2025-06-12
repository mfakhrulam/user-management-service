package com.batch14.usermanagementservice.service

import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto

interface MasterUserService {
    fun findAllActiveUsers(): List<ResGetUsersDto>
    fun findActiveUserById(userId: Int): ResGetUsersDto
}