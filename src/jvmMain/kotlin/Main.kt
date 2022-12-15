import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.random.Random
import kotlin.random.nextInt

@Composable
fun App(matchHeightConstraintsFirst: Boolean) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val dpSize by remember { derivedStateOf { with(density) {
        DpSize((size.width / 4).toDp(), (size.height / 4).toDp())
    } } }
    val animatedSize = animateDpAsState(min(dpSize.width, dpSize.height))
    val fontSize by remember { derivedStateOf { with(density) {
        (min(dpSize.width, dpSize.height) / 3).toSp()
    } } }

    val field = remember { List(16) { (it+1)%16 }.toMutableStateList().shuffle() }
    val coords by remember { derivedStateOf {
        List(16) { num -> field.indexOf(num).let {
            (it % 4) to (it / 4)
        } }
    } }
    val dpCoords by remember { derivedStateOf {
        coords.map {
            with(density) {
                (it.first * size.width / 4).toDp() to (it.second * size.height / 4).toDp()
            }
        }
    } }
    val animatedCoords = dpCoords.map {
        animateDpAsState(it.first) to animateDpAsState(it.second)
    }

    Box(Modifier.fillMaxSize()
        .aspectRatio(1f, matchHeightConstraintsFirst)
        .padding(8.dp)
        .onGloballyPositioned { size = it.size }
    ) {
        animatedCoords.forEachIndexed { i, (x, y) ->
            if (i>0)
            Card(Modifier.size(animatedSize.value).padding(8.dp).offset(x.value, y.value), RoundedCornerShape(12.dp), Color(0xFFEEFFFF), elevation = 8.dp) {
                Box(Modifier.fillMaxSize().clickable {
                    field.swap(field.indexOf(i))
                }) {
                    Text(i.toString(), Modifier.align(Alignment.Center), Color.Gray, fontSize = fontSize)
                }
            }
        }
    }
}

fun main() = application {
    val state = rememberWindowState(width = 600.dp, height = 623.dp)
    Window(onCloseRequest = ::exitApplication, state = state, title = "Игра 15") {
        MaterialTheme {
            App(state.size.width > state.size.height)
        }
    }
}

private val offsets = listOf(-4, -1, 1, 4)

private fun List<Int>.check(index: Int) =
    offsets.map { index + it }.firstOrNull {
        it in 0..15 && ((index%4 == it%4) xor (index/4 == it/4))  && get(it) == 0
    }

private fun SnapshotStateList<Int>.swap(from: Int) =
    check(from)?.let { to ->
        this[from] = this[to] . also { this[to] = this[from] }
    }

private fun SnapshotStateList<Int>.shuffle(steps: Int = 1000) = apply {
    var index: Int
    repeat(steps) {
        do { index = Random.nextInt(0..15) } while (check(index) == null)
        swap(index)
    }
}
