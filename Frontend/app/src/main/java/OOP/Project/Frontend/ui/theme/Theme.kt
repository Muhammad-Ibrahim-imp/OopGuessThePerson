package OOP.Project.Frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Theme.kt defines the app-wide Material3 theme.
 *
 * WHAT IS MATERIAL3?
 * Material3 (Material You) is Google's design system for Android.
 * It provides pre-built components (Button, Card, TextField etc.)
 * that automatically use your color scheme.
 *
 * WHAT IS A COLOR SCHEME?
 * A color scheme maps semantic roles (primary, background, surface)
 * to actual colors. Material3 components use these roles:
 *   - A Button uses "primary" for its background color
 *   - A Screen uses "background" for its backdrop
 *   - A Card uses "surface" for its background
 *
 * By setting these roles once in the theme, ALL components
 * automatically use the right colors without manual configuration.
 *
 * WHY darkColorScheme?
 * Our app uses a dark navy background, so we use a dark color scheme.
 * This also tells Android the app is dark-mode-friendly.
 */
private val AppColorScheme = darkColorScheme(
    /*
     * primary = the main brand color
     * Used by: Buttons (containerColor), Checkboxes, Sliders
     */
    primary = AccentRed,

    /*
     * background = the color behind all content (full screen backdrop)
     * Used by: Scaffold background
     */
    background = NavyBackground,

    /*
     * surface = the color of elevated surfaces like cards and sheets
     * Used by: Card, BottomSheet, Dialog
     */
    surface = CardBackground,

    /*
     * onPrimary = text/icon color ON TOP of primary color
     * When a button background is AccentRed, text on it uses onPrimary
     */
    onPrimary = Color.White,

    /*
     * onBackground = text color on top of the background
     * Used by: default Text composables
     */
    onBackground = Color.White,

    /*
     * onSurface = text/icon color on top of surface color
     * Used by: text inside Cards
     */
    onSurface = Color.White
)

/*
 * FunFactsTheme is a Composable wrapper that applies our theme
 * to everything inside it.
 *
 * HOW TO USE:
 * Wrap your entire app in MainActivity.kt:
 *   FunFactsTheme {
 *       AppNavigation()
 *   }
 *
 * After that, every Material3 component automatically uses our colors.
 *
 * @param content the UI content to apply the theme to
 *                "() -> Unit" means a lambda that takes no parameters
 *                and returns nothing — this is how Compose defines
 *                child composables (@Composable content blocks)
 */
@Composable
fun FunFactsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme, // apply our custom dark color scheme
        typography = AppTypography,
        content = content              // render the child composables inside the theme
    )
}