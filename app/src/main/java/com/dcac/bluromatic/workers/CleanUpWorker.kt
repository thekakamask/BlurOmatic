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

// WORKER QUI SUPPRIME LES IMAGES TEMPORAIRES (.PNG) APRÈS TRAITEMENT

class CleanupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    // MÉTHODE PRINCIPALE EXÉCUTÉE PAR WORKMANAGER
    // ELLE SUPPRIME LES FICHIERS PNG DANS LE DOSSIER TEMPORAIRE
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // NOTIFIE L'UTILISATEUR QUE LE NETTOYAGE COMMENCE
        makeStatusNotification(
            applicationContext.resources.getString(R.string.cleaning_up_files),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            // SIMULE UN DÉLAI POUR VISUALISATION
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                // ACCÈDE AU DOSSIER DE SORTIE DÉFINI
                val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)

                // SI LE DOSSIER EXISTE, SUPPRIME CHAQUE FICHIER .PNG
                if (outputDirectory.exists()) {
                    val entries = outputDirectory.listFiles()
                    if (entries != null) {
                        for (entry in entries) {
                            val name = entry.name
                            if (name.isNotEmpty() && name.endsWith(".png")) {
                                val deleted = entry.delete()
                                Log.i(TAG, "Deleted $name - $deleted")
                            }
                        }
                    }
                }

                // RETOURNE SUCCÈS SI TOUT EST OK
                Result.success()

            } catch (exception: Exception) {
                // LOG EN CAS D'ERREUR
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_cleaning_file),
                    exception
                )
                Result.failure()
            }
        }
    }
}