package com.v2ray.ang.service

import android.content.Context
import android.util.Log
import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import java.io.File

object CustomCoreManager {
    private const val TAG = "CustomCoreManager"
    private var currentProcess: Process? = null
    
    // Static port assignments for custom cores to avoid conflicts
    private const val PORT_AMNEZIAWG = 20001
    private const val PORT_DNSTT = 20002
    private const val PORT_SLIPSTREAM = 20003
    private const val PORT_SUSHMODE = 20004
    private const val PORT_TUIC = 20005

    fun getCorePort(config: ProfileItem): Int {
        return when (config.configType) {
            EConfigType.AMNEZIAWG -> PORT_AMNEZIAWG
            EConfigType.DNSTT -> PORT_DNSTT
            EConfigType.SLIPSTREAM -> PORT_SLIPSTREAM
            EConfigType.SUSH_MODE -> PORT_SUSHMODE
            EConfigType.TUIC -> PORT_TUIC
            else -> -1
        }
    }

    fun startCustomCore(context: Context, config: ProfileItem): Boolean {
        stopCustomCore() // Ensure any previous instance is stopped
        
        val binaryName = getBinaryName(config.configType) ?: return false
        val port = getCorePort(config)
        
        // Ensure binary is executable
        val binaryFile = File(context.filesDir, binaryName)
        if (!binaryFile.exists()) {
            Log.e(TAG, "Binary not found: $binaryName")
            return false
        }
        binaryFile.setExecutable(true)

        try {
            val command = buildCommand(binaryFile.absolutePath, config, port)
            Log.d(TAG, "Starting custom core with command: $command")
            
            currentProcess = ProcessBuilder(command)
                .directory(context.filesDir)
                .redirectErrorStream(true)
                .start()
                
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start custom core", e)
            return false
        }
    }

    fun stopCustomCore() {
        try {
            currentProcess?.destroy()
            currentProcess = null
            Log.d(TAG, "Custom core stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop custom core", e)
        }
    }

    private fun getBinaryName(configType: EConfigType): String? {
        return when (configType) {
            EConfigType.AMNEZIAWG -> "amneziawg-go"
            EConfigType.DNSTT -> "dnstt-client"
            EConfigType.SLIPSTREAM -> "slipstream"
            EConfigType.SUSH_MODE -> "sushmode"
            EConfigType.TUIC -> "tuic-client"
            else -> null
        }
    }

    private fun buildCommand(binaryPath: String, config: ProfileItem, port: Int): List<String> {
        val cmd = mutableListOf(binaryPath)
        
        when (config.configType) {
            EConfigType.AMNEZIAWG -> {
                // Example wrapper arguments, will depend on the exact binary syntax
                cmd.add("-bind")
                cmd.add("127.0.0.1:$port")
                // Normally you'd generate a temporary .conf file for amneziawg and pass it
                // We're stubbing this logic based on standard implementation practices
            }
            EConfigType.DNSTT -> {
                // dnstt-client -udp 127.0.0.1:$port -pubkey <pubkey> <resolver>
                cmd.add("-udp")
                cmd.add("127.0.0.1:$port")
                cmd.add("-pubkey")
                cmd.add(config.dnsPubKey.orEmpty())
                cmd.add(config.dnsResolver.orEmpty())
            }
            EConfigType.SLIPSTREAM -> {
                cmd.add("-local")
                cmd.add("127.0.0.1:$port")
                cmd.add("-server")
                cmd.add("${config.server}:${config.serverPort}")
                cmd.add("-id")
                cmd.add(config.password.orEmpty())
            }
            EConfigType.SUSH_MODE -> {
                cmd.add("-bind")
                cmd.add("127.0.0.1:$port")
                cmd.add("-key")
                cmd.add(config.publicKey.orEmpty())
            }
            EConfigType.TUIC -> {
                // Example tuic-client args
                // tuic-client -c config.json or pass via CLI params. We use CLI if supported, or assume a wrapper.
                cmd.add("-s")
                cmd.add("${config.server}:${config.serverPort}")
                cmd.add("-p")
                cmd.add(config.password.orEmpty())
                cmd.add("-u")
                cmd.add(config.username.orEmpty())
                cmd.add("-b")
                cmd.add("127.0.0.1:$port")
                cmd.add("--alpn")
                cmd.add(config.alpn.orEmpty())
            }
            else -> {}
        }
        return cmd
    }
}
