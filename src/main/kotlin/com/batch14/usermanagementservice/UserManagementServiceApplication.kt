package com.batch14.usermanagementservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableCaching
//@EnableAsync
class UserManagementServiceApplication

fun main(args: Array<String>) {
	runApplication<UserManagementServiceApplication>(*args)
}
