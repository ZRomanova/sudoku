package com.zoya.sudoku.ui.constructor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoya.sudoku.ui.components.ScreenHeader

private val tips = listOf(
    "Каждый цвет — это будущий регион из 9 клеток, как в обычном судоку. В решённой головоломке в нём должны стоять все цифры от 1 до 9 без повторов.",
    "Закрашивайте один регион полностью и только потом переходите к следующему. Так игра успевает проверять форму на каждом шаге и вовремя подскажет, если что-то пошло не так.",
    "Клетка стала красной? Значит, этим цветом её точно нельзя закрашивать — дальше собрать судоку не получится. Просто выберите другую клетку.",
    "Не растягивайте регион в длинную полоску через всю доску. Чем компактнее форма — примерно 3 клетки в ширину и 3 в высоту, — тем проще игре найти решение.",
    "Старайтесь не собирать весь регион в одной строке или одном столбце. Немного вразброс, как в обычных квадратах 3×3, обычно работает лучше.",
    "Зашли в тупик? Сотрите ластиком несколько последних клеток региона и попробуйте другую форму, а не пытайтесь протолкнуться дальше.",
    "Не знаете, с чего начать? Возьмите за образец классические квадраты 3×3 и слегка измените их края — так почти всегда получается рабочая раскраска."
)

@Composable
fun ConstructorTipsScreen(onHome: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScreenHeader("Как сделать раскраску", onHome)
            Spacer(Modifier.height(16.dp))
            for (tip in tips) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge)
                    Text(tip, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
