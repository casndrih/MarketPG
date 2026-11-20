package pg.marketpg.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var register by remember { mutableStateOf(false) }
            var name by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirm by remember { mutableStateOf("") }
            var busy by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()
            MaterialTheme {
                Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                    Column(
                        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MarketPG", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(if (register) "Create your account" else "Welcome back", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Buy and sell across Papua New Guinea", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(28.dp))
                        if (register) OutlinedTextField(name, { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(email, { email = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Email address") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(password, { password = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        if (register) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(confirm, { confirm = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    busy = true
                                    error = null
                                    try {
                                        val result = if (register) {
                                            MarketApi.service.register(RegisterRequest(name.trim(), email.trim(), password, confirm))
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
                            enabled = !busy && email.isNotBlank() && password.isNotBlank() && (!register || (name.isNotBlank() && confirm.isNotBlank())),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50)
                        ) { Text(if (busy) "Please wait..." else if (register) "Create account" else "Sign in") }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(this@LoginActivity, "Google sign-in needs the MarketPG Google OAuth Web Client ID and server verification configuration.", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("G", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Continue with Google")
                        }
                        TextButton(onClick = { register = !register; error = null }) {
                            Text(if (register) "Already have an account? Sign in" else "New to MarketPG? Create an account")
                        }
                        TextButton(onClick = { finish() }) { Text("Continue browsing") }
                    }
                }
            }
        }
    }
}
