package com.batch14.usermanagementservice.service.impl

import com.batch14.usermanagementservice.exception.CustomException
import com.batch14.usermanagementservice.repository.MasterUserRepository
import com.batch14.usermanagementservice.service.RedisExampleService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisExampleServiceImpl(
    private val stringRedisTemplate: StringRedisTemplate,
    private val masterUserRepository: MasterUserRepository
): RedisExampleService {
    override fun set(userId: Int): String {
        val user = masterUserRepository.findById(userId).orElseThrow {
            throw CustomException("User not found", 404)
        }

        val operationsString = stringRedisTemplate.opsForValue()

        // timeout di sini sama kaya time-to-live di application.yaml
        operationsString.set(
            "user-service:user:username:${user.id}",
            user.username,
            Duration.ofMinutes(1)
        )

        return "User with ID ${user.id} has been in redis"
    }

    override fun get(userId: Int): String {
        val user = masterUserRepository.findById(userId).orElseThrow {
            throw CustomException("User not found", 404)
        }

        val operationsString = stringRedisTemplate.opsForValue()

        val username = operationsString.get("user-service:user:username:${user.id}")
            ?: throw CustomException("Username not found in Redis", 404)

        return username
    }

}