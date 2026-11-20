package pg.marketpg.app

import android.content.Context
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
    val user: SellerDto? = null
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
    @POST("orders/{id}/confirm-cash") suspend fun confirmCash(@Path("id") id: Long): Map<String, Any>
    @POST("orders/{id}/complete") suspend fun completeOrder(@Path("id") id: Long): Map<String, Any>
    @POST("orders/{id}/cancel") suspend fun cancelOrder(@Path("id") id: Long): Map<String, Any>
    @GET("categories") suspend fun categories(): CategoryPage
    @POST("listings") suspend fun createListing(@Body request: CreateListingRequest): ListingDetailResponse
    @GET("my/listings") suspend fun myListings(): ListingPage
    @retrofit2.http.DELETE("listings/{id}") suspend fun deleteListing(@Path("id") id: Long): Map<String, Any>
    @GET("listings") suspend fun listings(
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("location") location: String? = null,
        @Query("sort") sort: String? = null
    ): ListingPage
    @GET("listings/{id}") suspend fun listing(@Path("id") id: Long): ListingDetailResponse
}

object MarketApi {
    private const val BASE_URL = "https://marketpg.onrender.com/api/"
    @Volatile private var token: String? = null
    private var preferences: android.content.SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences("marketpg_session", Context.MODE_PRIVATE)
        token = preferences?.getString("api_token", null)
    }

    fun saveToken(value: String) {
        token = value
        preferences?.edit()?.putString("api_token", value)?.apply()
    }

    fun clearToken() {
        token = null
        preferences?.edit()?.remove("api_token")?.apply()
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