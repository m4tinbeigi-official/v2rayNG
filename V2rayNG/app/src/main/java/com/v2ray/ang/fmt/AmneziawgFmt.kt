package com.v2ray.ang.fmt

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.extension.nullIfBlank
import com.v2ray.ang.extension.removeWhiteSpace
import com.v2ray.ang.util.Utils
import java.net.URI

object AmneziawgFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.AMNEZIAWG)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.rawQuery.isNullOrEmpty()) return null
        val queryParam = getQueryParam(uri)

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        config.secretKey = uri.userInfo.orEmpty()
        config.localAddress = queryParam["address"] ?: AppConfig.WIREGUARD_LOCAL_ADDRESS_V4
        config.publicKey = queryParam["publickey"].orEmpty()
        config.preSharedKey = queryParam["presharedkey"]?.nullIfBlank()
        config.mtu = Utils.parseInt(queryParam["mtu"] ?: AppConfig.WIREGUARD_LOCAL_MTU)
        config.reserved = queryParam["reserved"] ?: "0,0,0"

        config.AwgJc = queryParam["jc"]
        config.AwgJmin = queryParam["jmin"]
        config.AwgJmax = queryParam["jmax"]
        config.AwgS1 = queryParam["s1"]
        config.AwgS2 = queryParam["s2"]
        config.AwgH1 = queryParam["h1"]
        config.AwgH2 = queryParam["h2"]
        config.AwgH3 = queryParam["h3"]
        config.AwgH4 = queryParam["h4"]

        return config
    }

    fun toUri(config: ProfileItem): String {
        val dicQuery = HashMap<String, String>()

        dicQuery["publickey"] = config.publicKey.orEmpty()
        if (config.reserved != null) {
            dicQuery["reserved"] = config.reserved.removeWhiteSpace().orEmpty()
        }
        dicQuery["address"] = config.localAddress.removeWhiteSpace().orEmpty()
        if (config.mtu != null) {
            dicQuery["mtu"] = config.mtu.toString()
        }
        if (config.preSharedKey != null) {
            dicQuery["presharedkey"] = config.preSharedKey.removeWhiteSpace().orEmpty()
        }
        
        config.AwgJc?.let { dicQuery["jc"] = it }
        config.AwgJmin?.let { dicQuery["jmin"] = it }
        config.AwgJmax?.let { dicQuery["jmax"] = it }
        config.AwgS1?.let { dicQuery["s1"] = it }
        config.AwgS2?.let { dicQuery["s2"] = it }
        config.AwgH1?.let { dicQuery["h1"] = it }
        config.AwgH2?.let { dicQuery["h2"] = it }
        config.AwgH3?.let { dicQuery["h3"] = it }
        config.AwgH4?.let { dicQuery["h4"] = it }

        return toUri(config, config.secretKey, dicQuery)
    }
}
