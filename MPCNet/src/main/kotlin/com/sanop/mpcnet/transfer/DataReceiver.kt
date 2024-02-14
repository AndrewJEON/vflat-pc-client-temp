package com.sanop.mpcnet.transfer

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.jackson.*
import io.ktor.utils.io.core.use
import java.io.File

fun startServer(port: Int) {
    embeddedServer(Netty, port = port) {
        extracted()
    }.start(wait = true)
}

private fun Application.extracted() {
    install(ContentNegotiation) {
        jackson { }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    routing {
        post("/upload") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val fileBytes = part.streamProvider().use { it.readBytes() }
                        File(part.originalFileName!!).writeBytes(fileBytes)
                        part.dispose()
                    }
                    else -> {}
                }
            }
            call.respond(HttpStatusCode.OK, mapOf("status" to "Successfully uploaded!"))
        }
    }
}
