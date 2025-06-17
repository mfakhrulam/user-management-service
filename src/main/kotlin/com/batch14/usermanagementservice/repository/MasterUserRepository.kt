package com.batch14.usermanagementservice.repository

import com.batch14.usermanagementservice.domain.entity.MasterUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface MasterUserRepository: JpaRepository<MasterUserEntity, Int> {
    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
    """, nativeQuery = false)
    fun getAllActiveUser(): List<MasterUserEntity>

    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
        AND U.id = :userId
    """, nativeQuery = false)
    fun getUserById(
        @Param("userId") userId: Int
    ): Optional<MasterUserEntity>

    fun findFirstByEmail(email: String): MasterUserEntity?

    @Query("""
        SELECT U FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
        AND U.username = :username
    """, nativeQuery = false)
    fun findFirstByUsername(username: String): Optional<MasterUserEntity>

    @Query("""
        SELECT u FROM MasterUserEntity u
        WHERE u.id IN (:ids)
    """, nativeQuery = false)
    fun findAllByIds(ids: List<Int>): List<MasterUserEntity>

    @Modifying
    @Query("""
        DELETE FROM MasterUserEntity U
        WHERE U.isDelete = false
        AND U.isActive = true
        AND U.id = :userId
    """, nativeQuery = false)
    fun hardDeleteUser(
        @Param("userId") userId: Int
    )

}