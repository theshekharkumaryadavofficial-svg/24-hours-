package com.example.plans

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val iconBitmap: ImageBitmap?
)

object InstalledAppsProvider {

    /**
     * Queries all user-launchable installed apps via Play-Policy-compliant
     * ACTION_MAIN + CATEGORY_LAUNCHER <queries> filter.
     */
    suspend fun loadInstalledLauncherApps(context: Context): List<InstalledAppInfo> =
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val ownPkg = context.packageName
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(launcherIntent, 0)
            }

            val seenPackages = HashSet<String>()
            val results = ArrayList<InstalledAppInfo>(resolved.size)

            for (info in resolved) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg == ownPkg || !seenPackages.add(pkg)) continue

                val label = runCatching { info.loadLabel(pm)?.toString()?.trim() }
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() }
                    ?: pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }

                val iconBmp = runCatching {
                    val drawable = info.loadIcon(pm)
                    drawableToSmallImageBitmap(drawable, sizePx = 72)
                }.getOrNull()

                results.add(
                    InstalledAppInfo(
                        packageName = pkg,
                        label = label,
                        iconBitmap = iconBmp
                    )
                )
            }

            results.sortBy { it.label.lowercase() }
            results
        }

    private fun drawableToSmallImageBitmap(drawable: Drawable, sizePx: Int): ImageBitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val scaled = Bitmap.createScaledBitmap(drawable.bitmap, sizePx, sizePx, true)
            return scaled.asImageBitmap()
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}
