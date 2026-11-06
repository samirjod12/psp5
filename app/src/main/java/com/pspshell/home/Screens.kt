package com.pspshell.home

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.focusable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

private enum class Phase { Boot, Home, ShuttingDown, Sleeping }

private val Ink = Color(0xFF05070D)
private val Accent = Color(0xFF3D8BFF)

@Composable
fun ShellApp(activity: Activity) {
    var phase by remember { mutableStateOf(Phase.Boot) }
    Box(Modifier.fillMaxSize().background(Ink)) {
        Crossfade(targetState = phase, animationSpec = tween(700), label = "phase") { p ->
            when (p) {
                Phase.Boot -> BootScreen(onDone = { phase = Phase.Home })
                Phase.Home -> HomeScreen(
                    activity = activity,
                    onSleep = { phase = Phase.Sleeping },
                    onRestart = { phase = Phase.Boot },
                    onShutdown = { phase = Phase.ShuttingDown }
                )
                Phase.ShuttingDown -> ShutdownScreen(onDone = { activity.finishAndRemoveTask() })
                Phase.Sleeping -> Box(
                    Modifier.fillMaxSize().background(Color.Black)
                        .clickable { phase = Phase.Home }
                )
            }
        }
    }
}

/* ---------------------------------------------------------------- Boot */

@Composable
private fun BootScreen(onDone: () -> Unit) {
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fade.animateTo(1f, tween(1400))
        delay(1600)
        fade.animateTo(0f, tween(600))
        onDone()
    }
    val t = rememberInfiniteTransition(label = "boot")
    val phase by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )
    Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().alpha(fade.value * 0.9f)) {
            val cx = size.width / 2
            val cy = size.height / 2
            for (i in 0 until 5) {
                val p = (phase + i / 5f) % 1f
                drawCircle(
                    color = Accent.copy(alpha = (1f - p) * 0.35f),
                    radius = p * size.height * 0.9f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            for (k in 0 until 3) {
                val path = Path()
                val amp = size.height * (0.05f + k * 0.02f)
                for (x in 0..size.width.toInt() step 8) {
                    val y = cy + amp * sin((x / size.width) * 6.28f * (1 + k) + phase * 6.28f)
                    if (x == 0) path.moveTo(x.toFloat(), y) else path.lineTo(x.toFloat(), y)
                }
                drawPath(
                    path,
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Accent.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(fade.value)) {
            Text("PSP SHELL", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Light, letterSpacing = 14.sp)
            Spacer(Modifier.height(10.dp))
            Text("powered by PPSSPP", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, letterSpacing = 3.sp)
        }
    }
}

/* ------------------------------------------------------------ Shutdown */

@Composable
private fun ShutdownScreen(onDone: () -> Unit) {
    val a = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        delay(900)
        a.animateTo(0f, tween(1100))
        delay(300)
        onDone()
    }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text(
            "Powering off…", color = Color.White.copy(alpha = a.value),
            fontSize = 20.sp, fontWeight = FontWeight.Light, letterSpacing = 4.sp
        )
    }
}

/* ---------------------------------------------------------------- Home */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScreen(
    activity: Activity,
    onSleep: () -> Unit,
    onRestart: () -> Unit,
    onShutdown: () -> Unit
) {
    val ctx = LocalContext.current
    var games by remember { mutableStateOf(GameStore.load(ctx)) }
    var selected by remember { mutableStateOf(0) } // 0 = "add game" tile
    var tab by remember { mutableStateOf("Games") }
    var powerMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focus = remember { FocusRequester() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            games = GameStore.add(ctx, uri)
            selected = (games.indexOfFirst { it.uri == uri.toString() } + 1).coerceAtLeast(1)
        }
    }
    fun addGame() = picker.launch(arrayOf("*/*"))
    fun play(g: Game) {
        if (!launchInPpsspp(ctx, g)) {
            Toast.makeText(ctx, "PPSSPP is not installed — opening download page", Toast.LENGTH_LONG).show()
            openPpssppDownload(ctx)
        }
    }

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(selected) { listState.animateScrollToItem(selected.coerceAtLeast(0)) }

    val current = games.getOrNull(selected - 1)

    Box(
        Modifier.fillMaxSize()
            .focusRequester(focus).focusable()
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (e.key) {
                    Key.DirectionLeft -> { selected = (selected - 1).coerceAtLeast(0); true }
                    Key.DirectionRight -> { selected = (selected + 1).coerceAtMost(games.size); true }
                    Key.Enter, Key.ButtonA, Key.DirectionCenter -> {
                        if (current != null) play(current) else addGame(); true
                    }
                    Key.ButtonStart -> { powerMenu = !powerMenu; true }
                    else -> false
                }
            }
    ) {
        HeroBackdrop(seed = current?.title ?: "home")

        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 18.dp)) {
            TopBar(tab, { tab = it }, onSettings = {
                Toast.makeText(ctx, "Settings live inside PPSSPP", Toast.LENGTH_SHORT).show()
            }, onSearch = { addGame() }, onPower = { powerMenu = true })
            Spacer(Modifier.height(18.dp))

            if (tab == "Games") {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(listOf<Game?>(null) + games) { index, g ->
                        val isSel = index == selected
                        val size = if (isSel) 92.dp else 64.dp
                        Tile(
                            game = g, size = size, selected = isSel,
                            onClick = { selected = index; if (index == 0) addGame() },
                            onLongClick = { if (g != null) games = GameStore.remove(ctx, g).also { selected = 0 } }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    current?.let { "PSP   ${it.title}" } ?: "Add a game",
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                Column(Modifier.padding(bottom = 6.dp)) {
                    Text(
                        (current?.title ?: "PSP SHELL").uppercase(),
                        color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp, maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (current != null) "Press play to launch in PPSSPP · long-press tile to remove"
                        else "Pick an .iso / .cso / .pbp file to add it to your library",
                        color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    PlayButton(if (current != null) "Play Game" else "Add Game") {
                        if (current != null) play(current) else addGame()
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Media — nothing here yet", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                }
            }
        }

        AnimatedVisibility(powerMenu, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
            PowerMenu(
                onDismiss = { powerMenu = false },
                onSleep = { powerMenu = false; onSleep() },
                onRestart = { powerMenu = false; onRestart() },
                onShutdown = { powerMenu = false; onShutdown() }
            )
        }
    }
}

@Composable
private fun HeroBackdrop(seed: String) {
    val h = abs(seed.hashCode())
    val hue1 = (h % 360).toFloat()
    val top = Color.hsv(hue1, 0.45f, 0.28f)
    val mid = Color.hsv((hue1 + 30f) % 360f, 0.5f, 0.45f)
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(top, mid, Ink)))
        // layered original mountain silhouettes
        for (layer in 0 until 3) {
            val path = Path()
            val base = size.height * (0.55f + layer * 0.1f)
            path.moveTo(0f, size.height)
            path.lineTo(0f, base)
            var x = 0f
            var i = 0
            while (x <= size.width) {
                val peak = sin((h % 97) + i * 1.7f + layer) * size.height * 0.1f
                path.lineTo(x, base - abs(peak))
                x += size.width / 9f
                i++
            }
            path.lineTo(size.width, size.height)
            path.close()
            drawPath(path, Color.Black.copy(alpha = 0.25f + layer * 0.2f))
        }
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Ink.copy(alpha = 0.85f)), startY = size.height * 0.45f))
        drawRect(Brush.horizontalGradient(listOf(Ink.copy(alpha = 0.6f), Color.Transparent), endX = size.width * 0.6f))
    }
}

@Composable
private fun TopBar(
    tab: String, onTab: (String) -> Unit,
    onSettings: () -> Unit, onSearch: () -> Unit, onPower: () -> Unit
) {
    var now by remember { mutableStateOf(clock()) }
    LaunchedEffect(Unit) { while (true) { now = clock(); delay(15_000) } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        listOf("Games", "Media").forEach { name ->
            val on = name == tab
            Text(
                name,
                color = if (on) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = if (on) 20.sp else 18.sp,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(end = 26.dp).clickable { onTab(name) }
            )
        }
        Spacer(Modifier.weight(1f))
        TopIcon(Icons.Filled.Search, "Add game", onSearch)
        TopIcon(Icons.Filled.Settings, "Settings", onSettings)
        TopIcon(Icons.Filled.PowerSettingsNew, "Power", onPower)
        Text(now, color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(start = 14.dp))
    }
}

private fun clock(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

@Composable
private fun TopIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.padding(start = 6.dp).size(40.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = Color.White, modifier = Modifier.size(22.dp)) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tile(game: Game?, size: androidx.compose.ui.unit.Dp, selected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (game == null) Brush.linearGradient(listOf(Color(0xFF2A2F3C), Color(0xFF171A22)))
    else {
        val hue = (abs(game.title.hashCode()) % 360).toFloat()
        Brush.linearGradient(listOf(Color.hsv(hue, 0.6f, 0.75f), Color.hsv((hue + 50f) % 360f, 0.7f, 0.4f)))
    }
    Box(
        Modifier.size(size).clip(shape).background(bg)
            .then(if (selected) Modifier.border(2.dp, Color.White, shape) else Modifier)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        if (game == null) Icon(Icons.Filled.Add, "Add", tint = Color.White, modifier = Modifier.size(28.dp))
        else Text(
            game.title.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold,
            fontSize = if (selected) 28.sp else 20.sp, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlayButton(label: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick).padding(horizontal = 26.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.PlayArrow, null, tint = Ink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

/* ---------------------------------------------------------- Power menu */

@Composable
private fun PowerMenu(onDismiss: () -> Unit, onSleep: () -> Unit, onRestart: () -> Unit, onShutdown: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable(onClick = onDismiss)
    ) {
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 36.dp).width(260.dp)
                .clip(RoundedCornerShape(18.dp)).background(Color(0xEE12151D))
                .clickable(enabled = false) {}
                .padding(vertical = 10.dp)
        ) {
            Text("Power", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            PowerItem("Rest Mode", onSleep)
            PowerItem("Restart", onRestart)
            PowerItem("Turn Off", onShutdown)
        }
    }
}

@Composable
private fun PowerItem(label: String, onClick: () -> Unit) {
    Text(
        label, color = Color.White, fontSize = 17.sp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    )
}
