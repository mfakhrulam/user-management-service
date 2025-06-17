package com.batch14.usermanagementservice.controller

import com.batch14.usermanagementservice.domain.dto.request.ReqLoginDto
import com.batch14.usermanagementservice.domain.dto.request.ReqRegisterDto
import com.batch14.usermanagementservice.domain.dto.request.ReqUpdateUserDto
import com.batch14.usermanagementservice.domain.dto.response.BaseResponse
import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.domain.dto.response.ResLoginDto
import com.batch14.usermanagementservice.service.MasterUserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val masterUserService: MasterUserService
) {
    @GetMapping("/active")
    fun getAllActiveUser(): ResponseEntity<BaseResponse<List<ResGetUsersDto>>> {
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findAllActiveUsers()
            )
        )
    }

    @GetMapping("/{id}")
    fun getActiveUserById(
        @PathVariable(name = "id") idUser: Int
    ): ResponseEntity<BaseResponse<ResGetUsersDto>> {
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findUserById(idUser)
            )
        )
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: ReqRegisterDto
    ): ResponseEntity<BaseResponse<ResGetUsersDto>> {
        return ResponseEntity(
            BaseResponse(
                data = masterUserService.register(req),
                message = "Register Sukses"
            ),
            HttpStatus.CREATED
        )
    }

    @PostMapping("/login")
    fun login(
        @RequestBody req: ReqLoginDto
    ): ResponseEntity<BaseResponse<ResLoginDto>> {
        return ResponseEntity(
            BaseResponse(
                data = masterUserService.login(req),
                message = "Login Sukses"
            ),
            HttpStatus.OK
        )
    }

    @PutMapping()
    fun updateUser(
        @RequestBody req: ReqUpdateUserDto
    ): ResponseEntity<BaseResponse<ResGetUsersDto>> {
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.updateUser(req)
            )
        )
    }
}