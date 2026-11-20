package pg.marketpg.app

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

@Composable
fun MarketPgBannerAd(adUnitId: String) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val adView = remember(adUnitId, activity) { AdView(activity) }

    LaunchedEffect(adUnitId, adView) {
        val density = activity.resources.displayMetrics.density
        val widthDp = (activity.resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(320)
        val size = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, widthDp)
        val request = BannerAdRequest.Builder(adUnitId, size).build()
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) = Unit
            override fun onAdFailedToLoad(adError: LoadAdError) = Unit
        })
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { adView },
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        )
    }
}
