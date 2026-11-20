package pg.marketpg.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Lime = Color(0xFFB7F34A)
private val Ink = Color(0xFF26321B)
private val Canvas = Color(0xFFF7F8F4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MarketApi.initialize(applicationContext)
        setContent { MarketPGApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPGApp() {
    var search by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var listings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<ListingDto?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var retry by remember { mutableIntStateOf(0) }
    val snackbarHost = remember { SnackbarHostState() }
    var signedIn by remember { mutableStateOf(MarketApi.hasSession()) }
    var showAuth by remember { mutableStateOf(false) }
    var registerMode by remember { mutableStateOf(false) }
    var authName by remember { mutableStateOf("") }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authConfirm by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { categories = MarketApi.service.categories().data } catch (_: Exception) { }
    }
    LaunchedEffect(search, categoryId, retry) {
        delay(300)
        loading = true
        error = null
        try {
            listings = MarketApi.service.listings(
                query = search.trim().takeIf { it.isNotEmpty() },
                categoryId = categoryId
            ).data
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Unable to connect to MarketPG."
        } finally { loading = false }
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Ink, secondary = Lime, background = Canvas)) {
        Scaffold(
            containerColor = Canvas,
            snackbarHost = { SnackbarHost(snackbarHost) },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    val labels = listOf("Explore", "Saved", "Sell", "Inbox", "Profile")
                    val icons = listOf("⌂", "♡", "+", "✉", "☺")
                    labels.forEachIndexed { i, label ->
                        NavigationBarItem(tab == i, { tab = i; if (i != 0 && !signedIn) { showAuth = true; authError = null } }, icon = { Text(icons[i], fontSize = 20.sp) }, label = { Text(label) })
                    }
                }
            }
        ) { padding ->
            if (tab == 0) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("MarketPG", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
                            Text("Buy & sell across PNG", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("🇵🇬", fontSize = 26.sp)
                    }
                    OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), placeholder = { Text("Search phones, cars, furniture...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp))
                    Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Ink), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("GREAT LOCAL FINDS", color = Lime, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Give good things\na second life.", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Discover real listings from sellers across Papua New Guinea.", color = Color.LightGray)
                        }
                    }
                    Text("Browse categories", Modifier.padding(start = 18.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(categoryId == null, { categoryId = null }, label = { Text("All") }) }
                        items(categories.size) { index ->
                            val category = categories[index]
                            FilterChip(categoryId == category.id, { categoryId = category.id }, label = { Text(category.name) })
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fresh finds", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(listings.size.toString() + " listings", color = Color.Gray, fontSize = 12.sp)
                    }
                    when {
                        loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Ink) }
                        error != null -> Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Couldn't load listings", fontWeight = FontWeight.Bold)
                            Text(error ?: "Network error", color = Color.Gray)
                            Button(onClick = { retry++ }) { Text("Try again") }
                        }
                        listings.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No active listings found. Try another search.", color = Color.Gray) }
                        else -> LazyVerticalGrid(GridCells.Fixed(2), Modifier.weight(1f), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(listings, key = { it.id }) { listing ->
                                Card(Modifier.clickable { selected = listing }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column {
                                        if (listing.imageUrl != null) AsyncImage(listing.imageUrl, listing.title, Modifier.fillMaxWidth().height(118.dp))
                                        else Box(Modifier.fillMaxWidth().height(118.dp).background(Color(0xFFE9EDDF)), contentAlignment = Alignment.Center) { Text("📦", fontSize = 38.sp) }
                                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(listing.title, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("K " + listing.price, color = Color(0xFF547C1D), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text("⌖ " + listing.location, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(padding).padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(listOf("Explore", "Saved items", "Sell on MarketPG", "Messages", "Your profile")[tab], fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    Text(listOf("", "Your saved listings will appear here.", "Seller tools are being connected to your account.", "Buyer and seller conversations will appear here.", "Manage your MarketPG account.")[tab], color = Color.Gray)
                    if (tab == 4 && signedIn) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { MarketApi.clearToken(); signedIn = false; tab = 0 }) { Text("Sign out") }
                    }
                }
            }
        }

        if (showAuth) {
            AlertDialog(
                onDismissRequest = { showAuth = false },
                title = { Text(if (registerMode) "Create your MarketPG account" else "Sign in to MarketPG") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (registerMode) {
                            OutlinedTextField(authName, { authName = it }, label = { Text("Full name") }, singleLine = true)
                        }
                        OutlinedTextField(authEmail, { authEmail = it }, label = { Text("Email address") }, singleLine = true)
                        OutlinedTextField(authPassword, { authPassword = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                        if (registerMode) {
                            OutlinedTextField(authConfirm, { authConfirm = it }, label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                        }
                        authError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        TextButton(onClick = { registerMode = !registerMode; authError = null }) {
                            Text(if (registerMode) "Already registered? Sign in" else "New here? Create an account")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !authBusy,
                        onClick = {
                            scope.launch {
                                authBusy = true
                                authError = null
                                try {
                                    val result = if (registerMode) {
                                        MarketApi.service.register(RegisterRequest(authName.trim(), authEmail.trim(), authPassword, authConfirm))
                                    } else {
                                        MarketApi.service.login(LoginRequest(authEmail.trim(), authPassword))
                                    }
                                    MarketApi.saveToken(result.token)
                                    signedIn = true
                                    showAuth = false
                                    authPassword = ""
                                    authConfirm = ""
                                } catch (e: Exception) {
                                    authError = e.localizedMessage ?: "Authentication failed. Please try again."
                                } finally {
                                    authBusy = false
                                }
                            }
                        }
                    ) { Text(if (authBusy) "Please wait..." else if (registerMode) "Create account" else "Sign in") }
                },
                dismissButton = { TextButton(onClick = { showAuth = false }) { Text("Cancel") } }
            )
        }

        selected?.let { listing ->
            AlertDialog(
                onDismissRequest = { selected = null },
                title = { Text(listing.title, fontWeight = FontWeight.Bold) },
                text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("K " + listing.price, color = Color(0xFF547C1D), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(listing.description ?: "No description supplied.")
                    Text("Location: " + listing.location)
                    listing.condition?.let { Text("Condition: " + it) }
                    listing.user?.name?.let { Text("Seller: " + it) }
                } },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = {
                            if (!signedIn) {
                                showAuth = true
                            } else {
                                scope.launch {
                                    try {
                                        MarketApi.service.addToWishlist(listing.id)
                                        snackbarHost.showSnackbar("Saved to your wishlist.")
                                    } catch (e: Exception) {
                                        snackbarHost.showSnackbar(e.localizedMessage ?: "Could not save listing.")
                                    }
                                }
                            }
                        }) { Text("Save") }
                        Button(onClick = {
                            if (!signedIn) {
                                showAuth = true
                            } else {
                                scope.launch {
                                    try {
                                        val order = MarketApi.service.createOrder(listing.id, CreateOrderRequest())
                                        selected = null
                                        snackbarHost.showSnackbar(order.message ?: "Order reserved.")
                                    } catch (e: Exception) {
                                        snackbarHost.showSnackbar(e.localizedMessage ?: "Could not place order.")
                                    }
                                }
                            }
                        }) { Text("Buy") }
                    }
                }
            )
        }
    }
}
