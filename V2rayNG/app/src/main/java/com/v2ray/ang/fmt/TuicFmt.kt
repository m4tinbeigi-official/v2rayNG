package com.v2ray.ang.fmt

import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.util.Utils
import java.net.URI

object TuicFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.TUIC)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        val userInfo = uri.userInfo?.split(":", limit = 2)
        if (userInfo != null && userInfo.size == 2) {
            config.username = userInfo[0] // UUID
            config.password = userInfo[1] // Password
        }

        config.sni = queryParam["sni"].orEmpty()
        config.alpn = queryParam["alpn"].orEmpty()
        config.congestion = queryParam["congestion_control"].orEmpty()
        config.udpRelayMode = queryParam["udp_relay_mode"].orEmpty()

        return config
    }

    fun toUri(config: ProfileItem): String {
        val dicQuery = HashMap<String, String>()

        if (!config.sni.isNullOrEmpty()) {
            dicQuery["sni"] = config.sni.orEmpty()
        }
        if (!config.alpn.isNullOrEmpty()) {
            dicQuery["alpn"] = config.alpn.orEmpty()
        }
        if (!config.congestion.isNullOrEmpty()) {
            dicQuery["congestion_control"] = config.congestion.orEmpty()
        }
        if (!config.udpRelayMode.isNullOrEmpty()) {
            dicQuery["udp_relay_mode"] = config.udpRelayMode.orEmpty()
        }

        val auth = if (!config.username.isNullOrEmpty() && !config.password.isNullOrEmpty()) {
            "${config.username}:${config.password}"
        } else {
            ""
        }

        return toUri(config, auth, dicQuery)
    }
}
