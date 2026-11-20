package pg.marketpg.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CategoryDto(val id: Int, val name: String, val slug: String)
data class CategoryPage(val data: List<CategoryDto> = emptyList())
data class SellerDto(val id: Int, val name: String)
data class AuthUser(val id: Long, val name: String, val email: String, val phone: String? = null, val location: String? = null, val account_type: String? = null)
data class ProfileUpdateRequest(val name: String, val phone: String?, val location: String?, val account_type: String)
data class DisputeRequest(val reason: String, val details: String)
data class DisputeListingDto(val title: String? = null)
data class DisputeOrderDto(val order_number: String? = null, val listing: DisputeListingDto? = null)
data class DisputeDto(
    val id: Long,
    val order_id: Long,
    val reason: String,
    val details: String,
    val status: String,
    val resolution: String? = null,
    val order: DisputeOrderDto? = null
)
data class DisputePaginator(val data: List<DisputeDto> = emptyList())
data class DisputePage(val data: DisputePaginator)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val password_confirmation: String)
data class AuthResponse(val message: String? = null, val user: AuthUser? = null, val token: String)
data class MeResponse(val user: AuthUser)
data class CreateOrderRequest(val payment_method: String = "cash_on_pickup", val buyer_note: String? = null)
data class CreateListingRequest(val title: String, val description: String, val price: String, val condition: String, val location: String, val category_id: Int)
data class OrderListingDto(val id: Long, val title: String, val location: String? = null)
data class OrderDto(val id: Long, val order_number: String, val buyer_id: Long, val seller_id: Long, val amount: String, val currency: String, val payment_method: String, val status: String, val listing: OrderListingDto? = null)
data class OrderResponse(val message: String? = null, val payment_instructions: String? = null, val data: OrderDto)
data class OrderPage(val data: List<OrderDto> = emptyList())
data class WishlistItemDto(val id: Long, val listing: ListingDto?)
data class WishlistPage(val data: List<WishlistItemDto> = emptyList())
data class MessageUserDto(val id: Long, val name: String)
data class MessageListingDto(val id: Long, val title: String)
data class MessageDto(
    val id: Long,
    val listing_id: Long,
    val sender_id: Long,
    val recipient_id: Long,
    val body: String,
    val sender: MessageUserDto? = null,
    val recipient: MessageUserDto? = null,
    val listing: MessageListingDto? = null
)
data class MessagePage(val data: List<MessageDto> = emptyList())
data class MessageRequest(val recipient_id: Long, val body: String)

data class ListingDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val price: String,
    val condition: String? = null,
    val location: String,
    @SerializedName("image_path") val imagePath: String? = null,
    val status: String? = null,
    val user: SellerDto? = null,
    @SerializedName("category_id") val categoryId: Int? = null
) {
    val imageUrl: String?
        get() = imagePath?.takeIf { it.isNotBlank() }?.let {
            "https://marketpg.onrender.com/storage/" + it.trimStart('/')
        }
}
data class ListingPage(val data: List<ListingDto> = emptyList(), val current_page: Int? = null, val last_page: Int? = null)
data class ListingDetailResponse(val data: ListingDto)

interface MarketService {
    @POST("auth/login") suspend fun login(@Body request: LoginRequest): AuthResponse
    @POST("auth/register") suspend fun register(@Body request: RegisterRequest): AuthResponse
    @GET("auth/me") suspend fun me(): MeResponse
    @retrofit2.http.PATCH("profile") suspend fun updateProfile(@Body request: ProfileUpdateRequest): MeResponse
    @POST("auth/logout") suspend fun logout(): Map<String, Any>
    @GET("wishlist") suspend fun wishlist(): WishlistPage
    @GET("messages") suspend fun messages(): MessagePage
    @POST("listings/{id}/messages") suspend fun sendMessage(@Path("id") id: Long, @Body request: MessageRequest): Map<String, Any>
    @POST("listings/{id}/wishlist") suspend fun addToWishlist(@Path("id") id: Long): Map<String, Any>
    @retrofit2.http.DELETE("listings/{id}/wishlist") suspend fun removeFromWishlist(@Path("id") id: Long): Map<String, Any>
    @POST("listings/{id}/orders") suspend fun createOrder(@Path("id") id: Long, @Body request: CreateOrderRequest): OrderResponse
    @GET("orders") suspend fun orders(): OrderPage
    @GET("disputes") suspend fun disputes(): DisputePage
    @POST("orders/{id}/disputes") suspend fun raiseDispute(@Path("id") id: Long, @Body request: DisputeRequest): Map<String, Any>
    @POST("orders/{id}/confirm-cash") suspend fun confirmCash(@Path("id") id: Long): Map<String, Any>
    @POST("orders/{id}/complete") suspend fun completeOrder(@Path("id") id: Long): Map<String, Any>
    @POST("orders/{id}/cancel") suspend fun cancelOrder(@Path("id") id: Long): Map<String, Any>
    @GET("categories") suspend fun categories(): CategoryPage
    @POST("listings") suspend fun createListing(@Body request: CreateListingRequest): ListingDetailResponse
    @retrofit2.http.PUT("listings/{id}") suspend fun updateListing(@Path("id") id: Long, @Body request: CreateListingRequest): ListingDetailResponse
    @GET("my/listings") suspend fun myListings(): ListingPage
    @retrofit2.http.DELETE("listings/{id}") suspend fun deleteListing(@Path("id") id: Long): Map<String, Any>
    @GET("listings") suspend fun listings(
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("location") location: String? = null,
        @Query("sort") sort: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("condition") condition: String? = null
    ): ListingPage
    @GET("listings/{id}") suspend fun listing(@Path("id") id: Long): ListingDetailResponse
}

object MarketApi {
    private const val BASE_URL = "https://marketpg.onrender.com/api/"
    @Volatile private var token: String? = null
    private var preferences: android.content.SharedPreferences? = null

    private const val KEY_ALIAS = "marketpg_session_key"

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences("marketpg_session", Context.MODE_PRIVATE)
        token = readEncryptedToken()
    }

    fun saveToken(value: String) {
        token = value
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
            val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            preferences?.edit()?.putString("api_token_ciphertext", encrypted)?.putString("api_token_iv", iv)?.remove("api_token")?.apply()
        } catch (_: Exception) {
            // Keep the session only in memory rather than falling back to plaintext storage.
            preferences?.edit()?.remove("api_token")?.remove("api_token_ciphertext")?.remove("api_token_iv")?.apply()
        }
    }

    fun clearToken() {
        token = null
        preferences?.edit()?.remove("api_token")?.remove("api_token_ciphertext")?.remove("api_token_iv")?.apply()
    }

    private fun readEncryptedToken(): String? {
        val prefs = preferences ?: return null
        val ciphertext = prefs.getString("api_token_ciphertext", null) ?: run {
            // Remove legacy plaintext tokens written by earlier app builds.
            prefs.edit().remove("api_token").apply()
            return null
        }
        return try {
            val iv = Base64.decode(prefs.getString("api_token_iv", null), Base64.NO_WRAP)
            val encrypted = Base64.decode(ciphertext, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (_: Exception) {
            clearToken()
            null
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    fun hasSession(): Boolean = !token.isNullOrBlank()

    val service: MarketService by lazy {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                token?.takeIf { it.isNotBlank() }?.let { header("Authorization", "Bearer " + it) }
            }.build()
            chain.proceed(request)
        }.build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketService::class.java)
    }
}