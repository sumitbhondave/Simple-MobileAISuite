package com.sumit.simplemobileaisuite.core.permission

/**
 * Interface defining the contract for permission management.
 * This abstraction allows the app to check permissions without being coupled
 * to specific Android APIs.
 */
interface PermissionManager {
    /**
     * Checks if a specific permission is granted.
     *
     * @param permission The permission to check (e.g., Manifest.permission.CAMERA).
     * @return True if the permission is granted, false otherwise.
     */
    fun hasPermission(permission: String): Boolean
}
