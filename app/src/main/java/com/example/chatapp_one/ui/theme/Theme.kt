package com.example.chatapp_one.ui.theme

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorPalette = darkColors(
    primary = Color(0xFF81D4FA),
    primaryVariant = PurpleGrey80,
    secondary = Color(0xFF0A192F),
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF81D4FA),
    primaryVariant = PurpleGrey40,
    secondary = Color(0xFF81D4FA),

    background = Color(0xFFF5F5F5),      // soft light grey
    surface = Color(0xFFFFFFFF),         // still white (for cards, dialogs)
    onBackground = Color(0xFF212121),    // dark grey for readability
    onSurface = Color(0xFF333333),       // softer than pure black

    onPrimary = Color.White,
    onSecondary = Color.White
)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun ChatApp_OneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detect system dark mode
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
