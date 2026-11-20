package pg.marketpg.app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val PngProvinces = listOf("Central", "Gulf", "Milne Bay", "Northern (Oro)", "Southern Highlands", "Western Highlands", "Enga", "Chimbu (Simbu)", "Eastern Highlands", "Morobe", "Madang", "East Sepik", "West Sepik (Sandaun)", "West New Britain", "East New Britain", "New Ireland", "Manus", "Hela", "Jiwaka", "Western (Fly River)", "National Capital District", "Autonomous Region of Bougainville")

private val LightColors = lightColorScheme(primary = Color(0xFF2563EB), onPrimary = Color.White, primaryContainer = Color(0xFFDBEAFE), onPrimaryContainer = Color(0xFF102A56), secondary = Color(0xFF475569), onSecondary = Color.White, tertiary = Color(0xFF60A5FA), background = Color(0xFFF7F8FA), onBackground = Color(0xFF111827), surface = Color.White, onSurface = Color(0xFF111827), surfaceVariant = Color(0xFFF1F5F9), onSurfaceVariant = Color(0xFF64748B), outline = Color(0xFFCBD5E1))
private val DarkColors = darkColorScheme(primary = Color(0xFF60A5FA), onPrimary = Color(0xFF071A33), primaryContainer = Color(0xFF172B46), onPrimaryContainer = Color(0xFFDBEAFE), secondary = Color(0xFF94A3B8), onSecondary = Color(0xFF111827), tertiary = Color(0xFF93C5FD), background = Color.Black, onBackground = Color(0xFFF9FAFB), surface = Color(0xFF171717), onSurface = Color(0xFFF3F4F6), surfaceVariant = Color(0xFF262626), onSurfaceVariant = Color(0xFFA3A3A3), outline = Color(0xFF525252))

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
    val systemDarkTheme = isSystemInDarkTheme()
    var search by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var darkTheme by remember { mutableStateOf(systemDarkTheme) }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var locationFilter by remember { mutableStateOf("") }
    var filterProvince by remember { mutableStateOf("") }
    var newProvince by remember { mutableStateOf("") }
    var editProvince by remember { mutableStateOf("") }
    var profileProvince by remember { mutableStateOf("") }
    var conditionFilter by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf("newest") }
    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var listings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<ListingDto?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var retry by remember { mutableIntStateOf(0) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var mobileBannerAdUnitId by remember { mutableStateOf<String?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    var savedListings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var myListings by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
    var savedLoading by remember { mutableStateOf(false) }
    var savedError by remember { mutableStateOf<String?>(null) }
    var currentUser by remember { mutableStateOf<AuthUser?>(null) }
    var profileName by remember { mutableStateOf("") }
    var profilePhone by remember { mutableStateOf("") }
    var profileLocation by remember { mutableStateOf("") }
    var profileAccountType by remember { mutableStateOf("both") }
    var payoutBankName by remember { mutableStateOf("") }
    var payoutAccountName by remember { mutableStateOf("") }
    var payoutAccountNumber by remember { mutableStateOf("") }
    var profileBusy by remember { mutableStateOf(false) }
    var identityStatus by remember { mutableStateOf<IdentityStatusDto?>(null) }
    var identityBusy by remember { mutableStateOf(false) }
    var identityDocType by remember { mutableStateOf("png_nid") }
    var idFrontUri by remember { mutableStateOf<Uri?>(null) }
    var idBackUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var savedRefresh by remember { mutableIntStateOf(0) }
    var inboxMessages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var myOrders by remember { mutableStateOf<List<OrderDto>>(emptyList()) }
    var receiptOrder by remember { mutableStateOf<OrderDto?>(null) }
    var trackingOrder by remember { mutableStateOf<OrderDto?>(null) }
    var trackingText by remember { mutableStateOf("") }
    var ordersLoading by remember { mutableStateOf(false) }
    var myDisputes by remember { mutableStateOf<List<DisputeDto>>(emptyList()) }
    var disputesLoading by remember { mutableStateOf(false) }
    var inboxLoading by remember { mutableStateOf(false) }
    var inboxError by remember { mutableStateOf<String?>(null) }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }
    var disputeOrder by remember { mutableStateOf<OrderDto?>(null) }
    var disputeReason by remember { mutableStateOf("not_as_described") }
    var disputeDetails by remember { mutableStateOf("") }
    var replyText by remember { mutableStateOf("") }
    var messageListing by remember { mutableStateOf<ListingDto?>(null) }
    var messageDraft by remember { mutableStateOf("") }
    var messageBusy by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }
    var newImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var newCondition by remember { mutableStateOf("used") }
    var newCategoryId by remember { mutableIntStateOf(1) }
    var createBusy by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    var editingListing by remember { mutableStateOf<ListingDto?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editCondition by remember { mutableStateOf("used") }
    var editCategoryId by remember { mutableIntStateOf(1) }
    var editBusy by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }
    var signedIn by remember { mutableStateOf(MarketApi.hasSession()) }
    var showAuth by remember { mutableStateOf(false) }
    var registerMode by remember { mutableStateOf(false) }
    var authName by remember { mutableStateOf("") }
    var authEmail by remember { mutableStateOf("") }
    var authPassword by remember { mutableStateOf("") }
    var authConfirm by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var authBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val loginLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            signedIn = true
            showAuth = false
            tab = 4
        }
    }
    LaunchedEffect(showAuth) {



        if (showNetworkDialog) { AlertDialog(onDismissRequest = { showNetworkDialog = false }, shape = RoundedCornerShape(16.dp), containerColor = colors.surface, title = { Text("No internet connection") }, text = { Text("MarketPG couldn't reach the server. Check your connection and try again.", color = colors.onSurfaceVariant) }, confirmButton = { Button(onClick = { showNetworkDialog = false; retry++ }) { Text("Retry") } }, dismissButton = { TextButton(onClick = { showNetworkDialog = false }) { Text("Not now") } }) }
        messageListing?.let { listing ->
            AlertDialog(
                onDismissRequest = { if (!messageBusy) messageListing = null },
                title = { Text("Message seller") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ask a question about " + listing.title + ". Keep payments and personal information safe.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        OutlinedTextField(
                            value = messageDraft,
                            onValueChange = { messageDraft = it; messageError = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Write your message...") },
                            minLines = 3,
                            maxLines = 5
                        , shape = RoundedCornerShape(14.dp))
                        messageError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !messageBusy && messageDraft.isNotBlank() && listing.user != null,
                        onClick = {
                            scope.launch {
                                messageBusy = true
                                messageError = null
                                try {
                                    MarketApi.service.sendMessage(
                                        listing.id,
                                        MessageRequest(listing.user!!.id.toLong(), messageDraft.trim())
                                    )
                                    messageListing = null
                                    messageDraft = ""
                                    tab = 3
                                    inboxMessages = MarketApi.service.messages().data
                                    snackbarHost.showSnackbar("Message sent to seller.")
                                } catch (e: Exception) {
                                    messageError = e.localizedMessage ?: "Could not send your message."
                                } finally {
                                    messageBusy = false
                                }
                            }
                        }
                    ) { Text(if (messageBusy) "Sending..." else "Send message") }
                },
                dismissButton = {
                    TextButton(enabled = !messageBusy, onClick = { messageListing = null }) { Text("Cancel") }
                }
            )
        }


        trackingOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { trackingOrder = null },
                title = { Text("Shipment details") },
                text = { OutlinedTextField(trackingText, { trackingText = it }, label = { Text("Tracking reference") }, modifier = Modifier.fillMaxWidth(), singleLine = true) },
                confirmButton = {
                    Button(enabled = trackingText.isNotBlank(), onClick = {
                        scope.launch {
                            try {
                                MarketApi.service.shipOrder(order.id, ShipOrderRequest(trackingText.trim()))
                                myOrders = MarketApi.service.orders().data
                                trackingOrder = null
                                snackbarHost.showSnackbar("Shipment recorded.")
                            } catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not record shipment.") }
                        }
                    }) { Text("Confirm shipment") }
                },
                dismissButton = { TextButton(onClick = { trackingOrder = null }) { Text("Cancel") } }
            )
        }

        selected?.let { listing ->
            LaunchedEffect(listing.id) {
                context.startActivity(Intent(context, ListingDetailActivity::class.java).apply {
                    putExtra("listing_id", listing.id)
                    putExtra("title", listing.title)
                    putExtra("description", listing.description ?: "No description supplied.")
                    putExtra("price", listing.price)
                    putExtra("location", listing.location)
                    putExtra("condition", listing.condition ?: "")
                    putExtra("seller", listing.user?.name ?: "MarketPG seller")
                    putExtra("image_url", listing.imageUrl ?: "")
                    putStringArrayListExtra("gallery_urls", ArrayList(listing.images.map { it.url }))
                    putExtra("seller_rank", listing.user?.seller_rank ?: "New Seller")
                })
                selected = null
            }
        }
    }
}

@Composable
private fun ListingShimmer(modifier: Modifier = Modifier) {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "listing-shimmer")
    val alpha by transition.animateFloat(0.35f, 0.8f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(850), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse), label = "shimmer-alpha")
    val placeholder = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = modifier, contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(6) { Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) { Box(Modifier.fillMaxWidth().height(112.dp).background(placeholder, RoundedCornerShape(8.dp))); Box(Modifier.fillMaxWidth(0.82f).height(13.dp).background(placeholder, RoundedCornerShape(8.dp))); Box(Modifier.fillMaxWidth(0.52f).height(17.dp).background(placeholder, RoundedCornerShape(8.dp))); Box(Modifier.fillMaxWidth(0.66f).height(10.dp).background(placeholder, RoundedCornerShape(8.dp))) } } }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PngProvinceDropdown(value: String, onValueChange: (String) -> Unit, label: String = "Province") {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true,
            label = { Text(label) }, placeholder = { Text("Select province") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PngProvinces.forEach { province ->
                DropdownMenuItem(text = { Text(province) }, onClick = { onValueChange(province); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class ListingDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title").orEmpty()
        val description = intent.getStringExtra("description").orEmpty()
        val price = intent.getStringExtra("price").orEmpty()
        val location = intent.getStringExtra("location").orEmpty()
        val condition = intent.getStringExtra("condition").orEmpty()
        val seller = intent.getStringExtra("seller").orEmpty()
        val imageUrl = intent.getStringExtra("image_url").orEmpty()
        val galleryUrls = intent.getStringArrayListExtra("gallery_urls").orEmpty().let { urls -> (listOf(imageUrl) + urls).filter { it.isNotBlank() }.distinct() }
        val sellerRank = intent.getStringExtra("seller_rank").orEmpty()
        val listingId = intent.getLongExtra("listing_id", 0L)
        setContent {
            val scope = rememberCoroutineScope()
            var createdOrder by remember { mutableStateOf<OrderDto?>(null) }
            var paymentInstructions by remember { mutableStateOf("") }
            var checkoutBusy by remember { mutableStateOf(false) }
            var checkoutError by remember { mutableStateOf<String?>(null) }
            val receiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                val order = createdOrder
                if (uri != null && order != null) scope.launch {
                    try {
                        MarketApi.service.uploadReceipt(order.id, uri.toUploadPart(this@ListingDetailActivity, "receipt"))
                        checkoutError = "Receipt uploaded. MarketPG will verify your payment."
                    } catch (e: Exception) { checkoutError = e.localizedMessage ?: "Receipt upload unavailable." }
                }
            }
            val dark = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
                Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
                    Surface(shadowElevation = 1.dp) {
                        TopAppBar(title = { Text("Item details") }, navigationIcon = {
                            IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        })
                    }
                }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().height(280.dp))
                            Text("Item gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(galleryUrls) { url -> AsyncImage(model = url, contentDescription = "Item screenshot", modifier = Modifier.size(88.dp)) }
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Text("No item photos uploaded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("K $price", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        Text(description, style = MaterialTheme.typography.bodyLarge)
                        HorizontalDivider()
                        Text("Location: $location")
                        if (condition.isNotBlank()) Text("Condition: $condition")
                        Text("Seller: $seller")
                        Text("Seller level: $sellerRank", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        if (MarketApi.hasSession()) {
                            if (createdOrder == null) {
                                Button(enabled = !checkoutBusy && listingId > 0, onClick = {
                                    scope.launch {
                                        checkoutBusy = true
                                        checkoutError = null
                                        try {
                                            val response = MarketApi.service.createOrder(listingId, CreateOrderRequest())
                                            createdOrder = response.data
                                            paymentInstructions = response.payment_instructions ?: ""
                                        } catch (e: Exception) { checkoutError = e.localizedMessage ?: "Unable to create order." }
                                        finally { checkoutBusy = false }
                                    }
                                }, modifier = Modifier.fillMaxWidth()) { Text(if (checkoutBusy) "Preparing order..." else "Buy securely through MarketPG") }
                            } else {
                                Text("Order " + createdOrder?.order_number, fontWeight = FontWeight.Bold)
                                Text("Payment instructions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(paymentInstructions.ifBlank { "Platform bank transfer instructions are not configured yet. Please contact MarketPG support." })
                                Button(onClick = { receiptPicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) { Text("Upload payment receipt") }
                            }
                        } else {
                            Text("Sign in to place an order.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        checkoutError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    }
                }
            }
        }
    }
}


private fun Uri.toUploadPart(context: android.content.Context, field: String): MultipartBody.Part {
    val mime = context.contentResolver.getType(this) ?: "application/octet-stream"
    val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() }
        ?: throw IllegalStateException("Could not read selected file.")
    val fileName = when (field) { "id_front" -> "id-front"; "id_back" -> "id-back"; "selfie" -> "selfie"; "cover_photo" -> "cover"; "images[]" -> "listing-image"; else -> "profile" } +
        if (mime == "application/pdf") ".pdf" else ".jpg"
    return MultipartBody.Part.createFormData(field, fileName, bytes.toRequestBody(mime.toMediaTypeOrNull()))
}
