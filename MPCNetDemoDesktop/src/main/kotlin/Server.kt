import com.sanop.mpcnet.data.FileData
import com.sanop.mpcnet.service.ServiceProvider
import com.sanop.mpcnet.socket.SocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer


class Server {
    fun start(handler: InputFileHandler) {
        CoroutineScope(Dispatchers.IO).launch {
            ServiceProvider.start()

            SocketServer.bindAndAccept("0.0.0.0", 9802) {
                handler.handle(it)
                return@bindAndAccept mapOf("status" to "success")
            }
        }
    }

    fun stop() {
        ServiceProvider.stop()
    }

    fun interface InputFileHandler {
        fun handle(fileData: FileData)
    }
}