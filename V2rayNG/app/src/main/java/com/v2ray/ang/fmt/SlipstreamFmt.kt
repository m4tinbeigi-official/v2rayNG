package com.v2ray.ang.fmt

import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.util.Utils
import java.net.URI

object SlipstreamFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.SLIPSTREAM)

        val uri = URI(Utils.fixIllegalUrl(str))

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.password = uri.userInfo.orEmpty() // Using password for Identifier in config model

        return config
    }

    fun toUri(config: ProfileItem): String {
        return toUri(config, config.password.orEmpty(), null)
    }
}
