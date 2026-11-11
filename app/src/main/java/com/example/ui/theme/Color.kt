package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val YasinGreen = Color(0xFF007A55)
val YasinGreenLight = Color(0xFFE8F5E9)
val YasinNavy = Color(0xFF0B3C5D)
val YasinNavyDark = Color(0xFF07263B)
val YasinBlue = Color(0xFF1E88E5)
val YasinBlueLight = Color(0xFFE3F2FD)

// Status Colors
val StatusGreen = Color(0xFF16A34A)
val StatusGreenBg = Color(0xFFDCFCE7)
val StatusYellow = Color(0xFFD97706)
val StatusYellowBg = Color(0xFFFEF3C7)
val StatusRed = Color(0xFFDC2626)
val StatusRedBg = Color(0xFFFEE2E2)

// Theme Surfaces
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF64748B)
val BorderLight = Color(0xFFE2E8F0)

// Soft Accents (Information Boxes)
val SoftPurpleBg = Color(0xFFF3E8FF)
val SoftPurpleText = Color(0xFF6B21A8)
val SoftPinkBg = Color(0xFFFCE7F3)
val SoftPinkText = Color(0xFF9D174D)
val SoftBlueBg = Color(0xFFE0F2FE)
val SoftBlueText = Color(0xFF075985)
val SoftTealBg = Color(0xFFCCFBF1)
val SoftTealText = Color(0xFF115E59)
val SoftGreenBg = Color(0xFFDCFCE7)
val SoftGreenText = Color(0xFF166534)
val SoftOrangeBg = Color(0xFFFFEDD5)
val SoftOrangeText = Color(0xFF9A3412)
val SoftYellowBg = Color(0xFFFEF9C3)
val SoftYellowText = Color(0xFF854D0E)

// Vibrant Gradient Brushes for Dashboard Cards
object DashboardGradients {
    val sales = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0EA5E9)))
    val profit = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
    val products = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
    val stock = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)))
    val lowStock = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFF59E0B)))
    val due = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E)))
    val expenses = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFFB923C)))
    val cash = Brush.horizontalGradient(listOf(Color(0xFF0B3C5D), Color(0xFF1E40AF)))
    val banner = Brush.linearGradient(listOf(Color(0xFF0B3C5D), Color(0xFF07263B), Color(0xFF007A55)))
    val memoHeader = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFD97706), Color(0xFF1E293B)))
    val memoCyanHeader = Brush.horizontalGradient(listOf(Color(0xFF07263B), Color(0xFF0284C7), Color(0xFF0EA5E9)))
    val memoSkyBar = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
    val memoTableHead = Brush.horizontalGradient(listOf(Color(0xFF07263B), Color(0xFF0284C7)))
    val memoOrangeTableHead = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFF991B1B)))
    val memoOrangeWave = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFF59E0B), Color(0xFFFBBF24)))
    val memoOrangeDarkPanel = Color(0xFF0F172A)
    val memoPeachColumn = Color(0xFFFFF7ED)
}
