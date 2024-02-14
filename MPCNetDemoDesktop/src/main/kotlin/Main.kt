import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import common.FileUtil.getDefaultSavePath
import common.FileUtil.saveFile
import ui.MainLayoutHandler
import ui.MainLayout
import viewmodel.MainViewModel
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager


class App {
    private val savePath
        get() = viewModel.savePath.value

    private val viewModel: MainViewModel = MainViewModel().apply {
        setSavePath(getDefaultSavePath())
    }

    fun main() = application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "StarGate",
            state = WindowState(width = 600.dp, height = 300.dp),
            resizable = false
        ) {
            MainLayout(viewModel, layoutHandler)
        }

        val server = Server().apply {
            start {
                println("File received: ${it.name} (${it.content.size} bytes)")
                saveFile(it, savePath)
                openSavePath()
            }
        }

        runCatching {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        }
    }

    private fun openSavePath() {
        Desktop.getDesktop().open(File(savePath))
    }

    fun chooseSaveLocation() {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY // 폴더만 선택 가능하도록 설정
            isAcceptAllFileFilterUsed = false // 모든 파일 필터 사용 안함
        }

        val result = fileChooser.showSaveDialog(null)

        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            viewModel.setSavePath(selectedFile.absolutePath)
        }
    }

    private val layoutHandler = object : MainLayoutHandler {
        override fun onClickChangePath() {
            chooseSaveLocation()
        }

        override fun onClickSend() {
            val fileChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.FILES_ONLY // 폴더만 선택 가능하도록 설정
                isAcceptAllFileFilterUsed = false // 모든 파일 필터 사용 안함
            }

            val result = fileChooser.showSaveDialog(null)
        }
    }
}

fun main() {
    App().main()
}