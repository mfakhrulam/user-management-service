package com.batch14.usermanagementservice.service.impl

import com.batch14.usermanagementservice.domain.constant.Constant
import com.batch14.usermanagementservice.domain.dto.request.ReqLoginDto
import com.batch14.usermanagementservice.domain.dto.request.ReqRegisterDto
import com.batch14.usermanagementservice.domain.dto.request.ReqUpdateUserDto
import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.domain.dto.response.ResLoginDto
import com.batch14.usermanagementservice.domain.entity.MasterUserEntity
import com.batch14.usermanagementservice.exception.CustomException
import com.batch14.usermanagementservice.repository.MasterRoleRepository
import com.batch14.usermanagementservice.repository.MasterUserRepository
import com.batch14.usermanagementservice.service.MasterUserService
import com.batch14.usermanagementservice.util.BCryptUtil
import com.batch14.usermanagementservice.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class MasterUserServiceImpl(
    private val masterUserRepository: MasterUserRepository,
    private val masterRoleRepository: MasterRoleRepository,
    private val bcrypt: BCryptUtil,
    private val jwtUtil: JwtUtil,
    private val httpServletRequest: HttpServletRequest
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

    override fun findUserById(userId: Int): ResGetUsersDto {
        val rawData = masterUserRepository.getUserById(userId)
        val result = ResGetUsersDto(
            username = rawData.username,
            id = rawData.id,
            email = rawData.email,
            roleId = rawData.role?.id,
            roleName = rawData.role?.name
        )
        return result
    }

    override fun register(req: ReqRegisterDto): ResGetUsersDto {
        val role = if(req.roleId == null) {
            Optional.empty()  // ini berarti optional.isEmpty bernilai true
            // beda dengan null
        } else {
            masterRoleRepository.findById(req.roleId)
        }

        // cek apakah role id ada
        if(role.isEmpty && req.roleId != null) {
            throw CustomException("Role ${req.roleId} tidak ditemukan", 400)
        }

        // cek apakah email sudah terdaftar
        val existingUserEmail = masterUserRepository.findFirstByEmail(req.email)
        println(existingUserEmail)
        if(existingUserEmail != null) {
            throw CustomException("Email sudah terdaftar", 400)
        }

        val existingUsername = masterUserRepository.findFirstByUsername(req.username)
        if (existingUsername.isPresent) {
            throw CustomException("Username sudah terdaftar", 400)
        }

        val hashPw = bcrypt.hash(req.password)

        val userRaw = MasterUserEntity(
            email = req.email,
            password = hashPw,
            username = req.username,
            role = if(role.isPresent){ // ini ga pake role != null
                role.get()
            } else {
                null
            }
        )
        // entity/row dari hasil save dijadikan sebagai return value
        // save bakal mengembalikan data dari db
        val user = masterUserRepository.save(userRaw)
        return ResGetUsersDto(
            id = user.id,
            email = user.email,
            username = user.username,
            roleId = user.role?.id,
            roleName = user.role?.name
        )

    }

    override fun login(req: ReqLoginDto): ResLoginDto {
        val userEntityOpt = masterUserRepository.findFirstByUsername(req.username)

        if (userEntityOpt.isEmpty) {
            throw CustomException("Username atau Password salah", 400)
        }

        val userEntity = userEntityOpt.get()
        if (!bcrypt.verify(req.password, userEntity.password)) {
            throw CustomException("Username atau Password salah", 400)
        }

        val role = if (userEntity.role != null) {
            userEntity.role!!.name
        } else {
            "user"
        }

        val token = jwtUtil.generateToken(userEntity.id, role)

        return ResLoginDto(token)
    }

    override fun findUsersByIds(
        ids: List<Int>
    ): List<ResGetUsersDto> {
        val rawData = masterUserRepository.findAllByIds(
            ids
        )
        return rawData.map {
            ResGetUsersDto(
                id = it.id,
                username = it.username,
                email = it.email
            )
        }
    }

    override fun updateUser(req: ReqUpdateUserDto): ResGetUsersDto {
        val userId = httpServletRequest.getHeader(Constant.HEADER_USER_ID)

        val user = masterUserRepository.findById(userId.toInt()).orElseThrow {
            throw CustomException(
                "User id ${userId} tidak ditemukan",
                HttpStatus.BAD_REQUEST.value()
            )
        }

        val existingUser = masterUserRepository.findFirstByUsername(req.username)
        if(existingUser.isPresent) {
           if(existingUser.get().id != user.id){
               throw CustomException(
                   "Username telah terdaftar",
                   HttpStatus.BAD_REQUEST.value()
               )
           }
        }

        user.email = req.email
        user.username = req.username
        user.updatedBy = userId

        val result = masterUserRepository.save(user)

        return ResGetUsersDto(
            id = result.id,
            username =  result.username,
            email = result.email
        )
    }



}