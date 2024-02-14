package viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel {
    private var mutableSavePath = MutableStateFlow("")

    val savePath = mutableSavePath

    fun setSavePath(path: String) {
        CoroutineScope(Dispatchers.Main).launch {
            mutableSavePath.emit(path)
        }
    }
}