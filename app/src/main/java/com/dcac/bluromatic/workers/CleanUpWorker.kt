package com.dcac.bluromatic.workers

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dcac.bluromatic.DELAY_TIME_MILLIS
import com.dcac.bluromatic.OUTPUT_PATH
import com.dcac.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File


private const val TAG = "CleanupWorker"

// WORKER QUI SUPPRIME LES FICHIERS TEMPORAIRES (.PNG) DANS LE DOSSIER DE SORTIE
// CE NETTOYAGE EST EFFECTUÉ AVANT CHAQUE TRAITEMENT POUR ÉVITER LES FICHIERS INUTILES

class CleanupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // NOTIFICATION DE DÉMARRAGE
        makeStatusNotification(
            applicationContext.getString(R.string.cleaning_up_files),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS) // SIMULATION D’UN TEMPS DE TRAITEMENT

            try {
                // ACCÈS AU DOSSIER DE SORTIE
                val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)

                // SUPPRESSION DE CHAQUE FICHIER .PNG DÉJÀ PRÉSENT
                if (outputDirectory.exists()) {
                    outputDirectory.listFiles()?.forEach { file ->
                        if (file.name.endsWith(".png")) {
                            val deleted = file.delete()
                            Log.i(TAG, "Deleted ${file.name} - $deleted")
                        }
                    }
                }

                Result.success()

            } catch (e: Exception) {
                Log.e(TAG, applicationContext.getString(R.string.error_cleaning_file), e)
                Result.failure()
            }
        }
    }
}