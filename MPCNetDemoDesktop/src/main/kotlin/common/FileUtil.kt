package common

import com.sanop.mpcnet.data.FileData
import java.io.File

object FileUtil {
    fun getDefaultSavePath(): String {
        val osName = System.getProperty("os.name").toLowerCase()
        val userHome = System.getProperty("user.home")

        val defaultPath = if (osName.contains("win")) {
            // Windows
            "$userHome\\Documents\\stargate"
        } else if (osName.contains("mac")) {
            // macOS
            "$userHome/Documents/mpcTest"
        } else {
            // Linux and other OS
            "$userHome/mpcTest"
        }

        return defaultPath
    }

    fun saveFile(file: FileData, savePath: String) {
        val folder = File(savePath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        File("$savePath/${file.name}").writeBytes(file.content)
    }
}