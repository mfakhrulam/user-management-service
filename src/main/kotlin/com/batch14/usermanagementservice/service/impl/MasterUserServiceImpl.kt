package com.batch14.usermanagementservice.service.impl

import com.batch14.usermanagementservice.domain.dto.response.ResGetUserByIdDto
import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.repository.MasterUserRepository
import com.batch14.usermanagementservice.service.MasterUserService
import org.springframework.stereotype.Service

@Service
class MasterUserServiceImpl(
    private val masterUserRepository: MasterUserRepository
): MasterUserService {
    override fun findAllActiveUsers(): List<ResGetUsersDto> {
        val rawData = masterUserRepository.getAllActiveUser()
        val result = mutableListOf<ResGetUsersDto>()
        rawData.forEach { u ->
            result.add(
                ResGetUsersDto(
                    username = u.username,
                    id = u.id,
                    email = u.email,
                    // jika user memiliki role maka ambil id role
                    // jika tidak ada maka value null
                    roleId = u.role?.id,
                    // jika user memiliki role maka ambil nama role
                    roleName = u.role?.name
                )
            )
        }
        return result
    }

    override fun findActiveUserById(userId: Int): ResGetUserByIdDto {
        val rawData = masterUserRepository.getActiveUserById(userId)
        val result = ResGetUserByIdDto(
            username = rawData.username,
            id = rawData.id,
            email = rawData.email,
            roleId = rawData.role?.id,
            roleName = rawData.role?.name
        )
        return result
    }
}