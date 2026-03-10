package com.v2ray.ang.fmt

import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.util.Utils
import java.net.URI

object SushModeFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.SUSH_MODE)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()
        config.publicKey = queryParam["pubkey"].orEmpty()

        return config
    }

    fun toUri(config: ProfileItem): String {
        val dicQuery = HashMap<String, String>()
        dicQuery["pubkey"] = config.publicKey.orEmpty()
        return toUri(config, "", dicQuery)
    }
}
