package pg.marketpg.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
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
    var savedListings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var myListings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var savedLoading by remember { mutableStateOf(false) }
    var savedError by remember { mutableStateOf<String?>(null) }
    var currentUser by remember { mutableStateOf<AuthUser?>(null) }
    var savedRefresh by remember { mutableIntStateOf(0) }
    var inboxMessages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var myOrders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    var ordersLoading by remember { mutableStateOf(false) }
    var inboxLoading by remember { mutableStateOf(false) }
    var inboxError by remember { mutableStateOf<String?>(null) }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }
    var replyText by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }
    var newCondition by remember { mutableStateOf("used") }
    var newCategoryId by remember { mutableIntStateOf(1) }
    var createBusy by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
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
    LaunchedEffect(tab, signedIn, savedRefresh) {
        if (tab == 2 && signedIn) {
            try { myListings = MarketApi.service.myListings().data } catch (_: Exception) { myListings = emptyList() }
        }
        if (tab == 1 && signedIn) {
            savedLoading = true
            savedError = null
            try {
                savedListings = MarketApi.service.wishlist().data.mapNotNull { it.listing }
            } catch (e: Exception) {
                savedError = e.localizedMessage ?: "Could not load saved listings."
            } finally {
                savedLoading = false
            }
        }
        if ((tab == 3 || tab == 4) && signedIn) {
            try { currentUser = MarketApi.service.me().user } catch (_: Exception) { currentUser = null }
        }
        if (tab == 4 && signedIn) {
            ordersLoading = true
            try { myOrders = MarketApi.service.orders().data } catch (_: Exception) { myOrders = emptyList() }
            finally { ordersLoading = false }
        }
        if (tab == 3 && signedIn) {
            inboxLoading = true
            inboxError = null
            try { inboxMessages = MarketApi.service.messages().data }
            catch (e: Exception) { inboxError = e.localizedMessage ?: "Could not load messages." }
            finally { inboxLoading = false }
        }
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
            } else if (tab == 1 && signedIn) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Text("Saved listings", Modifier.padding(20.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    when {
                        savedLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        savedError != null -> Text(savedError ?: "Error", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                        savedListings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("You haven't saved any listings yet.", color = Color.Gray) }
                        else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(savedListings, key = { it.id }) { item ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.SemiBold)
                                            Text("K " + item.price + " • " + item.location, color = Color.Gray)
                                        }
                                        TextButton(onClick = {
                                            scope.launch {
                                                try {
                                                    MarketApi.service.removeFromWishlist(item.id)
                                                    savedRefresh++
                                                } catch (e: Exception) {
                                                    snackbarHost.showSnackbar(e.localizedMessage ?: "Could not remove item.")
                                                }
                                            }
                                        }) { Text("Remove") }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tab == 2 && signedIn) {
                Column(
                    Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Create a listing", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("Your listing will be reviewed before it appears publicly.", color = Color.Gray)
                    OutlinedTextField(newTitle, { newTitle = it }, label = { Text("Item title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(newDescription, { newDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(newPrice, { newPrice = it }, label = { Text("Price (PGK)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(newLocation, { newLocation = it }, label = { Text("Town / Province") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Text("Condition", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("new", "used", "refurbished").forEach { condition ->
                            FilterChip(newCondition == condition, { newCondition = condition }, label = { Text(condition.replaceFirstChar(Char::uppercase)) })
                        }
                    }
                    Text("Category", fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(newCategoryId == category.id, { newCategoryId = category.id }, label = { Text(category.name) })
                        }
                    }
                    createError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        enabled = !createBusy && newTitle.isNotBlank() && newDescription.isNotBlank() && newPrice.isNotBlank() && newLocation.isNotBlank(),
                        onClick = {
                            scope.launch {
                                createBusy = true
                                createError = null
                                try {
                                    MarketApi.service.createListing(CreateListingRequest(newTitle.trim(), newDescription.trim(), newPrice.trim(), newCondition, newLocation.trim(), newCategoryId))
                                    myListings = MarketApi.service.myListings().data
                                    newTitle = ""; newDescription = ""; newPrice = ""; newLocation = ""
                                    tab = 0
                                    retry++
                                    snackbarHost.showSnackbar("Listing submitted for review.")
                                } catch (e: Exception) {
                                    createError = e.localizedMessage ?: "Could not create listing."
                                } finally {
                                    createBusy = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (createBusy) "Submitting..." else "Submit listing") }
                    Spacer(Modifier.height(12.dp))
                    Text("Your listings", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (myListings.isEmpty()) {
                        Text("You have not posted any listings yet.", color = Color.Gray)
                    } else {
                        myListings.forEach { own ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(own.title, fontWeight = FontWeight.SemiBold)
                                        Text("K " + own.price + " • " + (own.status ?: "pending"), color = Color.Gray, fontSize = 12.sp)
                                    }
                                    if (own.status != "reserved" && own.status != "sold") {
                                        TextButton(onClick = {
                                            scope.launch {
                                                try {
                                                    MarketApi.service.deleteListing(own.id)
                                                    myListings = MarketApi.service.myListings().data
                                                    snackbarHost.showSnackbar("Listing removed.")
                                                } catch (e: Exception) {
                                                    snackbarHost.showSnackbar(e.localizedMessage ?: "Could not remove listing.")
                                                }
                                            }
                                        }) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tab == 3 && signedIn) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Text("Inbox", Modifier.padding(20.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                    when {
                        inboxLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        inboxError != null -> Text(inboxError ?: "Error", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                        inboxMessages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No messages yet.", color = Color.Gray) }
                        else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(inboxMessages, key = { it.id }) { message ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(message.listing?.title ?: "Marketplace conversation", fontWeight = FontWeight.Bold)
                                        Text("From: " + (message.sender?.name ?: "MarketPG user") + " • To: " + (message.recipient?.name ?: "MarketPG user"), color = Color.Gray, fontSize = 12.sp)
                                        Text(message.body, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        TextButton(onClick = { replyingTo = message; replyText = "" }) { Text("Reply") }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tab == 4 && signedIn) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Your profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(currentUser?.name ?: "Loading profile...", fontWeight = FontWeight.SemiBold)
                        Text(currentUser?.email ?: "", color = Color.Gray)
                        TextButton(onClick = { MarketApi.clearToken(); signedIn = false; tab = 0 }) { Text("Sign out") }
                    }
                    Text("Your orders", Modifier.padding(horizontal = 20.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (ordersLoading) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else if (myOrders.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No orders yet.", color = Color.Gray) }
                    } else {
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(myOrders, key = { it.id }) { order ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(order.listing?.title ?: order.order_number, fontWeight = FontWeight.Bold)
                                        Text(order.order_number, color = Color.Gray, fontSize = 12.sp)
                                        Text("K " + order.amount + " • " + order.status.replace('_', ' '), color = Ink)
                                        if (order.seller_id == currentUser?.id && order.status == "awaiting_payment" && order.payment_method == "cash_on_pickup") {
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try { MarketApi.service.confirmCash(order.id); savedRefresh++ }
                                                    catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not confirm payment.") }
                                                }
                                            }) { Text("Confirm cash received") }
                                        }
                                        if (order.seller_id == currentUser?.id && order.status == "paid") {
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try { MarketApi.service.completeOrder(order.id); savedRefresh++ }
                                                    catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not complete order.") }
                                                }
                                            }) { Text("Mark completed") }
                                        }
                                        if (order.buyer_id == currentUser?.id && order.status == "awaiting_payment") {
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try { MarketApi.service.cancelOrder(order.id); savedRefresh++ }
                                                    catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not cancel order.") }
                                                }
                                            }) { Text("Cancel order") }
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
                    Text(listOf("", "Sign in to see your saved listings.", "Seller tools are being connected to your account.", "Buyer and seller conversations will appear here.", "Sign in to manage your account.")[tab], color = Color.Gray)
                }
            }
        }

        replyingTo?.let { message ->
            AlertDialog(
                onDismissRequest = { replyingTo = null },
                title = { Text("Reply to " + (message.sender?.name ?: message.recipient?.name ?: "buyer/seller")) },
                text = { OutlinedTextField(replyText, { replyText = it }, label = { Text("Your message") }, minLines = 3) },
                confirmButton = {
                    Button(enabled = replyText.isNotBlank(), onClick = {
                        scope.launch {
                            try {
                                val recipientId = if (message.sender_id == currentUser?.id) message.recipient_id else message.sender_id
                                MarketApi.service.sendMessage(message.listing_id, MessageRequest(recipientId, replyText.trim()))
                                replyingTo = null
                                replyText = ""
                                savedRefresh++
                                snackbarHost.showSnackbar("Message sent.")
                            } catch (e: Exception) {
                                snackbarHost.showSnackbar(e.localizedMessage ?: "Could not send message.")
                            }
                        }
                    }) { Text("Send") }
                },
                dismissButton = { TextButton(onClick = { replyingTo = null }) { Text("Cancel") } }
            )
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
                                        savedRefresh++
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
