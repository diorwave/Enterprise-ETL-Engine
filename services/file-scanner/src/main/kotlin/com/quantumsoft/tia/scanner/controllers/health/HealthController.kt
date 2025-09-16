package com.quantumsoft.tia.scanner.controllers.health

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.data.redis.core.RedisTemplate
import com.quantumsoft.tia.scanner.components.QueueManager

@RestController
@RequestMapping("/api/v1/scanner")
@Tag(name = "Health", description = "Service health monitoring")
class HealthController(
    @Autowired(required = false) private val jdbcTemplate: JdbcTemplate?,
    @Autowired(required = false) private val redisTemplate: RedisTemplate<String, String>?,
    @Autowired(required = false) private val queueManager: QueueManager?
) : HealthIndicator {

    @GetMapping("/health")
    @Operation(
        summary = "Get service health status",
        description = "Check the health status of the service and its dependencies"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Service is healthy"),
        ApiResponse(responseCode = "503", description = "Service is unhealthy")
    )
    fun getHealth(): ResponseEntity<Map<String, Any>> {
        val health = health()
        val response = mapOf(
            "status" to health.status.code,
            "components" to mapOf(
                "database" to checkDatabase(),
                "redis" to checkRedis(),
                "queue" to checkQueue()
            ),
            "details" to health.details
        )
        
        val httpStatus = if (health.status == Health.up().build().status) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(503).body(response)
        }
        
        return httpStatus
    }

    override fun health(): Health {
        val builder = Health.Builder()
        
        val dbHealth = checkDatabase()
        val redisHealth = checkRedis()
        val queueHealth = checkQueue()
        
        if (dbHealth["status"] == "UP" && redisHealth["status"] == "UP" && queueHealth["status"] == "UP") {
            builder.up()
        } else {
            builder.down()
        }
        
        return builder
            .withDetail("scanner", "UP")
            .withDetail("database", dbHealth)
            .withDetail("redis", redisHealth)
            .withDetail("queue", queueHealth)
            .build()
    }
    
    private fun checkDatabase(): Map<String, String> {
        return try {
            jdbcTemplate?.queryForObject("SELECT 1", Int::class.java)
            mapOf("status" to "UP", "message" to "Database connection successful")
        } catch (e: Exception) {
            mapOf("status" to "DOWN", "message" to "Database connection failed: ${e.message}")
        }
    }
    
    private fun checkRedis(): Map<String, String> {
        return try {
            redisTemplate?.connectionFactory?.connection?.ping()
            mapOf("status" to "UP", "message" to "Redis connection successful")
        } catch (e: Exception) {
            mapOf("status" to "DOWN", "message" to "Redis connection failed: ${e.message}")
        }
    }
    
    private fun checkQueue(): Map<String, String> {
        return try {
            // Check if queue manager is operational
            if (queueManager != null) {
                mapOf("status" to "UP", "message" to "Queue manager operational")
            } else {
                mapOf("status" to "UNKNOWN", "message" to "Queue manager not initialized")
            }
        } catch (e: Exception) {
            mapOf("status" to "DOWN", "message" to "Queue check failed: ${e.message}")
        }
    }
}