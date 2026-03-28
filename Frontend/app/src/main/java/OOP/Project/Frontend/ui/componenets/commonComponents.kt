package OOP.Project.Frontend.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import OOP.Project.Frontend.ui.theme.AccentRed
import OOP.Project.Frontend.ui.theme.AppTextStyles
import OOP.Project.Frontend.ui.theme.CardBackground
import OOP.Project.Frontend.ui.theme.MutedText

/*
 * CommonComponents.kt contains all reusable UI building blocks
 * shared across multiple screens.
 *
 * WHAT CHANGED FROM THE PREVIOUS VERSION?
 * Every hardcoded fontSize, fontWeight, and lineHeight has been
 * replaced with a reference to AppTextStyles from Type.kt.
 *
 * BEFORE (hardcoded):
 *   Text(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
 *
 * AFTER (centralized):
 *   Text(style = AppTextStyles.buttonText, color = Color.White)
 *
 * WHY IS THIS BETTER?
 * If you want to change the button text size from 18sp to 20sp,
 * you change ONE line in Type.kt and it updates everywhere automatically.
 * With hardcoded values, you'd have to hunt through every file.
 *
 * NOTE ON style vs individual parameters:
 * When you pass style = AppTextStyles.buttonText, the TextStyle object
 * sets fontFamily, fontWeight, fontSize, lineHeight, and letterSpacing
 * all at once. You can still override individual properties AFTER
 * the style parameter — later parameters win over style values.
 *
 * Example:
 *   Text(
 *       style = AppTextStyles.buttonText,  // sets Bold + 18sp
 *       fontWeight = FontWeight.Normal      // overrides just the weight
 *   )
 */

// ════════════════════════════════════════════════════════════════
// PrimaryButton
// ════════════════════════════════════════════════════════════════

/*
 * The main action button used across all screens.
 * Used for: Create Room, Join Game, Start Game, etc.
 *
 * Typography used: AppTextStyles.buttonText
 *   → FontFamily.Default, FontWeight.Bold, 18sp, letterSpacing 0.5sp
 *   Defined in Type.kt under the "Button Text" section.
 *
 * @param text           the label displayed inside the button
 * @param onClick        called when the button is tapped
 * @param modifier       optional layout modifier from the caller
 * @param enabled        false = button is grayed out and non-tappable
 * @param containerColor background color (default AccentRed, override for green Start button)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = AccentRed
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            // disabled state uses the same color at 40% opacity
            disabledContainerColor = containerColor.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            /*
             * AppTextStyles.buttonText applies:
             *   fontFamily   = DefaultFontFamily
             *   fontWeight   = FontWeight.Bold
             *   fontSize     = 18.sp
             *   lineHeight   = 24.sp
             *   letterSpacing = 0.5.sp
             *
             * The 0.5sp letter spacing slightly improves readability
             * for call-to-action button text.
             *
             * color is set separately because TextStyle.color only
             * works when no override is provided — we always want white
             * button text regardless of which containerColor is used.
             */
            style = AppTextStyles.buttonText,
            color = Color.White
        )
    }
}

// ════════════════════════════════════════════════════════════════
// GameTextField
// ════════════════════════════════════════════════════════════════

/*
 * Styled text input field used on HomeScreen, JoinScreen.
 * Wraps OutlinedTextField with app colors and typography pre-applied.
 *
 * Typography used:
 *   - Input text the user types → AppTextStyles.bodyText (16sp Normal)
 *     This is the text the user sees as they type their name or fun fact.
 *
 *   - Floating label above the field → AppTextStyles.secondaryText (14sp Normal)
 *     The label ("Your Name", "Room Code") floats above when focused.
 *
 *   - Placeholder hint text → AppTextStyles.secondaryText (14sp Normal)
 *     Shown when field is empty: "e.g. ABC123"
 *
 * @param value         current string value of the field (from state)
 * @param onValueChange called every time user types — update your state here
 * @param label         floating label string shown above/inside the field
 * @param modifier      optional layout modifier
 * @param placeholder   hint text shown when the field is empty
 * @param minLines      minimum visible lines (use 2 for fun fact field)
 */
@Composable
fun GameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                /*
                 * AppTextStyles.secondaryText applies:
                 *   fontWeight   = FontWeight.Normal
                 *   fontSize     = 14.sp
                 *   lineHeight   = 20.sp
                 *   letterSpacing = 0.25.sp
                 *
                 * Labels are secondary information — smaller and lighter
                 * than the actual input text so they don't compete visually.
                 */
                style = AppTextStyles.secondaryText
            )
        },
        placeholder = if (placeholder.isNotEmpty()) {
            {
                Text(
                    text = placeholder,
                    /*
                     * Placeholder uses the same style as the label
                     * for visual consistency — both are hint/secondary text.
                     * Color is overridden to a dimmer shade to distinguish
                     * placeholder from actual typed input.
                     */
                    style = AppTextStyles.secondaryText,
                    color = Color(0xFF666680) // dim lavender — clearly a hint
                )
            }
        } else null,
        /*
         * textStyle controls how the TEXT THE USER TYPES appears.
         * Using bodyText (16sp Normal) makes it clearly distinct from
         * the smaller label and placeholder text.
         *
         * This is different from the label/placeholder styles above —
         * those are the hint texts, this is the actual user input.
         */
        textStyle = AppTextStyles.bodyText,
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentRed,
            focusedLabelColor = AccentRed,
            cursorColor = AccentRed,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

// ════════════════════════════════════════════════════════════════
// LoadingSpinner
// ════════════════════════════════════════════════════════════════

/*
 * Full-screen loading indicator shown while waiting for API responses.
 * No text is displayed — the spinner alone communicates "loading".
 * No typography changes needed here — CircularProgressIndicator has no text.
 */
@Composable
fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentRed)
    }
}

// ════════════════════════════════════════════════════════════════
// ErrorMessage
// ════════════════════════════════════════════════════════════════

/*
 * Dismissible error card shown when API calls fail.
 *
 * Typography used:
 *   - Error message body → AppTextStyles.errorText (14sp Normal)
 *     Readable against the dark red card background.
 *     14sp is compact enough for the card but readable enough to understand.
 *
 *   - Dismiss button "✕" → AppTextStyles.buttonText (18sp Bold)
 *     Slightly larger to be an easy tap target.
 *
 * @param message   the error string from ViewModel._errorMessage
 * @param onDismiss called when user taps ✕ — should call viewModel.clearError()
 */
@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B0000) // dark red background for errors
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                /*
                 * AppTextStyles.errorText applies:
                 *   fontWeight    = FontWeight.Normal
                 *   fontSize      = 14.sp
                 *   lineHeight    = 20.sp
                 *   letterSpacing = 0.25.sp
                 *
                 * Normal weight is intentional — errors don't need
                 * extra emphasis. The dark red card already signals urgency.
                 * The message text just needs to be clearly readable.
                 */
                style = AppTextStyles.errorText,
                color = Color.White,
                /*
                 * weight(1f) makes the text take all available horizontal
                 * space except what the dismiss button needs.
                 * Without this, a long error message would push ✕ off screen.
                 */
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = "✕",
                    /*
                     * Using buttonText for the dismiss icon makes it
                     * a comfortable tap target (18sp Bold).
                     * Larger than errorText so it stands out as an action.
                     */
                    style = AppTextStyles.buttonText,
                    color = Color.White
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// PlayerCard
// ════════════════════════════════════════════════════════════════

/*
 * Single player row shown in the lobby player list.
 * Displays a checkmark and the player's name.
 *
 * Typography used:
 *   - Player name → AppTextStyles.bodyText (16sp Normal) + Medium weight override
 *
 *   WHY OVERRIDE fontWeight HERE?
 *   AppTextStyles.bodyText is FontWeight.Normal by design —
 *   it is the general reading style used for descriptions.
 *   But inside a card, player names need slightly more presence.
 *   FontWeight.Medium (500) is the middle ground:
 *     Normal (400) → too light for a name in a card
 *     Bold   (700) → too heavy, feels like a heading
 *     Medium (500) → just right for a name in a list
 *
 *   This is a valid use of style override — we want the size/spacing
 *   from bodyText but a slightly heavier weight for this context.
 *
 * @param playerName the name to display in the card
 */
@Composable
fun PlayerCard(playerName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅  ",
                /*
                 * AppTextStyles.bodyText applies the base size (16sp).
                 * The checkmark emoji doesn't need special styling —
                 * it just needs to match the name text height.
                 */
                style = AppTextStyles.bodyText
            )
            Text(
                text = playerName,
                /*
                 * AppTextStyles.bodyText sets fontSize = 16.sp,
                 * lineHeight = 24.sp, letterSpacing = 0.15.sp.
                 *
                 * fontWeight = FontWeight.Medium overrides just the weight
                 * from the style (which is FontWeight.Normal).
                 * The override applies on top of the style — all other
                 * style properties remain from AppTextStyles.bodyText.
                 */
                style = AppTextStyles.bodyText,
                fontWeight = FontWeight.Medium, // slightly heavier than Normal for names in cards
                color = Color.White
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// ScoreCard
// ════════════════════════════════════════════════════════════════

/*
 * One player's score row on the leaderboard.
 * Shows medal/rank, player name, and their score.
 *
 * Typography used:
 *   - Medal emoji (🥇🥈🥉) or rank number → AppTextStyles.medalText (24sp Normal)
 *     Large enough to feel celebratory and immediately recognizable.
 *
 *   - Player name → AppTextStyles.playerName (18sp Bold)
 *     Bold and larger than body text — this is the star of the show.
 *     Players should be able to scan the leaderboard and immediately
 *     spot names.
 *
 *   - Score "1,000 pts" → AppTextStyles.scoreDisplay (18sp Bold)
 *     Same size as playerName so name and score have equal visual weight.
 *     Both sit on the same row — visual balance is important here.
 *
 * @param rank   1-based position (1 = first, 2 = second, etc.)
 * @param name   the player's display name
 * @param score  their total score in points
 */
@Composable
fun ScoreCard(
    rank: Int,
    name: String,
    score: Int
) {
    /*
     * Kotlin when expression returns a value based on rank.
     * rank 1 → 🥇, rank 2 → 🥈, rank 3 → 🥉, rank 4+ → "4." etc.
     *
     * "$rank." uses string template — inserts rank's value.
     * For rank = 4, this produces "4."
     */
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$rank."
    }

    // gold tint background for first place, standard dark card for others
    val cardColor = if (rank == 1) Color(0xFF2D2A00) else CardBackground

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: medal + player name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = medal,
                    /*
                     * AppTextStyles.medalText applies:
                     *   fontWeight   = FontWeight.Normal
                     *   fontSize     = 24.sp
                     *   lineHeight   = 32.sp
                     *   letterSpacing = 0.sp
                     *
                     * 24sp makes the medal/rank clearly visible and
                     * celebratory for the top 3 positions.
                     * Normal weight is fine — emoji rendering handles
                     * its own visual weight.
                     */
                    style = AppTextStyles.medalText
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = name,
                    /*
                     * AppTextStyles.playerName applies:
                     *   fontWeight   = FontWeight.Bold
                     *   fontSize     = 18.sp
                     *   lineHeight   = 24.sp
                     *   letterSpacing = 0.sp
                     *
                     * Bold at 18sp makes names prominent on the leaderboard.
                     * Players scan for their own name — it needs to stand out.
                     */
                    style = AppTextStyles.playerName,
                    color = Color.White
                )
            }

            // Right side: score
            Text(
                text = "$score pts",
                /*
                 * AppTextStyles.scoreDisplay applies:
                 *   fontWeight   = FontWeight.Bold
                 *   fontSize     = 18.sp
                 *   lineHeight   = 24.sp
                 *   letterSpacing = 0.sp
                 *
                 * Same size and weight as playerName (both 18sp Bold).
                 * This creates visual balance — name on the left and
                 * score on the right have equal visual prominence.
                 * The AccentRed color differentiates the score from the name
                 * without needing different text sizes.
                 */
                style = AppTextStyles.scoreDisplay,
                color = AccentRed
            )
        }
    }
}