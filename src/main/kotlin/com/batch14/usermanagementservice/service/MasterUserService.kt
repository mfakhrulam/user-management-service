package com.batch14.usermanagementservice.service

import com.batch14.usermanagementservice.domain.dto.request.ReqLoginDto
import com.batch14.usermanagementservice.domain.dto.request.ReqRegisterDto
import com.batch14.usermanagementservice.domain.dto.request.ReqUpdateUserDto
import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.domain.dto.response.ResLoginDto

interface MasterUserService {
    fun findAllActiveUsers(): List<ResGetUsersDto>
    fun findUserById(userId: Int): ResGetUsersDto
    fun register(req: ReqRegisterDto): ResGetUsersDto
    fun login(req: ReqLoginDto): ResLoginDto
    fun findUsersByIds(ids: List<Int>): List<ResGetUsersDto>
    fun updateUser(req: ReqUpdateUserDto): ResGetUsersDto
}