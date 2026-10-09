package com.changfeng.tomato.data

import kotlinx.serialization.json.Json

internal val appJson = Json {
    coerceInputValues = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    useAlternativeNames = true
    allowSpecialFloatingPointValues = true
    prettyPrint = true
}

/** 数据文件统一保留在程序工作目录（见 docs/adr/0001）。 */
internal const val DATA_DIR: String = "."
