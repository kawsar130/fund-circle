package com.fundcircle.app.data.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.fundcircle.app.domain.model.ReportType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages caching and FileProvider URI generation for on-device CSV reports.
 * Invariant 5: Offline-first; writes strictly to application internal cache directory.
 */
@Singleton
class ReportFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Saves CSV content to cacheDir/reports/FundCircle_<CircleName>_<ReportType>_<Timestamp>.csv
     */
    fun saveCsvToCache(circleName: String, reportType: ReportType, csvContent: String): File {
        val sanitizedName = circleName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val timestamp = System.currentTimeMillis()
        val fileName = "FundCircle_${sanitizedName}_${reportType.name}_${timestamp}.csv"

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val file = File(reportsDir, fileName)
        file.writeText(csvContent, Charsets.UTF_8)
        return file
    }

    /**
     * Obtains secure content:// URI via configured FileProvider for sharing.
     */
    fun getShareUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Builds Android Share Sheet Intent with FLAG_GRANT_READ_URI_PERMISSION.
     */
    fun createShareIntent(uri: Uri, fileName: String, chooserTitle: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
