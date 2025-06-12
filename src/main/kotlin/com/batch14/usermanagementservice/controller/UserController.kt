package com.batch14.usermanagementservice.controller

import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.service.MasterUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val masterUserService: MasterUserService
) {
    @GetMapping("/active")
    fun getAllActiveUser(): ResponseEntity<List<ResGetUsersDto>>{
        return ResponseEntity.ok(
            masterUserService.findAllActiveUsers()
        )
    }

    @GetMapping("/active/{id}")
    fun getActiveUserById(
        @PathVariable(name = "id") idUser: Int
    ): ResponseEntity<ResGetUsersDto> {
        return ResponseEntity.ok(
            masterUserService.findActiveUserById(idUser)
        )
    }
}