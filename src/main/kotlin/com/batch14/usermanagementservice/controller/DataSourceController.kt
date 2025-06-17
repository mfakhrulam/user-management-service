package com.batch14.usermanagementservice.controller

import com.batch14.usermanagementservice.domain.dto.response.BaseResponse
import com.batch14.usermanagementservice.domain.dto.response.ResGetUsersDto
import com.batch14.usermanagementservice.service.MasterUserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/data-source")
class DataSourceController(
    private val masterUserService: MasterUserService
) {
    @GetMapping("/users/by-ids")
    fun getUsersByIds(
        @RequestParam(value = "ids", required = true) userIds: List<Int>
    ): ResponseEntity<BaseResponse<List<ResGetUsersDto>>>{
        return ResponseEntity.ok(
            BaseResponse(
                data = masterUserService.findUsersByIds(userIds)
            )
        )
    }

}