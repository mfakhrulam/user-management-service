package com.batch14.usermanagementservice.service.impl

import com.batch14.usermanagementservice.domain.constant.Constant
import com.batch14.usermanagementservice.domain.dto.request.ReqLoginDto
import com.batch14.usermanagementservice.domain.dto.request.ReqRegisterDto
import com.batch14.usermanagementservice.domain.dto.request.ReqUpdateUserDto
import com.batch14.usermanagementservice.domain.dto.response.ResDeletedUserDto
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
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.Optional
import kotlin.text.toInt

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

    // kalau data belum ada, data akan di simpan
    // kalau data di redis udah ada bakal langsung return data dari redis
//    @Cacheable(
//        "getUserById",
//        key= "{#userId}" // harus sama dengan yg di parameter
//    )
    override fun findUserById(userId: Int): ResGetUsersDto {
        val user = masterUserRepository.getUserById(userId).orElseThrow {
            throw CustomException(
                "User id $userId tidak ditemukan",
                HttpStatus.BAD_REQUEST.value()
            )
        }

        val result = ResGetUsersDto(
            username = user.username,
            id = user.id,
            email = user.email,
            roleId = user.role?.id,
            roleName = user.role?.name
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

//    @CacheEvict(
//        value = ["getUserById"],
//        key = "{#userId}"
////        allEntries = true
//    )
    override fun updateUser(req: ReqUpdateUserDto, userId: Int): ResGetUsersDto {
        val user = masterUserRepository.findById(userId.toInt()).orElseThrow {
            throw CustomException(
                "User id $userId tidak ditemukan",
                HttpStatus.BAD_REQUEST.value(),
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
        user.updatedBy = userId.toString()

        val result = masterUserRepository.save(user)

        return ResGetUsersDto(
            id = result.id,
            username =  result.username,
            email = result.email
        )
    }

    // tidak ada penjelasan bisa hapus ke siapa? ke diri sendiri atau gimana? hapus user lain?
    // akhirnya untuk delete ku bebaskan semua, kecuali kalau id gaada,
    // role admin bisa hard delete ke semua user,
    // semua role bisa soft delete ke siapapun
    @Transactional
    override fun hardDeleteUserById(userId: Int): ResDeletedUserDto {
        // isinya "admmin" atau "user"
        val roleUser = httpServletRequest.getHeader(Constant.HEADER_USER_ROLE)
        // tidak perlu pencocokan user id dan role karena masuk dari JWT token

        if(!roleUser.equals("admin")) {
            throw CustomException(
                "Unauthorized user",
                HttpStatus.UNAUTHORIZED.value()
            )
        }

        val deleteUser = masterUserRepository.findById(userId.toInt()).orElseThrow {
            throw CustomException(
                "User id $userId tidak ditemukan",
                HttpStatus.BAD_REQUEST.value()
            )
        }

        masterUserRepository.hardDeleteUser(deleteUser.id)
        return ResDeletedUserDto(
            id = deleteUser.id,
            username = deleteUser.username,
            deletedAt = Timestamp(System.currentTimeMillis())
        )
    }

    override fun softDeleteUserById(userId: Int): ResDeletedUserDto {
        val requestUserId = httpServletRequest.getHeader(Constant.HEADER_USER_ID)
        val deleteUser = masterUserRepository.findById(userId.toInt()).orElseThrow {
            throw CustomException(
                "User id $userId tidak ditemukan",
                HttpStatus.BAD_REQUEST.value(),
            )
        }

        deleteUser.isActive = false
        deleteUser.isDelete = true
        deleteUser.deletedBy = requestUserId.toString()
        deleteUser.deletedAt = Timestamp(System.currentTimeMillis())

        val result = masterUserRepository.save(deleteUser)

        return ResDeletedUserDto(
            id = result.id,
            username = result.username,
            deletedAt = result.deletedAt!!
        )

    }
}