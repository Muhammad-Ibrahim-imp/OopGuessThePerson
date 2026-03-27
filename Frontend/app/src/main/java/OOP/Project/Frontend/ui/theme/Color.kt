package OOP.Project.Frontend.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * All colors for the app defined in ONE place.
 *
 * WHY CENTRALIZE COLORS?
 * If you hardcode Color(0xFF1A1A2E) in 5 different files and then
 * decide to change the background color, you'd have to find and
 * change all 5 places. With centralized colors, you change ONE line.
 *
 * HOW COLORS WORK IN COMPOSE:
 * Color(0xFFRRGGBB) where:
 *   FF = alpha (opacity) — FF means fully opaque
 *   RR = red component   (00-FF)
 *   GG = green component (00-FF)
 *   BB = blue component  (00-FF)
 *
 * Example: Color(0xFFE94560)
 *   Alpha = FF (fully opaque)
 *   Red   = E9 (high red — makes it pink/red)
 *   Green = 45 (some green)
 *   Blue  = 60 (some blue)
 *   Result: a vibrant pink-red color
 */

// ── Background Colors ─────────────────────────────────────────
// Main screen background — very dark navy blue
val NavyBackground = Color(0xFF1A1A2E)

// Card surfaces — slightly lighter than the background
// Used for question cards, player cards, score cards
val CardBackground = Color(0xFF16213E)

// ── Text Colors ───────────────────────────────────────────────
// Secondary/hint text — muted lavender gray
// Used for subtitles, placeholders, helper text
val MutedText = Color(0xFFB0B0C0)

// ── Accent Colors ─────────────────────────────────────────────
// Primary brand color — vibrant pink-red
// Used for buttons, highlights, the room code display
val AccentRed = Color(0xFFE94560)

// Success/positive color — bright green
// Used for Start Game button and correct answer feedback
val GreenCorrect = Color(0xFF05C46B)

// Trophy/achievement color — classic gold
// Used for the leaderboard trophy emoji and title
val GoldColor = Color(0xFFFFD700)

// ── Answer Button Colors (Kahoot style 2x2 grid) ──────────────
// Each of the 4 answer buttons gets a distinct color
// so players can quickly identify their choice (even color-blind players
// still have the shape icons ▲ ● ■ ★ to differentiate)
val OptionRed    = Color(0xFFE94560) // top-left button    ▲
val OptionBlue   = Color(0xFF0F3460) // top-right button   ●
val OptionPurple = Color(0xFF533483) // bottom-left button ■
val OptionGreen  = Color(0xFF05C46B) // bottom-right button ★