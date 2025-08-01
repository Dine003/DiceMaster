package com.example.DiceMaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.DiceMaster.ui.theme.DiceGameTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlin.text.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dicegame.R

val GameFont = FontFamily(
    Font(R.font.luckiestguy, FontWeight.Normal)
)
val MouldyCheeseFont = FontFamily(
    Font(R.font.mouldycheese, FontWeight.Normal)
)

private val DiceGameTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GameFont,
        fontSize = 32.sp,
        fontWeight = FontWeight.Normal,
        color = Color.White
    ),
    titleLarge = TextStyle(
        fontFamily = GameFont,
        fontSize = 24.sp,
        color = Color.White
    ),
    bodyLarge = TextStyle(
        fontFamily = GameFont,
        fontSize = 16.sp,
        color = Color.White
    )
)
private val DiceGameColors = lightColorScheme(
    primary = Color(0xFF2F5249),
    secondary = Color(0xFF437057),
    background = Color(0xFF97B067),
    onPrimary = Color(0xFFEEEEEE),
    onBackground = Color(0xFFEEEEEE)
)

// Main game activity
class MainActivity : ComponentActivity() {
    // Main activity that manages game screens and tracks win counts across games
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = DiceGameColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Track current screen and scores
                    var currentScreen by remember { mutableStateOf(Screen.Menu) }
                    var targetScore by remember { mutableStateOf(101) }
                    var humanWins by remember { mutableStateOf(0) }
                    var computerWins by remember { mutableStateOf(0) }
                    // Screen navigation
                    when (currentScreen) {
                        Screen.Menu -> MainScreen(
                            onNewGameClick = { currentScreen = Screen.SetupGame }
                        )
                        Screen.SetupGame -> GameSetupScreen(
                            initialTargetScore = targetScore,
                            onStartGame = { newTargetScore ->
                                targetScore = newTargetScore
                                currentScreen = Screen.Game
                            },
                            onBack = { currentScreen = Screen.Menu }
                        )
                        Screen.Game -> GameScreen(
                            onBackToMenu = { currentScreen = Screen.Menu },
                            targetScore = targetScore,
                            onGameEnd = { humanWon ->
                                if (humanWon) humanWins++ else computerWins++
                            },
                            humanWins = humanWins,
                            computerWins = computerWins
                        )
                    }
                }
            }
        }
    }
}

// Game screens
enum class Screen {
    Menu,      // Initial screen with New Game and About buttons
    SetupGame, // Screen to set target score
    Game       // Main game screen
}

// Initial screen with New Game and About buttons
@Composable
fun MainScreen(onNewGameClick: () -> Unit) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title and Logo Section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dices as Logo
            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.dicelogo),
                    contentDescription = "Dice Logo",
                    modifier = Modifier.size(150.dp)
                )
            }

            // Game Title
            Text(
                text = "Dice Master",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 42.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                fontFamily = GameFont,
                modifier = Modifier.padding(top = 14.dp)
            )

            Text(
                text = "Let's Play!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

        }

        // Buttons Section
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onNewGameClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "New Game",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun GameScreen(
    onBackToMenu: () -> Unit,
    targetScore: Int,
    onGameEnd: (Boolean) -> Unit,
    humanWins: Int,
    computerWins: Int
) {
    // State variables to track game progress
    var humanDice by remember { mutableStateOf(List(5) { 1 }) }
    var computerDice by remember { mutableStateOf(List(5) { 1 }) }
    var humanTotalScore by remember { mutableStateOf(0) }
    var computerTotalScore by remember { mutableStateOf(0) }
    var rollCount by remember { mutableStateOf(0) }
    var attempts by remember { mutableStateOf(0) }
    var selectedDice by remember { mutableStateOf(List(5) { false }) }
    var gameState by remember { mutableStateOf(GameState.Playing) }
    var isTieBreaker by remember { mutableStateOf(false) }
    var showInstructions by remember { mutableStateOf(false) }

    // Add state for computer's selected dice
    var computerSelectedDice by remember { mutableStateOf(List(5) { false }) }

    // Add state for win dialog
    var showWinDialog by remember { mutableStateOf(false) }

    // Add state to track reroll count separately from roll count
    var rerollsRemaining by remember { mutableStateOf(2) }

    // Add state for tie-breaker scores
    var tieBreakerHumanScore by remember { mutableStateOf(0) }
    var tieBreakerComputerScore by remember { mutableStateOf(0) }

    // Function to calculate score from dice
    fun calculateScore(dice: List<Int>) = dice.sum()

    // Function to check win condition
    fun checkWinCondition() {
        if (humanTotalScore >= targetScore || computerTotalScore >= targetScore) {
            if (humanTotalScore == computerTotalScore && attempts == attempts) {
                isTieBreaker = true
                // Reset dice for tie breaker
                humanDice = List(5) { 1 }
                computerDice = List(5) { 1 }
            } else {
                gameState = if (humanTotalScore > computerTotalScore)
                    GameState.HumanWon else GameState.ComputerWon
                showWinDialog = true
                onGameEnd(gameState == GameState.HumanWon)
            }
        }
    }

    // Function to handle computer's random strategy
    fun computerPlay(remainingRolls: Int) {
        var rolls = remainingRolls
        while (rolls > 0) {
            // 70% chance to reroll for computer
            if (rolls < remainingRolls && Math.random() > 0.3) {
                computerSelectedDice = List(5) { Math.random() > 0.5 }
                computerDice = computerDice.mapIndexed { index, value ->
                    if (!computerSelectedDice[index]) (1..6).random() else value
                }
            }
            rolls--
        }
    }

    // Update handleScoring to include computer's strategy
    fun handleScoring() {
        // If human scores early, let computer use remaining rolls
        if (rerollsRemaining > 0) {
            computerPlay(rerollsRemaining)
        }

        humanTotalScore += calculateScore(humanDice)
        computerTotalScore += calculateScore(computerDice)
        attempts++
        rollCount = 0
        rerollsRemaining = 2  // Reset rerolls for next turn
        selectedDice = List(5) { false }
        computerSelectedDice = List(5) { false }

        if (!isTieBreaker) {
            checkWinCondition()
        } else {
            gameState = if (calculateScore(humanDice) > calculateScore(computerDice))
                GameState.HumanWon else GameState.ComputerWon
        }

        // Reset dice for next turn if game continues
        if (gameState == GameState.Playing) {
            humanDice = List(5) { 1 }
            computerDice = List(5) { 1 }
        }
    }

    // Update handleScoring for tie breaker
    fun handleTieBreakerScoring() {
        tieBreakerHumanScore = calculateScore(humanDice)
        tieBreakerComputerScore = calculateScore(computerDice)

        gameState = if (tieBreakerHumanScore > tieBreakerComputerScore)
            GameState.HumanWon else GameState.ComputerWon
        showWinDialog = true
        onGameEnd(gameState == GameState.HumanWon)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Scores section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Win counter and scores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "You: $humanTotalScore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF0A400C)
                        )
                    )
                    Text(
                        text = "Computer: $computerTotalScore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFFC62828)
                        )

                    )
                }
                Text(
                    text = " 🏆H:$humanWins/C:$computerWins🏆",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )


            }
        }

        // Instructions
        if (rollCount in 1..2) {
            Text(
                text = "Tap dice to keep them for the next roll",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Player's dice section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your Dice${if (isTieBreaker) " (Tie Breaker)" else ""}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MouldyCheeseFont,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                humanDice.forEachIndexed { index, value ->
                    DiceImage(
                        value = value,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(
                                enabled = !isTieBreaker && rollCount in 1..2,
                                onClick = {
                                    selectedDice = selectedDice.toMutableList().apply {
                                        this[index] = !this[index]
                                    }
                                }
                            ),
                        selected = selectedDice[index]
                    )
                }
            }
        }

        // Computer's dice section
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Computer's Dice",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MouldyCheeseFont,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                computerDice.forEach { value ->
                    DiceImage(
                        value = value,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Game controls section
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rerolls remaining: $rerollsRemaining",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (isTieBreaker) {
                            // REQUIREMENT: Single throw for tie-breaker (no rerolls)
                            humanDice = List(5) { (1..6).random() }
                            computerDice = List(5) { (1..6).random() }
                            handleTieBreakerScoring()
                        } else {
                            // Normal game throw logic
                            if (rollCount == 0) {
                                // First throw
                                humanDice = List(5) { (1..6).random() }
                                computerDice = List(5) { (1..6).random() }
                                rollCount++
                                showInstructions = true
                            } else if (rerollsRemaining > 0) {
                                // Reroll unselected dice
                                humanDice = humanDice.mapIndexed { index, value ->
                                    if (!selectedDice[index]) (1..6).random() else value
                                }

                                // Handle computer's reroll decision
                                if (Math.random() > 0.3) { // 70% chance to reroll
                                    computerSelectedDice = List(5) { Math.random() > 0.5 }
                                    computerDice = computerDice.mapIndexed { index, value ->
                                        if (!computerSelectedDice[index]) (1..6).random() else value
                                    }
                                }

                                rerollsRemaining--

                                // Auto-score if no rerolls remaining
                                if (rerollsRemaining == 0) {
                                    handleScoring()
                                }
                            }
                        }
                    },
                    enabled = gameState == GameState.Playing &&
                             (isTieBreaker || rollCount == 0 || rerollsRemaining > 0),
                    modifier = Modifier.width(150.dp)
                ) {
                    Text(
                        text = when {
                            isTieBreaker -> "Throw (Tie Breaker)"
                            rollCount == 0 -> "Throw"
                            rerollsRemaining > 0 -> "Reroll Selected"
                            else -> "Throw"
                        },
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { handleScoring() },
                    enabled = gameState == GameState.Playing &&
                             rollCount > 0 && rerollsRemaining > 0 && !isTieBreaker,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text("Score")
                }
            }
        }
    }

    // Win Dialog
    if (showWinDialog) {
        Dialog(onDismissRequest = { /* Dialog can only be dismissed by button */ }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (gameState) {
                            GameState.HumanWon -> "You Win!"
                            GameState.ComputerWon -> "You Lose!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = MouldyCheeseFont
                        ),
                        color = when (gameState) {
                            GameState.HumanWon -> Color.Green
                            GameState.ComputerWon -> Color.Red
                            else -> Color.Unspecified
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Show tie breaker scores if applicable
                    if (tieBreakerHumanScore > 0 || tieBreakerComputerScore > 0) {
                        Text(
                            text = "Tie Breaker Results",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Your Roll: $tieBreakerHumanScore",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Computer Roll: $tieBreakerComputerScore",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Text(
                        text = "Final Scores",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Your Score: $humanTotalScore",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Computer Score: $computerTotalScore",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Attempts: $attempts",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Button(
                        onClick = {
                            showWinDialog = false
                            onBackToMenu()
                        },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("Back to Menu")
                    }
                }
            }
        }
    }
}

enum class GameState {
    Playing, HumanWon, ComputerWon
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Update title to remove back arrow
                Text(
                    text = "About Dice Master",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = MouldyCheeseFont
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Student ID: 20230948",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Name: Tharooshi Dinethma",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = " I confirm that I understand what plagiarism is and have read and\n" +
                            " understood the section on Assessment Offences in the Essential\n" +
                            " Information for Students. The work that I have submitted is\n" +
                            " entirely my own. Any work from other authors is duly referenced\n" +
                            " and acknowledged.",
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun GameSetupScreen(
    initialTargetScore: Int,
    onStartGame: (Int) -> Unit,
    onBack: () -> Unit
) {
    var targetScore by remember { mutableStateOf(initialTargetScore.toString()) }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Center the setup content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Set Target Score",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = MouldyCheeseFont,
                    fontWeight = FontWeight.Bold ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = targetScore,
                onValueChange = {
                    targetScore = it
                    showError = false
                },
                label = { Text("Target Score") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = showError,
                supportingText = if (showError) {
                    { Text("Please enter a valid number greater than 0") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val score = targetScore.toIntOrNull()
                    if (score != null && score > 0) {
                        onStartGame(score)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Start Game")
            }

            Spacer(modifier = Modifier.height(16.dp))

    }
        }
    }


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DiceGameTheme {
        MainScreen(onNewGameClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    DiceGameTheme {
        GameScreen(onBackToMenu = {}, targetScore = 101, onGameEnd = {}, humanWins = 0, computerWins = 0)
    }
}

@Composable
fun DiceImage(
    value: Int,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    rotationDegrees: Float = 0f
) {
    val diceId = when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        else -> R.drawable.dice_1
    }

    Surface(
        modifier = modifier.rotate(rotationDegrees),
        color = if (selected)
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.surface,
        border = if (selected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.tertiary
        ) else null,
        shadowElevation = 4.dp
    ) {
        Icon(
            painter = painterResource(id = diceId),
            contentDescription = "Dice showing $value",
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            tint = Color.Unspecified
        )
    }
}