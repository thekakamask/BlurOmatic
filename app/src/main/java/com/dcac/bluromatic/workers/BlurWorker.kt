package com.dcac.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dcac.bluromatic.DELAY_TIME_MILLIS
import com.dcac.bluromatic.KEY_BLUR_LEVEL
import com.dcac.bluromatic.KEY_IMAGE_URI
import com.dcac.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

private const val TAG = "BlurWorker"

// WORKER RESPONSABLE D’APPLIQUER UN FLOU À UNE IMAGE EN ARRIÈRE-PLAN
// CE WORKER UTILISE L’URI DE L’IMAGE + UN NIVEAU DE FLOU, ET RENVOIE UNE NOUVELLE IMAGE FLOUTÉE (FICHIER TEMPORAIRE)

class BlurWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // RÉCUPÈRE LES DONNÉES D’ENTRÉE : L’IMAGE À TRAITER ET LE NIVEAU DE FLOU À APPLIQUER
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        Log.d(TAG, "doWork() executing")

        // ENVOIE UNE NOTIFICATION POUR INFORMER L’UTILISATEUR
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS) // SIMULATION D’UNE LATENCE

            try {
                require(!resourceUri.isNullOrBlank()) {
                    applicationContext.getString(R.string.invalid_input_uri)
                }

                // CHARGE L’IMAGE D’ENTRÉE ET CRÉE UNE VERSION FLOUTÉE
                val picture = BitmapFactory.decodeStream(
                    applicationContext.contentResolver.openInputStream(resourceUri.toUri())
                )
                val blurredBitmap = blurBitmap(picture, blurLevel)

                // ENREGISTRE L’IMAGE TRAITÉE DANS UN FICHIER TEMPORAIRE
                val outputUri = writeBitmapToFile(applicationContext, blurredBitmap)

                // RENVOIE L’URI DE L’IMAGE TRAITÉE EN DONNÉE DE SORTIE
                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())
                Result.success(outputData)

            } catch (throwable: Throwable) {
                Log.e(TAG, applicationContext.getString(R.string.error_applying_blur), throwable)
                Result.failure()
            }
        }
    }
}