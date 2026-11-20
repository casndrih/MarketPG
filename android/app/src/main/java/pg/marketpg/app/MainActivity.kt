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
        MarketDiagnostics.initialize(applicationContext)
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
    var homeBanners by remember { mutableStateOf<List<HomeBannerDto>>(emptyList()) }
    var sellerBio by remember { mutableStateOf("") }
    var sellerWebsite by remember { mutableStateOf("") }
    var sellerFacebook by remember { mutableStateOf("") }
    var sellerX by remember { mutableStateOf("") }
    var sellerLinkedIn by remember { mutableStateOf("") }
    var smeTin by remember { mutableStateOf("") }
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
        } else {
            (context as? android.app.Activity)?.finish()
        }
    }
    LaunchedEffect(Unit) {
        if (!signedIn) showAuth = true
    }
    LaunchedEffect(showAuth) {
        if (showAuth) {
            showAuth = false
            loginLauncher.launch(Intent(context, LoginActivity::class.java))
        }
    }
    val listingImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { newImages = it }
    val profilePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            profileBusy = true
            try { currentUser = MarketApi.service.uploadProfileMedia(uri.toUploadPart(context, "profile_photo"), null).user; snackbarHost.showSnackbar("Profile photo saved.") }
            catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Profile photo upload unavailable.") }
            finally { profileBusy = false }
        }
    }
    val coverPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            profileBusy = true
            try { currentUser = MarketApi.service.uploadProfileMedia(null, uri.toUploadPart(context, "cover_photo")).user; snackbarHost.showSnackbar("Cover photo saved.") }
            catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Cover photo upload unavailable.") }
            finally { profileBusy = false }
        }
    }
    val idFrontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { idFrontUri = it }
    val idBackPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { idBackUri = it }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selfieUri = it }
    val receiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val order = receiptOrder
        if (uri != null && order != null) scope.launch {
            try {
                MarketApi.service.uploadReceipt(order.id, uri.toUploadPart(context, "receipt"))
                snackbarHost.showSnackbar("Payment receipt submitted for admin verification.")
                savedRefresh++
            } catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Receipt upload unavailable.") }
            finally { receiptOrder = null }
        }
    }


    SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        val systemBarColor = (if (darkTheme) DarkColors.surface else LightColors.surface).toArgb()
        window?.statusBarColor = systemBarColor
        window?.navigationBarColor = systemBarColor
        window?.decorView?.systemUiVisibility = if (darkTheme) {
            0
        } else {
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    LaunchedEffect(Unit) {
        try { categories = MarketApi.service.categories().data } catch (_: Exception) { }
        try { homeBanners = MarketApi.service.homeCarousel().data } catch (_: Exception) { homeBanners = emptyList() }
        try {
            val ads = withContext(Dispatchers.IO) { MarketApi.service.publicConfig().ads }
            val appId = ads.mobile_app_id?.takeIf { it.startsWith("ca-app-pub-") && it.contains("~") }
            val unitId = ads.mobile_banner_ad_unit_id?.takeIf { it.startsWith("ca-app-pub-") && it.contains("/") }
            if (ads.enabled && appId != null && unitId != null) {
                withContext(Dispatchers.IO) {
                    MobileAds.initialize(context.applicationContext, InitializationConfig.Builder(appId).build()) { }
                }
                mobileBannerAdUnitId = unitId
            }
        } catch (_: Exception) { mobileBannerAdUnitId = null }
    }
    LaunchedEffect(signedIn) {
        if (signedIn) {
            try { currentUser = MarketApi.service.me().user } catch (_: Exception) { currentUser = null }
        } else {
            currentUser = null
        }
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
            try {
                currentUser = MarketApi.service.me().user
                identityStatus = MarketApi.service.identityStatus().data
                profileName = currentUser?.name ?: ""
                profilePhone = currentUser?.phone ?: ""
                profileLocation = currentUser?.town ?: currentUser?.location?.substringBefore(",")?.trim().orEmpty()
                profileProvince = currentUser?.province ?: currentUser?.location?.substringAfter(",", "")?.trim().orEmpty()
                profileAccountType = currentUser?.account_type ?: "both"
                sellerBio = currentUser?.seller_bio.orEmpty()
                sellerWebsite = currentUser?.seller_website.orEmpty()
                sellerFacebook = currentUser?.seller_facebook_url.orEmpty()
                sellerX = currentUser?.seller_x_url.orEmpty()
                sellerLinkedIn = currentUser?.seller_linkedin_url.orEmpty()
            } catch (_: Exception) { currentUser = null }
        }
        if (tab == 4 && signedIn) {
            ordersLoading = true
            disputesLoading = true
            try { myOrders = MarketApi.service.orders().data } catch (_: Exception) { myOrders = emptyList() }
            finally { ordersLoading = false }
            try { myDisputes = MarketApi.service.disputes().data.data } catch (_: Exception) { myDisputes = emptyList() }
            finally { disputesLoading = false }
        }
        if (tab == 3 && signedIn) {
            inboxLoading = true
            inboxError = null
            try { inboxMessages = MarketApi.service.messages().data }
            catch (e: Exception) { inboxError = e.localizedMessage ?: "Could not load messages." }
            finally { inboxLoading = false }
        }
    }
    LaunchedEffect(search, categoryId, minPrice, maxPrice, locationFilter, conditionFilter, sortOrder, retry) {
        delay(300)
        loading = true
        error = null
        try {
            listings = MarketApi.service.listings(
                query = search.trim().takeIf { it.isNotEmpty() },
                categoryId = categoryId,
                location = listOf(locationFilter.trim(), filterProvince).filter { it.isNotBlank() }.joinToString(", ").takeIf { it.isNotEmpty() },
                sort = sortOrder,
                minPrice = minPrice.toDoubleOrNull(),
                maxPrice = maxPrice.toDoubleOrNull(),
                condition = conditionFilter
            ).data
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Unable to connect to MarketPG."
            showNetworkDialog = true
        } finally { loading = false }
    }

    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(50), medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(16.dp))) {
        val colors = MaterialTheme.colorScheme
        Scaffold(
            containerColor = colors.background,
            topBar = {
                Surface(shadowElevation = 1.dp, color = colors.surface) {
                TopAppBar(
                    title = { Text("MarketPG", fontWeight = FontWeight.SemiBold, color = colors.onSurface) },
                    navigationIcon = {
                        if (showSearch) IconButton(onClick = { showSearch = false; search = "" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search") }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) { Icon(Icons.Filled.Search, contentDescription = "Search listings") }
                        IconButton(onClick = { darkTheme = !darkTheme }) {
                            Icon(
                                imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (darkTheme) "Switch to light theme" else "Switch to dark theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.surface,
                        titleContentColor = colors.onSurface,
                        actionIconContentColor = colors.onSurface
                    )
                )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHost) },
            bottomBar = {
                Surface(shadowElevation = 1.dp, color = colors.surface) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                mobileBannerAdUnitId?.let { MarketPgBannerAd(it) }
                NavigationBar(containerColor = colors.surface, contentColor = colors.onSurface, tonalElevation = 0.dp) {
                    val labels = listOf("Explore", "Saved", "Sell", "Inbox", "Profile")
                    val icons = listOf(
                        Icons.Filled.Home,
                        Icons.Filled.FavoriteBorder,
                        Icons.Filled.AddCircle,
                        Icons.Filled.ChatBubbleOutline,
                        Icons.Filled.PersonOutline
                    )
                    labels.forEachIndexed { i, label ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i; if (i != 0 && !signedIn) { showAuth = true; authError = null } },
                            icon = { Icon(icons[i], contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                indicatorColor = colors.primaryContainer,
                                unselectedIconColor = colors.onSurfaceVariant,
                                unselectedTextColor = colors.onSurfaceVariant
                            )
                        )
                    }
                }
                }
                }
            }
        ) { padding ->
            if (showSearch) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth().padding(16.dp), placeholder = { Text("Search phones, cars, furniture...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(50))
                    Text("Search results", Modifier.padding(horizontal = 18.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    when {
                        loading -> ListingShimmer(Modifier.fillMaxSize())
                        listings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No matching listings found.", color = colors.onSurfaceVariant) }
                        else -> LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(listings, key = { it.id }) { listing ->
                                Card(Modifier.clickable { selected = listing }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                    Column {
                                        if (listing.imageUrl != null) AsyncImage(listing.imageUrl, listing.title, Modifier.fillMaxWidth().height(118.dp))
                                        else Box(Modifier.fillMaxWidth().height(118.dp).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { Text("📦", fontSize = 38.sp) }
                                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(listing.title, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("K " + listing.price, color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text("⌖ " + listing.location, color = colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tab == 0) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    if (homeBanners.isNotEmpty()) {
                        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(homeBanners, key = { it.id }) { banner ->
                                Card(
                                    Modifier.width(330.dp).height(156.dp).clickable {
                                        banner.link_url?.takeIf { it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true) }?.let {
                                            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } catch (_: Exception) { }
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
                                ) {
                                    Box(Modifier.fillMaxSize()) {
                                        AsyncImage(model = secureMarketPgUrl(banner.image_url), contentDescription = banner.alt_text ?: "MarketPG promotion", modifier = Modifier.fillMaxSize())
                                        if (!banner.alt_text.isNullOrBlank()) Text(banner.alt_text, Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.55f)).padding(10.dp), color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                    if (listings.isNotEmpty()) {
                        Text("Featured finds", Modifier.padding(start = 18.dp, top = 10.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(listings.take(6), key = { "featured-${it.id}" }) { featured ->
                                Card(Modifier.width(260.dp).height(156.dp).clickable { selected = featured }, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)) {
                                    Box(Modifier.fillMaxSize()) {
                                        featured.imageUrl?.let { AsyncImage(it, featured.title, Modifier.fillMaxSize()) }
                                        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(colors.surface.copy(alpha = 0.92f)).padding(10.dp)) {
                                            Text(featured.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                            Text("K ${featured.price} • ${featured.location}", color = colors.primary, maxLines = 1, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = colors.primaryContainer), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("GREAT LOCAL FINDS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Give good things\na second life.", color = colors.onPrimaryContainer, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Discover real listings from sellers across Papua New Guinea.", color = colors.onSurfaceVariant)
                        }
                    }
                    Text("Browse categories", Modifier.padding(start = 18.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(categoryId == null, { categoryId = null }, label = { Text("All") }, shape = RoundedCornerShape(50)) }
                        items(categories.size) { index ->
                            val category = categories[index]
                            FilterChip(categoryId == category.id, { categoryId = category.id }, label = { Text(category.name) }, shape = RoundedCornerShape(50))
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Fresh finds", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text(listings.size.toString() + " listings", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        }
                        TextButton(onClick = { showFilters = true }) { Icon(Icons.Filled.Tune, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Filter & sort") }
                    }
                    when {
                        loading -> ListingShimmer(Modifier.weight(1f).fillMaxWidth())
                        error != null -> Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Couldn't load listings", fontWeight = FontWeight.Bold)
                            Text(error ?: "Network error", color = colors.onSurfaceVariant)
                            Button(onClick = { retry++ }) { Text("Try again") }
                        }
                        listings.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No active listings found. Try another search.", color = colors.onSurfaceVariant) }
                        else -> PullToRefreshBox(isRefreshing = loading, onRefresh = { retry++ }, modifier = Modifier.weight(1f).fillMaxWidth()) {
                            LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(listings, key = { it.id }) { listing ->
                                Card(Modifier.clickable { selected = listing }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                    Column {
                                        if (listing.imageUrl != null) AsyncImage(listing.imageUrl, listing.title, Modifier.fillMaxWidth().height(118.dp))
                                        else Box(Modifier.fillMaxWidth().height(118.dp).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { Text("📦", fontSize = 38.sp) }
                                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(listing.title, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("K " + listing.price, color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text("⌖ " + listing.location, color = colors.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            } else if (tab == 1 && signedIn) {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    Text("Saved listings", Modifier.padding(20.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    when {
                        savedLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        savedError != null -> Text(savedError ?: "Error", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                        savedListings.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("You haven't saved any listings yet.", color = colors.onSurfaceVariant) }
                        else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(savedListings, key = { it.id }) { item ->
                                Card(colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(item.title, fontWeight = FontWeight.SemiBold)
                                            Text("K " + item.price + " • " + item.location, color = colors.onSurfaceVariant)
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
                    Text("Create a listing", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    Text("Your listing will be reviewed before it appears publicly.", color = colors.onSurfaceVariant)
                    OutlinedTextField(newTitle, { newTitle = it }, label = { Text("Item title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(newDescription, { newDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(newPrice, { newPrice = it }, label = { Text("Price (PGK)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(newLocation, { newLocation = it }, label = { Text("Town / City") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    PngProvinceDropdown(newProvince, { newProvince = it })
                    OutlinedButton(onClick = { listingImagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text(if (newImages.isEmpty()) "Add item photos (up to 10)" else "${newImages.size} item photos selected") }
                    Text("Listing photo uploads activate after persistent image storage is configured.", color = colors.onSurfaceVariant, fontSize = 11.sp)
                    Text("Condition", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("new", "used", "refurbished").forEach { condition ->
                            FilterChip(newCondition == condition, { newCondition = condition }, label = { Text(condition.replaceFirstChar(Char::uppercase)) }, shape = RoundedCornerShape(50))
                        }
                    }
                    Text("Category", fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            FilterChip(newCategoryId == category.id, { newCategoryId = category.id }, label = { Text(category.name) }, shape = RoundedCornerShape(50))
                        }
                    }
                    createError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        enabled = !createBusy && newTitle.isNotBlank() && newDescription.isNotBlank() && newPrice.isNotBlank() && newLocation.isNotBlank() && newProvince.isNotBlank(),
                        onClick = {
                            scope.launch {
                                createBusy = true
                                createError = null
                                try {
                                    if (newImages.isEmpty()) {
                                        MarketApi.service.createListing(CreateListingRequest(newTitle.trim(), newDescription.trim(), newPrice.trim(), newCondition, listOf(newLocation.trim(), newProvince).filter { it.isNotBlank() }.joinToString(", "), newCategoryId, newLocation.trim(), newProvince))
                                    } else {
                                        val textType = "text/plain".toMediaTypeOrNull()
                                        val fields = mapOf(
                                            "title" to newTitle.trim().toRequestBody(textType),
                                            "description" to newDescription.trim().toRequestBody(textType),
                                            "price" to newPrice.trim().toRequestBody(textType),
                                            "condition" to newCondition.toRequestBody(textType),
                                            "location" to listOf(newLocation.trim(), newProvince).joinToString(", ").toRequestBody(textType),
                                            "town" to newLocation.trim().toRequestBody(textType),
                                            "province" to newProvince.toRequestBody(textType),
                                            "category_id" to newCategoryId.toString().toRequestBody(textType)
                                        )
                                        MarketApi.service.createListingWithImages(fields, newImages.map { it.toUploadPart(context, "images[]") })
                                    }
                                    myListings = MarketApi.service.myListings().data
                                    newTitle = ""; newDescription = ""; newPrice = ""; newLocation = ""; newProvince = ""; newImages = emptyList()
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
                        Text("You have not posted any listings yet.", color = colors.onSurfaceVariant)
                    } else {
                        myListings.forEach { own ->
                            Card(colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(own.title, fontWeight = FontWeight.SemiBold)
                                        Text("K " + own.price + " • " + (own.status ?: "pending"), color = colors.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    if (own.status != "reserved" && own.status != "sold") {
                                        TextButton(onClick = {
                                            editingListing = own
                                            editTitle = own.title
                                            editDescription = own.description.orEmpty()
                                            editPrice = own.price
                                            editLocation = own.location.substringBefore(",").trim()
                                            editProvince = own.location.substringAfter(",", "").trim()
                                            editCondition = own.condition ?: "used"
                                            editCategoryId = categories.firstOrNull { it.id == own.categoryId }?.id ?: categories.firstOrNull()?.id ?: 1
                                            editError = null
                                        }) { Text("Edit") }
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
                    Text("Inbox", Modifier.padding(20.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    when {
                        inboxLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        inboxError != null -> Text(inboxError ?: "Error", Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                        inboxMessages.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No messages yet.", color = colors.onSurfaceVariant) }
                        else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(inboxMessages, key = { it.id }) { message ->
                                Card(colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(message.listing?.title ?: "Marketplace conversation", fontWeight = FontWeight.Bold)
                                        Text("From: " + (message.sender?.name ?: "MarketPG user") + " • To: " + (message.recipient?.name ?: "MarketPG user"), color = colors.onSurfaceVariant, fontSize = 12.sp)
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
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        Text(currentUser?.email ?: "", color = colors.onSurfaceVariant)
                        Box(Modifier.fillMaxWidth().height(116.dp).background(colors.surfaceVariant, RoundedCornerShape(16.dp))) {
                            currentUser?.cover_photo_url?.let { AsyncImage(model = secureMarketPgUrl(it), contentDescription = "Profile cover photo", modifier = Modifier.fillMaxSize()) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!currentUser?.profile_photo_url.isNullOrBlank()) AsyncImage(model = secureMarketPgUrl(currentUser?.profile_photo_url), contentDescription = "Profile photo", modifier = Modifier.size(76.dp))
                            else Box(Modifier.size(76.dp).background(colors.primaryContainer, RoundedCornerShape(50)), contentAlignment = Alignment.Center) { Text(currentUser?.name?.firstOrNull()?.uppercase() ?: "M", color = colors.onPrimaryContainer, fontSize = 28.sp, fontWeight = FontWeight.SemiBold) }
                            Column(Modifier.weight(1f)) {
                                Text("Profile and cover photos are required", fontWeight = FontWeight.SemiBold)
                                Text("Secure image storage is not configured yet, so uploads are temporarily paused.", color = colors.onSurfaceVariant, fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(enabled = !profileBusy, onClick = { profilePhotoPicker.launch("image/*") }) { Text("Profile photo") }
                                    TextButton(enabled = !profileBusy, onClick = { coverPhotoPicker.launch("image/*") }) { Text("Cover photo") }
                                }
                            }
                        }
                        Text("Seller level: " + (currentUser?.seller_rank ?: "New Seller"), color = colors.primary, fontWeight = FontWeight.SemiBold)
                        HorizontalDivider()
                        Text("App diagnostics", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Upload a redacted technical report to the MarketPG GitHub repository to help troubleshoot app and network errors. No passwords, tokens, request bodies or identity documents are recorded.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        var diagnosticsBusy by remember { mutableStateOf(false) }
                        OutlinedButton(
                            enabled = !diagnosticsBusy,
                            onClick = {
                                scope.launch {
                                    diagnosticsBusy = true
                                    try {
                                        val version = packageManager.getPackageInfo(packageName, 0).versionName ?: "unknown"
                                        val result = MarketApi.service.uploadDiagnostics(
                                            DiagnosticLogRequest(
                                                report = MarketDiagnostics.exportReport(),
                                                app_version = version,
                                                device = MarketDiagnostics.deviceSummary()
                                            )
                                        )
                                        snackbarHost.showSnackbar(result.message ?: "Diagnostic report uploaded.")
                                    } catch (e: Exception) {
                                        MarketDiagnostics.record("ERROR", "diagnostics-upload", e.javaClass.simpleName)
                                        snackbarHost.showSnackbar(MarketApiErrors.userMessage(e, "upload diagnostics"))
                                    } finally { diagnosticsBusy = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(if (diagnosticsBusy) "Uploading diagnostic report..." else "Upload diagnostic logs to GitHub")
                        }
                        if (profileAccountType != "buyer") {
                            HorizontalDivider()
                            Text("Seller profile", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Tell buyers about your business and add your public social links.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                            OutlinedTextField(sellerBio, { sellerBio = it }, label = { Text("Seller bio / description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6, shape = RoundedCornerShape(14.dp))
                            OutlinedTextField(sellerWebsite, { sellerWebsite = it }, label = { Text("Website URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                            OutlinedTextField(sellerFacebook, { sellerFacebook = it }, label = { Text("Facebook page URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                            OutlinedTextField(sellerX, { sellerX = it }, label = { Text("X page URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                            OutlinedTextField(sellerLinkedIn, { sellerLinkedIn = it }, label = { Text("LinkedIn page URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                            Text("Registered SME verification", fontWeight = FontWeight.SemiBold)
                            Text("Provide your business TIN for admin review. Your TIN is encrypted and never shown publicly.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                            Text("Status: " + (currentUser?.sme_status?.replace('_', ' ')?.replaceFirstChar(Char::uppercase) ?: "Not submitted"), color = when(currentUser?.sme_status) { "verified" -> Color(0xFF16A34A); "rejected" -> MaterialTheme.colorScheme.error; "pending" -> Color(0xFFD97706); else -> colors.onSurfaceVariant })
                            OutlinedTextField(smeTin, { smeTin = it }, label = { Text("Business TIN") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        }

                        OutlinedTextField(profileName, { profileName = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(profilePhone, { profilePhone = it }, label = { Text("Phone number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(profileLocation, { profileLocation = it }, label = { Text("Town / City") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        PngProvinceDropdown(profileProvince, { profileProvince = it })

                        HorizontalDivider()
                        Text("Identity verification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Status: " + (identityStatus?.status?.replaceFirstChar(Char::uppercase) ?: "Not submitted"), color = when(identityStatus?.status) { "approved" -> Color(0xFF16A34A); "rejected" -> MaterialTheme.colorScheme.error; else -> colors.onSurfaceVariant })
                        Text("Upload a PNG National ID, passport or driver's licence. Buyer purchases require approval.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        Text("Encrypted document storage is not configured yet; ID submissions are temporarily paused.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("png_nid" to "PNG National ID", "passport" to "Passport", "drivers_licence" to "Driver licence").forEach { (value, label) ->
                                FilterChip(identityDocType == value, { identityDocType = value }, label = { Text(label) }, shape = RoundedCornerShape(50))
                            }
                        }
                        identityStatus?.rejection_reason?.takeIf { it.isNotBlank() }?.let { Text("Review note: $it", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { idFrontPicker.launch("*/*") }) { Text(if (idFrontUri == null) "ID front" else "Front selected") }
                            OutlinedButton(onClick = { idBackPicker.launch("*/*") }) { Text(if (idBackUri == null) "ID back (optional)" else "Back selected") }
                            OutlinedButton(onClick = { selfiePicker.launch("image/*") }) { Text(if (selfieUri == null) "Selfie" else "Selfie selected") }
                        }
                        Button(enabled = !identityBusy && idFrontUri != null && selfieUri != null && identityStatus?.status != "pending" && identityStatus?.status != "approved", onClick = {
                            scope.launch {
                                identityBusy = true
                                try {
                                    val front = idFrontUri!!.toUploadPart(context, "id_front")
                                    val selfie = selfieUri!!.toUploadPart(context, "selfie")
                                    val back = idBackUri?.toUploadPart(context, "id_back")
                                    MarketApi.service.submitIdentity(identityDocType.toRequestBody("text/plain".toMediaTypeOrNull()), "true".toRequestBody("text/plain".toMediaTypeOrNull()), front, back, selfie)
                                    identityStatus = MarketApi.service.identityStatus().data
                                    snackbarHost.showSnackbar("Identity documents submitted for review.")
                                } catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Identity upload unavailable.") }
                                finally { identityBusy = false }
                            }
                        }) { Text(if (identityBusy) "Submitting..." else "Submit identity documents") }

                        Text("Seller payout account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Add the bank account where MarketPG will send your payout after delivery is confirmed. Details are encrypted at rest.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        OutlinedTextField(payoutBankName, { payoutBankName = it }, label = { Text("Bank name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(payoutAccountName, { payoutAccountName = it }, label = { Text("Account holder name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(payoutAccountNumber, { payoutAccountNumber = it }, label = { Text("Account number") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        Text("Account type", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("buyer", "seller", "both").forEach { type ->
                                FilterChip(profileAccountType == type, { profileAccountType = type }, label = { Text(type.replaceFirstChar(Char::uppercase)) }, shape = RoundedCornerShape(50))
                            }
                        }
                        Button(enabled = !profileBusy && profileName.isNotBlank() && profileLocation.isNotBlank() && profileProvince.isNotBlank(), onClick = {
                            scope.launch {
                                profileBusy = true
                                try {
                                    currentUser = MarketApi.service.updateProfile(ProfileUpdateRequest(profileName.trim(), profilePhone.trim().ifBlank { null }, listOf(profileLocation.trim(), profileProvince).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null }, profileAccountType, profileLocation.trim().ifBlank { null }, profileProvince.ifBlank { null }, payoutBankName.trim().ifBlank { null }, payoutAccountName.trim().ifBlank { null }, payoutAccountNumber.trim().ifBlank { null }, sellerBio.trim().ifBlank { null }, sellerWebsite.trim().ifBlank { null }, sellerFacebook.trim().ifBlank { null }, sellerX.trim().ifBlank { null }, sellerLinkedIn.trim().ifBlank { null }, smeTin.trim().ifBlank { null })).user
                                    snackbarHost.showSnackbar("Profile updated.")
                                } catch (e: Exception) {
                                    snackbarHost.showSnackbar(e.localizedMessage ?: "Could not update profile.")
                                } finally { profileBusy = false }
                            }
                        }) { Text(if (profileBusy) "Saving..." else "Save profile") }
                        TextButton(onClick = {
                            scope.launch {
                                try { MarketApi.service.logout() } catch (_: Exception) { }
                                MarketApi.clearToken()
                                signedIn = false
                                tab = 0
                            }
                        }) { Text("Sign out") }
                    }
                    Text("Your disputes", Modifier.padding(horizontal = 20.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    when {
                        disputesLoading -> LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 20.dp))
                        myDisputes.isEmpty() -> Text("No disputes raised.", Modifier.padding(horizontal = 20.dp), color = colors.onSurfaceVariant, fontSize = 12.sp)
                        else -> LazyRow(
                            Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myDisputes, key = { it.id }) { dispute ->
                                Card(
                                    Modifier.width(230.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(dispute.order?.listing?.title ?: dispute.order?.order_number ?: "Order dispute", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(dispute.reason.replace('_', ' '), color = colors.onSurfaceVariant, fontSize = 12.sp)
                                        Text("Status: " + dispute.status.replace('_', ' '), color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        if (!dispute.resolution.isNullOrBlank()) {
                                            Text("Resolution: " + dispute.resolution, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text("Your orders", Modifier.padding(horizontal = 20.dp), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    if (ordersLoading) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else if (myOrders.isEmpty()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No orders yet.", color = colors.onSurfaceVariant) }
                    } else {
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(myOrders, key = { it.id }) { order ->
                                Card(colors = CardDefaults.cardColors(containerColor = colors.surface)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(order.listing?.title ?: order.order_number, fontWeight = FontWeight.Bold)
                                        Text(order.order_number, color = colors.onSurfaceVariant, fontSize = 12.sp)
                                        Text("K " + order.amount + " • " + order.status.replace('_', ' '), color = colors.primary)
                                        Text("Seller payout: " + (order.payout_status ?: "held").replaceFirstChar(Char::uppercase), color = colors.onSurfaceVariant, fontSize = 12.sp)
                                        order.tracking_reference?.let { Text("Tracking: $it", fontSize = 12.sp) }
                                        if (order.buyer_id == currentUser?.id && order.status == "awaiting_payment") {
                                            TextButton(onClick = { receiptOrder = order; receiptPicker.launch("*/*") }) { Text("Upload payment receipt") }
                                            TextButton(onClick = {
                                                scope.launch {
                                                    try { MarketApi.service.cancelOrder(order.id); savedRefresh++ }
                                                    catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not cancel order.") }
                                                }
                                            }) { Text("Cancel order") }
                                        }
                                        if (order.seller_id == currentUser?.id && order.status == "paid") {
                                            TextButton(onClick = { trackingOrder = order; trackingText = "" }) { Text("Mark shipped") }
                                        }
                                        if (order.buyer_id == currentUser?.id && order.status == "shipped") {
                                            Button(onClick = {
                                                scope.launch {
                                                    try { MarketApi.service.confirmReceived(order.id); myOrders = MarketApi.service.orders().data; snackbarHost.showSnackbar("Delivery confirmed. Seller payout is pending.") }
                                                    catch (e: Exception) { snackbarHost.showSnackbar(e.localizedMessage ?: "Could not confirm delivery.") }
                                                }
                                            }) { Text("Confirm item received") }
                                        }
                                        if (order.status in listOf("paid", "shipped", "delivered")) {
                                            TextButton(onClick = { disputeOrder = order; disputeReason = "not_as_described"; disputeDetails = "" }) { Text("Report an issue") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(padding).padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(listOf("Explore", "Saved items", "Sell on MarketPG", "Messages", "Your profile")[tab], fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(listOf("", "Sign in to see your saved listings.", "Seller tools are being connected to your account.", "Buyer and seller conversations will appear here.", "Sign in to manage your account.")[tab], color = colors.onSurfaceVariant)
                }
            }
        }

        if (showFilters) {
            AlertDialog(
                onDismissRequest = { showFilters = false },
                title = { Text("Filter & sort listings") },
                text = {
                    Column(
                        Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Price range (PGK)", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(minPrice, { minPrice = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Minimum") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(14.dp))
                            OutlinedTextField(maxPrice, { maxPrice = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Maximum") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(14.dp))
                        }
                        Text("Location", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(locationFilter, { locationFilter = it }, placeholder = { Text("Town / city") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        PngProvinceDropdown(filterProvince, { filterProvince = it }, label = "Province")
                        Text("Condition", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(conditionFilter == null, { conditionFilter = null }, label = { Text("Any") }, shape = RoundedCornerShape(50))
                            listOf("new", "used", "refurbished").forEach { value ->
                                FilterChip(conditionFilter == value, { conditionFilter = value }, label = { Text(value.replaceFirstChar(Char::uppercase)) }, shape = RoundedCornerShape(50))
                            }
                        }
                        Text("Sort by", fontWeight = FontWeight.SemiBold)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("newest" to "Newest first", "oldest" to "Oldest first", "price_asc" to "Price: low to high", "price_desc" to "Price: high to low").forEach { (value, label) ->
                                FilterChip(sortOrder == value, { sortOrder = value }, label = { Text(label) }, shape = RoundedCornerShape(50))
                            }
                        }
                        if (minPrice.isNotBlank() && minPrice.toDoubleOrNull() == null || maxPrice.isNotBlank() && maxPrice.toDoubleOrNull() == null) {
                            Text("Enter valid prices.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        } else if (minPrice.isNotBlank() && maxPrice.isNotBlank() && minPrice.toDouble() > maxPrice.toDouble()) {
                            Text("Minimum price cannot exceed maximum price.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = (minPrice.isBlank() || minPrice.toDoubleOrNull() != null) &&
                            (maxPrice.isBlank() || maxPrice.toDoubleOrNull() != null) &&
                            (minPrice.isBlank() || maxPrice.isBlank() || minPrice.toDouble() <= maxPrice.toDouble()),
                        onClick = { showFilters = false }
                    ) { Text("Apply filters") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        minPrice = ""; maxPrice = ""; locationFilter = ""
                        conditionFilter = null; sortOrder = "newest"
                    }) { Text("Reset") }
                }
            )
        }

        editingListing?.let { listing ->
            AlertDialog(
                onDismissRequest = { if (!editBusy) editingListing = null },
                title = { Text("Edit listing") },
                text = {
                    Column(
                        Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Saving changes to an active listing sends it back for moderation.", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        OutlinedTextField(editTitle, { editTitle = it }, label = { Text("Item title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(editDescription, { editDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(editPrice, { editPrice = it }, label = { Text("Price (PGK)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(editLocation, { editLocation = it }, label = { Text("Town / City") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                        PngProvinceDropdown(editProvince, { editProvince = it })
                        Text("Condition", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("new", "used", "refurbished").forEach { condition ->
                                FilterChip(editCondition == condition, { editCondition = condition }, label = { Text(condition.replaceFirstChar(Char::uppercase)) }, shape = RoundedCornerShape(50))
                            }
                        }
                        Text("Category", fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { category ->
                                FilterChip(editCategoryId == category.id, { editCategoryId = category.id }, label = { Text(category.name) }, shape = RoundedCornerShape(50))
                            }
                        }
                        editError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !editBusy && editTitle.isNotBlank() && editDescription.isNotBlank() &&
                            editPrice.toBigDecimalOrNull()?.signum()?.let { it >= 0 } == true && editLocation.isNotBlank() && editProvince.isNotBlank(),
                        onClick = {
                            scope.launch {
                                editBusy = true
                                editError = null
                                try {
                                    MarketApi.service.updateListing(
                                        listing.id,
                                        CreateListingRequest(editTitle.trim(), editDescription.trim(), editPrice.trim(), editCondition, listOf(editLocation.trim(), editProvince).filter { it.isNotBlank() }.joinToString(", "), editCategoryId, editLocation.trim(), editProvince)
                                    )
                                    myListings = MarketApi.service.myListings().data
                                    editingListing = null
                                    retry++
                                    snackbarHost.showSnackbar("Listing updated and submitted for review.")
                                } catch (e: Exception) {
                                    editError = e.localizedMessage ?: "Could not update listing."
                                } finally {
                                    editBusy = false
                                }
                            }
                        }
                    ) { Text(if (editBusy) "Saving..." else "Save changes") }
                },
                dismissButton = {
                    TextButton(enabled = !editBusy, onClick = { editingListing = null }) { Text("Cancel") }
                }
            )
        }
        disputeOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { disputeOrder = null },
                title = { Text("Report an order issue") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(order.order_number, color = colors.onSurfaceVariant)
                        listOf("item_not_received", "not_as_described", "damaged_item", "payment_issue", "other").forEach { reason ->
                            FilterChip(disputeReason == reason, { disputeReason = reason }, label = { Text(reason.replace('_', ' ')) }, shape = RoundedCornerShape(50))
                        }
                        OutlinedTextField(disputeDetails, { disputeDetails = it }, label = { Text("Explain the issue (at least 20 characters)") }, minLines = 3, shape = RoundedCornerShape(14.dp))
                    }
                },
                confirmButton = {
                    Button(enabled = disputeDetails.trim().length >= 20, onClick = {
                        scope.launch {
                            try {
                                MarketApi.service.raiseDispute(order.id, DisputeRequest(disputeReason, disputeDetails.trim()))
                                disputeOrder = null
                                savedRefresh++
                                snackbarHost.showSnackbar("Dispute submitted for review.")
                            } catch (e: Exception) {
                                snackbarHost.showSnackbar(e.localizedMessage ?: "Could not submit dispute.")
                            }
                        }
                    }) { Text("Submit dispute") }
                },
                dismissButton = { TextButton(onClick = { disputeOrder = null }) { Text("Cancel") } }
            )
        }
        replyingTo?.let { message ->
            AlertDialog(
                onDismissRequest = { replyingTo = null },
                title = { Text("Reply to " + (message.sender?.name ?: message.recipient?.name ?: "buyer/seller")) },
                text = { OutlinedTextField(replyText, { replyText = it }, label = { Text("Your message") }, minLines = 3, shape = RoundedCornerShape(14.dp)) },
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
                    putStringArrayListExtra("gallery_urls", ArrayList(listing.images.mapNotNull { secureMarketPgUrl(it.url) }))
                    putExtra("seller_rank", listing.user?.seller_rank ?: "New Seller")
                    putExtra("seller_id", listing.user?.id?.toLong() ?: 0L)
                    putExtra("seller_avatar", listing.user?.profile_photo_url ?: "")
                    putExtra("seller_bio", listing.user?.seller_bio ?: "")
                    putExtra("seller_website", listing.user?.seller_website ?: "")
                    putExtra("seller_facebook", listing.user?.seller_facebook_url ?: "")
                    putExtra("seller_x", listing.user?.seller_x_url ?: "")
                    putExtra("seller_linkedin", listing.user?.seller_linkedin_url ?: "")
                    putExtra("seller_sme_status", listing.user?.sme_status ?: "not_submitted")
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
        val imageUrl = secureMarketPgUrl(intent.getStringExtra("image_url")).orEmpty()
        val galleryUrls = intent.getStringArrayListExtra("gallery_urls").orEmpty().let { urls -> (listOf(imageUrl) + urls).mapNotNull { secureMarketPgUrl(it) }.distinct() }
        val sellerRank = intent.getStringExtra("seller_rank").orEmpty()
        val sellerId = intent.getLongExtra("seller_id", 0L)
        val sellerAvatar = intent.getStringExtra("seller_avatar").orEmpty()
        val sellerBioPublic = intent.getStringExtra("seller_bio").orEmpty()
        val sellerWebsitePublic = intent.getStringExtra("seller_website").orEmpty()
        val sellerFacebookPublic = intent.getStringExtra("seller_facebook").orEmpty()
        val sellerXPublic = intent.getStringExtra("seller_x").orEmpty()
        val sellerLinkedInPublic = intent.getStringExtra("seller_linkedin").orEmpty()
        val sellerSmeStatus = intent.getStringExtra("seller_sme_status").orEmpty()
        val listingId = intent.getLongExtra("listing_id", 0L)
        setContent {
            val scope = rememberCoroutineScope()
            var createdOrder by remember { mutableStateOf<OrderDto?>(null) }
            var paymentInstructions by remember { mutableStateOf("") }
            var checkoutBusy by remember { mutableStateOf(false) }
            var checkoutError by remember { mutableStateOf<String?>(null) }
            var sellerSheetOpen by remember { mutableStateOf(false) }
            var sellerItems by remember { mutableStateOf<List<ListingDto>>(emptyList()) }
            var sellerItemsLoading by remember { mutableStateOf(false) }
            var sellerItemsError by remember { mutableStateOf<String?>(null) }
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
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable {
                                sellerSheetOpen = true
                                if (sellerItems.isEmpty() && sellerId > 0) scope.launch {
                                    sellerItemsLoading = true
                                    sellerItemsError = null
                                    try { sellerItems = MarketApi.service.sellerListings(sellerId).data }
                                    catch (e: Exception) { sellerItemsError = e.localizedMessage ?: "Could not load this seller's items." }
                                    finally { sellerItemsLoading = false }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (sellerAvatar.isNotBlank()) {
                                    AsyncImage(model = secureMarketPgUrl(sellerAvatar), contentDescription = "Seller avatar", modifier = Modifier.size(52.dp))
                                } else {
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(52.dp)) {
                                        Box(contentAlignment = Alignment.Center) { Text(seller.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                                    }
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(seller, fontWeight = FontWeight.SemiBold)
                                    Text("Seller level: $sellerRank", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    Text("Tap to see this seller's other items", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                                Text("›", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
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
                if (sellerSheetOpen) {
                    ModalBottomSheet(
                        onDismissRequest = { sellerSheetOpen = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                if (sellerAvatar.isNotBlank()) AsyncImage(model = secureMarketPgUrl(sellerAvatar), contentDescription = "Seller avatar", modifier = Modifier.size(48.dp))
                                Column {
                                    Text(seller, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(sellerRank, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            if (sellerSmeStatus == "verified") {
                                Text("✓ Verified registered SME", color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                            } else if (sellerSmeStatus == "pending") {
                                Text("◷ SME verification pending", color = Color(0xFFD97706), style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Unverified seller", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            if (sellerBioPublic.isNotBlank()) Text(sellerBioPublic, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            val socialLinks = listOf(
                                "🌐 Website" to sellerWebsitePublic,
                                "f Facebook" to sellerFacebookPublic,
                                "𝕏 X" to sellerXPublic,
                                "in LinkedIn" to sellerLinkedInPublic
                            ).filter { it.second.startsWith("https://", ignoreCase = true) || it.second.startsWith("http://", ignoreCase = true) }
                            socialLinks.forEach { (label, url) ->
                                TextButton(onClick = { try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) { } }) {
                                    Text(label, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("More items from this seller", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            when {
                                sellerItemsLoading -> Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                sellerItemsError != null -> Text(sellerItemsError!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 18.dp))
                                sellerItems.isEmpty() -> Text("This seller has no other active items yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 18.dp))
                                else -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(sellerItems.filter { it.id != listingId }, key = { it.id }) { item ->
                                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                if (!item.imageUrl.isNullOrBlank()) AsyncImage(model = item.imageUrl, contentDescription = item.title, modifier = Modifier.size(72.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                                                    Text("K \${item.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                                    Text(item.location, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
