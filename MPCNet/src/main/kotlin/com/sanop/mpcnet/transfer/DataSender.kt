package com.sanop.mpcnet.transfer

import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.streams.*
import java.io.File

@OptIn(InternalAPI::class)
suspend fun sendFile(file: File, url: String, port: Int) {
    val client = HttpClient {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    val response: HttpResponse = client.post("http://${url}:${port}/upload") {
        body = MultiPartFormDataContent(formData {
            appendInput(
                key = "image",
                headers = Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                },
                size = file.length()
            ) {
                file.inputStream().asInput()
            }
        })
    }

    println("Server responded: ${response.bodyAsText()}")
    client.close()
}
