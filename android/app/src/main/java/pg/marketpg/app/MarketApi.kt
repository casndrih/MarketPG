package pg.marketpg.app

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String
)

data class CategoryPage(
    val data: List<CategoryDto> = emptyList()
)

data class SellerDto(
    val id: Int,
    val name: String
)

data class ListingDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    val price: String,
    val condition: String? = null,
    val location: String,
    @SerializedName("image_path") val imagePath: String? = null,
    val user: SellerDto? = null
) {
    val imageUrl: String?
        get() = imagePath?.takeIf { it.isNotBlank() }?.let {
            "https://marketpg.onrender.com/storage/" + it.trimStart('/')
        }
}

data class ListingPage(
    val data: List<ListingDto> = emptyList(),
    val current_page: Int? = null,
    val last_page: Int? = null
)

data class ListingDetailResponse(
    val data: ListingDto
)

interface MarketService {
    @GET("categories")
    suspend fun categories(): CategoryPage

    @GET("listings")
    suspend fun listings(
        @Query("q") query: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("location") location: String? = null,
        @Query("sort") sort: String? = null
    ): ListingPage

    @GET("listings/{id}")
    suspend fun listing(@Path("id") id: Long): ListingDetailResponse
}

object MarketApi {
    private const val BASE_URL = "https://marketpg.onrender.com/api/"

    val service: MarketService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketService::class.java)
    }
}
