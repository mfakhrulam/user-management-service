package com.batch14.usermanagementservice.controller

import com.batch14.usermanagementservice.domain.dto.request.ReqRedisDto
import com.batch14.usermanagementservice.domain.dto.response.BaseResponse
import com.batch14.usermanagementservice.service.RedisExampleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/redis-example")
class RedisExampleController(
    private val redisExampleService: RedisExampleService
) {
    @PostMapping()
    fun setValue(
        @RequestBody req: ReqRedisDto
    ): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity.ok(
            BaseResponse(
                data= redisExampleService.set(req.userId)
            )
        )
    }
    @GetMapping("/{userId}")
    fun getValue(
        @PathVariable userId: Int
    ): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity.ok(
            BaseResponse(
                data = redisExampleService.get(userId)
            )
        )
    }
}