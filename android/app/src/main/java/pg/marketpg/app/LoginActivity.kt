package pg.marketpg.app

import android.content.res.Configuration
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AuthLightColors = lightColorScheme(
    primary = Color(0xFF2563EB), onPrimary = Color.White,
    background = Color(0xFFF7F8FA), onBackground = Color(0xFF111827),
    surface = Color.White, onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEFF1F5), onSurfaceVariant = Color(0xFF64748B),
    outline = Color.Transparent
)
private val AuthDarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA), onPrimary = Color(0xFF071A33),
    background = Color(0xFF000000), onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF171717), onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF242424), onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color.Transparent
)

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val pageColor = if (dark) android.graphics.Color.BLACK else android.graphics.Color.rgb(247, 248, 250)
        window.statusBarColor = pageColor
        window.navigationBarColor = pageColor
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if (dark) 0 else (
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            )

        setContent {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val colors = if (isDark) AuthDarkColors else AuthLightColors
            var register by remember { mutableStateOf(false) }
            var name by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirm by remember { mutableStateOf("") }
            var accountType by remember { mutableStateOf("buyer") }
            var passwordVisible by remember { mutableStateOf(false) }
            var confirmVisible by remember { mutableStateOf(false) }
            var busy by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            MaterialTheme(colorScheme = colors) {
                Scaffold(containerColor = colors.background) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MarketPG", style = MaterialTheme.typography.headlineMedium, color = colors.primary)
                        Spacer(Modifier.height(12.dp))
                        Text(if (register) "Create your account" else "Welcome back", style = MaterialTheme.typography.headlineSmall, color = colors.onBackground)
                        Spacer(Modifier.height(4.dp))
                        Text("Buy and sell across Papua New Guinea", color = colors.onSurfaceVariant, fontSize = 16.sp)
                        Spacer(Modifier.height(38.dp))

                        if (register) {
                            AuthInput(name, { name = it; error = null }, "Full name", colors)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (register) {
                            Text("I want to join MarketPG as a", color = colors.onSurface, fontSize = 14.sp, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("buyer" to "Buyer", "seller" to "Seller").forEach { (value, label) ->
                                    Row(
                                        Modifier.weight(1f).background(colors.surfaceVariant, RoundedCornerShape(14.dp)).clickable { accountType = value }.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = accountType == value, onClick = { accountType = value }, colors = RadioButtonDefaults.colors(selectedColor = colors.primary))
                                        Text(label, color = colors.onSurface)
                                    }
                                }
                            }
                            Surface(color = colors.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Use your own, genuine profile photo and cover picture. Fake, misleading, or impersonated images may result in account suspension.",
                                    modifier = Modifier.padding(14.dp), color = colors.onSurface, fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        AuthInput(email, { email = it; error = null }, "Email address", colors)
                        Spacer(Modifier.height(12.dp))
                        AuthInput(
                            value = password,
                            onValueChange = { password = it; error = null },
                            label = "Password",
                            colors = colors,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )
                        if (register) {
                            Spacer(Modifier.height(12.dp))
                            AuthInput(
                                value = confirm,
                                onValueChange = { confirm = it; error = null },
                                label = "Confirm password",
                                colors = colors,
                                passwordVisible = confirmVisible,
                                onTogglePassword = { confirmVisible = !confirmVisible }
                            )
                        }
                        error?.let { Text(it, color = colors.error, modifier = Modifier.padding(top = 12.dp)) }
                        Spacer(Modifier.height(22.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    busy = true
                                    error = null
                                    try {
                                        val result = if (register) {
                                            MarketApi.service.register(RegisterRequest(name.trim(), email.trim(), password, confirm, accountType))
                                        } else {
                                            MarketApi.service.login(LoginRequest(email.trim(), password))
                                        }
                                        MarketApi.saveToken(result.token)
                                        setResult(RESULT_OK)
                                        finish()
                                    } catch (e: Exception) {
                                        error = e.localizedMessage ?: "Unable to sign in. Please try again."
                                    } finally { busy = false }
                                }
                            },
                            enabled = !busy && email.isNotBlank() && password.isNotBlank() &&
                                (!register || (name.isNotBlank() && confirm.isNotBlank())),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (busy) "Please wait..." else if (register) "Create account" else "Sign in", fontSize = 17.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                // OAuth credentials and backend ID-token verification are not configured yet.
                                android.widget.Toast.makeText(
                                    this@LoginActivity,
                                    "Google sign-in setup is required before this option can authenticate.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF292929) else Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            GoogleMark()
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google", fontSize = 17.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        if (!register) {
                            TextButton(onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://marketpg.onrender.com/password/reset")))
                            }) {
                                Text("Forgot password?", fontSize = 14.sp, color = colors.primary)
                            }
                        }
                        TextButton(onClick = { register = !register; error = null }) {
                            Text(if (register) "Sign in" else "Create new account", fontSize = 16.sp, color = colors.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: ColorScheme,
    passwordVisible: Boolean? = null,
    onTogglePassword: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(label, color = colors.onSurfaceVariant, fontSize = 16.sp) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = if (passwordVisible == null || passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = if (passwordVisible != null && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = colors.onSurfaceVariant
                    )
                }
            }
        } else null,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            disabledContainerColor = colors.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = colors.primary,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface
        )
    )
}


@Composable
private fun GoogleMark() {
    Canvas(Modifier.size(22.dp)) {
        val stroke = Stroke(width = size.minDimension * 0.19f, cap = StrokeCap.Butt)
        val inset = size.minDimension * 0.12f
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)
        drawArc(Color(0xFF4285F4), -45f, 90f, false, topLeft, arcSize, style = stroke)
        drawArc(Color(0xFF34A853), 45f, 90f, false, topLeft, arcSize, style = stroke)
        drawArc(Color(0xFFFBBC05), 135f, 90f, false, topLeft, arcSize, style = stroke)
        drawArc(Color(0xFFEA4335), 225f, 90f, false, topLeft, arcSize, style = stroke)
        drawLine(Color(0xFF4285F4), Offset(size.width * 0.52f, size.height * 0.5f), Offset(size.width * 0.94f, size.height * 0.5f), strokeWidth = size.minDimension * 0.19f)
    }
}
