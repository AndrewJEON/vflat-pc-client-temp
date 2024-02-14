package com.sanop.mpcnet.data

enum class Command {
    FILE,
    PING,
    PONG,
    CLOSE;

    fun toCommandString() = when (this) {
        FILE -> "@FILE"
        PING -> "@PING"
        PONG -> "@PONG"
        CLOSE -> "@CLOSE"
    }

    companion object {
        fun fromCommandString(string: String) = when (string) {
            "@FILE" -> FILE
            "@PING" -> PING
            "@PONG" -> PONG
            "@CLOSE" -> CLOSE
            else -> null
        }
    }
}
