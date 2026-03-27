package OOP.Project.Frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Type.kt centralizes ALL typography (text styles) for the app.
 *
 * WHAT IS TYPOGRAPHY?
 * Typography is the system of text styles used throughout your app.
 * Instead of writing fontSize = 36.sp, fontWeight = FontWeight.Bold
 * in every screen file, you define named styles once here and
 * reuse them everywhere.
 *
 * WHY CENTRALIZE TYPOGRAPHY?
 *
 * WITHOUT centralized typography:
 *   HomeScreen.kt   → Text(fontSize = 36.sp, fontWeight = FontWeight.Bold)
 *   LobbyScreen.kt  → Text(fontSize = 36.sp, fontWeight = FontWeight.Bold)
 *   JoinScreen.kt   → Text(fontSize = 28.sp, fontWeight = FontWeight.Bold)
 *   If you want to change the title size → update 10+ files
 *
 * WITH centralized typography:
 *   HomeScreen.kt   → Text(style = AppTypography.titleLarge)
 *   LobbyScreen.kt  → Text(style = AppTypography.titleLarge)
 *   JoinScreen.kt   → Text(style = AppTypography.titleMedium)
 *   If you want to change the title size → update ONE line in Type.kt
 *
 * WHAT IS MATERIAL3 TYPOGRAPHY SCALE?
 * Material3 defines a set of named text roles:
 *
 *   Display  → largest text, hero text (rarely used)
 *   Headline → large titles, section headers
 *   Title    → medium titles, screen titles
 *   Body     → regular reading text, descriptions
 *   Label    → small text, captions, button labels
 *
 * Each role has three sizes: Large, Medium, Small.
 * e.g. headlineLarge, headlineMedium, headlineSmall
 *
 * WHAT IS TextStyle?
 * TextStyle is Compose's data class that holds all text configuration:
 *   - fontFamily  → which font to use
 *   - fontWeight  → Bold, Normal, Light etc.
 *   - fontSize    → in sp (scale-independent pixels)
 *   - lineHeight  → spacing between lines
 *   - letterSpacing → spacing between characters
 *
 * WHAT IS sp?
 * sp = Scale-independent Pixels.
 * Like dp (density-independent pixels) but ALSO respects the user's
 * font size preference in Android Settings.
 * If a user sets large text in accessibility settings, sp scales up.
 * Always use sp for text sizes, never dp.
 *
 * WHAT IS FontFamily?
 * FontFamily groups font files of different weights together.
 * e.g. Roboto Regular + Roboto Bold + Roboto Light = one FontFamily.
 * When you use FontWeight.Bold, Android picks the right file automatically.
 *
 * HOW TO USE CUSTOM FONTS:
 * 1. Download font files (.ttf or .otf)
 * 2. Create folder: app/src/main/res/font/
 * 3. Copy font files there (lowercase names only: roboto_regular.ttf)
 * 4. Reference with Font(R.font.roboto_regular)
 *
 * For this guide we use the DEFAULT system font (no custom font files needed).
 * To add a custom font later, follow the steps above and uncomment
 * the custom font section below.
 */

// ════════════════════════════════════════════════════════════════
// FONT FAMILY
// ════════════════════════════════════════════════════════════════

/*
 * DEFAULT FONT SETUP (uses Android's built-in system font)
 *
 * FontFamily.Default uses the system font (Roboto on most Android devices).
 * No font files needed — works out of the box.
 *
 * This is the safe choice for your first project.
 * You can always swap to a custom font later.
 */
val DefaultFontFamily = FontFamily.Default

/*
 * CUSTOM FONT SETUP (uncomment this when you want a custom font)
 *
 * HOW TO ADD A CUSTOM FONT (step by step):
 *
 * Step 1: Choose a font from fonts.google.com
 *         Recommended for a game app: "Nunito" or "Poppins"
 *
 * Step 2: Download the font files (.ttf format)
 *         You need at least: Regular (400) and Bold (700)
 *         Optional: Medium (500), SemiBold (600)
 *
 * Step 3: Create the font resource folder
 *         Right-click app/src/main/res → New → Android Resource Directory
 *         Resource type: font → OK
 *         This creates: app/src/main/res/font/
 *
 * Step 4: Copy your .ttf files into app/src/main/res/font/
 *         IMPORTANT: filenames must be lowercase with underscores only
 *         ✅ nunito_regular.ttf
 *         ❌ Nunito-Regular.ttf  (hyphens and uppercase not allowed)
 *
 * Step 5: Uncomment the code below and update font names to match your files
 *
 * val AppFontFamily = FontFamily(
 *     Font(R.font.nunito_regular, FontWeight.Normal),
 *     Font(R.font.nunito_medium, FontWeight.Medium),
 *     Font(R.font.nunito_bold, FontWeight.Bold)
 * )
 *
 * Step 6: Replace DefaultFontFamily with AppFontFamily in all TextStyles below
 */

// ════════════════════════════════════════════════════════════════
// TEXT STYLES
// Custom named styles for every text element in the app.
// These are additional styles BEYOND Material3's built-in scale.
// ════════════════════════════════════════════════════════════════

/*
 * AppTextStyles is an object (singleton) containing all custom text styles.
 *
 * USAGE IN ANY COMPOSABLE:
 *   Text(
 *       text = "FunFacts Quiz",
 *       style = AppTextStyles.appTitle
 *   )
 *
 * This replaces writing:
 *   Text(
 *       text = "FunFacts Quiz",
 *       fontSize = 36.sp,
 *       fontWeight = FontWeight.Bold,
 *       fontFamily = DefaultFontFamily
 *   )
 *
 * BENEFIT: If you want to change the app title style,
 * you change it in ONE place and it updates everywhere.
 */
object AppTextStyles {

    // ── App Title ─────────────────────────────────────────────
    /*
     * Used for: "🎉 FunFacts Quiz" on the Home screen
     * The largest, most prominent text in the app.
     * Bold to make a strong visual impression.
     *
     * 36.sp = quite large, clearly the headline of the screen
     * FontWeight.Bold = maximum visual weight
     * lineHeight = 44.sp → comfortable reading for a 2-line title
     */
    val appTitle = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp  // no extra spacing for large titles
    )

    // ── Screen Title ──────────────────────────────────────────
    /*
     * Used for: "Join a Room", "Waiting for Players", "Final Scores"
     * Secondary screen titles — smaller than appTitle but still prominent.
     *
     * 28.sp = clearly a title, but subordinate to appTitle
     */
    val screenTitle = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )

    // ── Room Code Display ─────────────────────────────────────
    /*
     * Used for: "ABC123" displayed large in the lobby
     * Needs to be very large and readable — players read this to join.
     *
     * 52.sp = very large, hard to miss on any screen size
     * FontWeight.Bold = maximum weight so each character is clearly visible
     * letterSpacing = 8.sp = wide spacing between characters
     *   "A3F9K2" is much easier to read than "A3F9K2" when spaced out
     */
    val roomCode = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = 8.sp  // wide spacing makes code easy to read and share
    )

    // ── Question Statement ────────────────────────────────────
    /*
     * Used for: the fun fact shown in the question card
     * e.g. "I have visited 30 countries"
     * Must be readable at a glance — players have limited time.
     *
     * 20.sp = large enough to read quickly under time pressure
     * FontWeight.Bold = extra emphasis — this IS the question
     * lineHeight = 30.sp = comfortable reading for potentially 2-3 line facts
     */
    val questionStatement = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // ── Answer Option Label ───────────────────────────────────
    /*
     * Used for: player names on the colored answer buttons
     * Must be readable on colored backgrounds.
     *
     * 14.sp = fits in the button without overflow
     * FontWeight.Bold = stands out against the colored background
     */
    val answerOption = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // ── Button Text ───────────────────────────────────────────
    /*
     * Used for: text inside PrimaryButton (Create Room, Join Game, Start Game)
     * Slightly larger than normal text — buttons should be easy to read.
     *
     * 18.sp = comfortable for a call-to-action button
     * FontWeight.Bold = makes the action feel intentional and clear
     */
    val buttonText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp  // slight spacing improves button text readability
    )

    // ── Player Name (lobby + leaderboard) ────────────────────
    /*
     * Used for: player names in PlayerCard and ScoreCard
     *
     * 16.sp and 18.sp for lobby vs leaderboard respectively.
     * Using one style for both is a reasonable tradeoff.
     * 18.sp is used as the base — leaderboard names should be prominent.
     */
    val playerName = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // ── Score Display ─────────────────────────────────────────
    /*
     * Used for: "1,000 pts" on the leaderboard
     * Same visual weight as playerName — they appear side by side.
     *
     * FontWeight.Bold = score feels important, rewarding
     */
    val scoreDisplay = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // ── Body Text ─────────────────────────────────────────────
    /*
     * Used for: general descriptive text
     * e.g. "Guess who has which fun fact!", "Share this code with your friends"
     *
     * 16.sp = comfortable reading size for descriptive text
     * FontWeight.Normal = regular weight, not a headline
     * lineHeight = 24.sp = good readability for 1-2 line descriptions
     */
    val bodyText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp  // slight spacing improves body text readability
    )

    // ── Secondary / Hint Text ─────────────────────────────────
    /*
     * Used for: MutedText colored labels, helper text, subtitles
     * e.g. "3 players joined", "Waiting for host to start..."
     *
     * 14.sp = clearly secondary — less important than body text
     * FontWeight.Normal = low visual weight matches secondary importance
     */
    val secondaryText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )

    // ── Caption / Label ───────────────────────────────────────
    /*
     * Used for: very small labels, "Question 1 / 3", "🤔 Who said this?"
     * Smallest readable size — used for supplementary information.
     *
     * 12.sp = minimum comfortable size (don't go smaller)
     * FontWeight.Medium = slightly heavier than Normal for readability at small size
     */
    val caption = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp  // more spacing at smaller sizes improves readability
    )

    // ── Progress Indicator Text ───────────────────────────────
    /*
     * Used for: "Question 1 / 3" at the top of QuestionScreen
     * Should be noticeable but not compete with the question itself.
     *
     * 14.sp = small enough to not distract from the question
     */
    val progressText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // ── Medal / Rank Text ─────────────────────────────────────
    /*
     * Used for: 🥇🥈🥉 emojis and "4." rank numbers on leaderboard
     * Large enough to be visually rewarding for top 3 players.
     *
     * 24.sp = large emoji display size
     * FontWeight.Normal = emoji weight is handled by the emoji renderer
     */
    val medalText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    // ── Leaderboard Title ─────────────────────────────────────
    /*
     * Used for: "🏆 Final Scores" at the top of LeaderboardScreen
     * Big and celebratory — game is over, this is the moment of truth.
     *
     * 32.sp = very large, commanding attention
     * FontWeight.Bold = maximum celebration weight
     */
    val leaderboardTitle = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )

    // ── Answer Button Icon ────────────────────────────────────
    /*
     * Used for: ▲ ● ■ ★ shape icons on answer buttons
     * Large enough to be immediately recognizable.
     *
     * 20.sp = clearly visible inside the button above the name
     */
    val answerIcon = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // ── Error Message ─────────────────────────────────────────
    /*
     * Used for: text inside ErrorMessage component
     * Must be readable against a dark red background.
     *
     * 14.sp = fits within a compact error card
     * FontWeight.Normal = errors don't need extra weight
     */
    val errorText = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
}

// ════════════════════════════════════════════════════════════════
// MATERIAL3 TYPOGRAPHY SCALE
// Maps our custom styles to Material3's semantic typography roles.
// Material3 components (Button, TextField, Card) use these roles
// to pick the right text style automatically.
// ════════════════════════════════════════════════════════════════

/*
 * AppTypography is the Material3 Typography object used in Theme.kt.
 *
 * HOW MATERIAL3 USES THIS:
 * MaterialTheme(typography = AppTypography) in Theme.kt registers these styles.
 * Material3 components then pick styles based on their role:
 *   - Button uses labelLarge for its text
 *   - TextField uses bodyLarge for input text and bodySmall for the label
 *   - Card uses bodyMedium for its content by default
 *
 * You don't HAVE to use all slots — unmapped ones keep Material3 defaults.
 *
 * MAPPING STRATEGY:
 * We map our game-specific styles to the closest Material3 semantic role.
 * This ensures Material3 components look consistent with our custom text.
 */
val AppTypography = Typography(

    // ── Display ───────────────────────────────────────────────
    /*
     * displayLarge = largest possible text — hero text on splash screens
     * We use roomCode style here (52sp) — the room code IS our hero moment
     * in the lobby.
     */
    displayLarge = AppTextStyles.roomCode,

    /*
     * displayMedium = large display text
     * Mapped to appTitle (36sp) — used for "🎉 FunFacts Quiz"
     */
    displayMedium = AppTextStyles.appTitle,

    /*
     * displaySmall = smaller display text
     * Mapped to leaderboardTitle (32sp) — "🏆 Final Scores"
     */
    displaySmall = AppTextStyles.leaderboardTitle,

    // ── Headline ──────────────────────────────────────────────
    /*
     * headlineLarge = prominent section heading
     * Mapped to screenTitle (28sp) — "Join a Room", "Waiting for Players"
     */
    headlineLarge = AppTextStyles.screenTitle,

    /*
     * headlineMedium = medium section heading
     * Mapped to questionStatement (20sp) — the fun fact shown in question card
     */
    headlineMedium = AppTextStyles.questionStatement,

    /*
     * headlineSmall = smaller heading
     * Mapped to playerName (18sp) — player names on leaderboard
     */
    headlineSmall = AppTextStyles.playerName,

    // ── Title ─────────────────────────────────────────────────
    /*
     * titleLarge = prominent content title
     * Mapped to scoreDisplay (18sp) — "1,000 pts" score display
     */
    titleLarge = AppTextStyles.scoreDisplay,

    /*
     * titleMedium = medium content title
     * Mapped to buttonText (18sp) — text inside PrimaryButton
     * Material3 Button uses titleMedium / labelLarge for its text
     */
    titleMedium = AppTextStyles.buttonText,

    /*
     * titleSmall = smaller content title
     * Mapped to answerOption (14sp) — player names on answer buttons
     */
    titleSmall = AppTextStyles.answerOption,

    // ── Body ──────────────────────────────────────────────────
    /*
     * bodyLarge = primary reading text
     * Mapped to bodyText (16sp) — descriptions, subtitles
     * Also used by TextField for the input text the user types
     */
    bodyLarge = AppTextStyles.bodyText,

    /*
     * bodyMedium = secondary reading text
     * Mapped to secondaryText (14sp) — helper text, muted labels
     */
    bodyMedium = AppTextStyles.secondaryText,

    /*
     * bodySmall = small reading text
     * Mapped to progressText (14sp) — "Question 1 / 3" progress indicator
     */
    bodySmall = AppTextStyles.progressText,

    // ── Label ─────────────────────────────────────────────────
    /*
     * labelLarge = large label / button text
     * Mapped to buttonText (18sp)
     * Material3 Button specifically uses labelLarge for its text content
     */
    labelLarge = AppTextStyles.buttonText,

    /*
     * labelMedium = medium label
     * Mapped to caption (12sp) — "🤔 Who said this?", small labels
     */
    labelMedium = AppTextStyles.caption,

    /*
     * labelSmall = smallest label
     * Mapped to errorText (14sp) — error messages in ErrorMessage component
     */
    labelSmall = AppTextStyles.errorText
)