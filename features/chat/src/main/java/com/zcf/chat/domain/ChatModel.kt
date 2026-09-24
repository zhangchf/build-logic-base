package com.zcf.chat.domain

/** The model ids the API accepts. */
enum class ChatModel(val id: String) {
    FLASH("deepseek-flash"),
    PRO("deepseek-v4-pro")
}
