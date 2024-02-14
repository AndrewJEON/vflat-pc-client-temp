package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodel.MainViewModel

interface MainLayoutHandler {
    fun onClickChangePath()
    fun onClickSend()
}

val font_regular = FontFamily(Font("font/pretendard_regular.otf"))
val font_medium = FontFamily(Font("font/pretendard_medium.otf"))

@Composable
fun MainLayout(viewModel: MainViewModel, handler: MainLayoutHandler) {
    val savePath by viewModel.savePath.collectAsState("")

    var openSavePath by remember { mutableStateOf(true) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(8.dp)
        ) {
            SettingSwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Open save path after download",
                switchStatus = openSavePath,
                onSwitchStatusChanged = { openSavePath = it }
            )
            Spacer(Modifier.height(8.dp))
            SettingItem(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Column() {
                        Text(
                            text = "Save Path",
                            style = TextStyle(
                                fontFamily = font_medium,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = savePath,
                            style = TextStyle(
                                fontFamily = font_regular,
                                fontSize = 14.sp
                            ),
                            color = Color.Gray
                        )
                    }

                },
                selector = {
                    Button(
                        onClick = handler::onClickChangePath
                    ) {
                        Text("Change")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onClick = handler::onClickSend
            ) {
                Text("Send File")
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    modifier: Modifier,
    title: String,
    switchStatus: Boolean,
    onSwitchStatusChanged: (Boolean) -> Unit
) {
    SettingItem(
        modifier = modifier,
        content = {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = font_medium,
                    fontSize = 16.sp
                )
            )
        },
        selector = {
            Switch(
                checked = switchStatus,
                onCheckedChange = {
                    onSwitchStatusChanged(it)
                }
            )
        }
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    selector: @Composable BoxScope.() -> Unit,
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.widthIn(min = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            selector()
        }
    }
}

@Preview
@Composable
fun MainLayoutPreview() {
    MainLayout(MainViewModel().apply { setSavePath("C:/vflat")}, object : MainLayoutHandler {
        override fun onClickChangePath() {
            println("Clicked")
        }

        override fun onClickSend() {

        }
    })
}