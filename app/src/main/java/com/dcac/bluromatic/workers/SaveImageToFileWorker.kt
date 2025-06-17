package com.dcac.bluromatic.workers

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dcac.bluromatic.DELAY_TIME_MILLIS
import com.dcac.bluromatic.KEY_IMAGE_URI
import com.dcac.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri

private const val TAG = "SaveImageToFileWorker"

// WORKER QUI SAUVEGARDE L’IMAGE FLOUTÉE DANS LA GALERIE (MediaStore)
// UTILISÉ EN FIN DE CHAÎNE POUR RENDRE LE RÉSULTAT ACCESSIBLE AU GRAND PUBLIC

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    // INFOS POUR L’ENREGISTREMENT DE L’IMAGE DANS MediaStore
    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // NOTIFIE QUE LA SAUVEGARDE COMMENCE
        makeStatusNotification(
            applicationContext.getString(R.string.saving_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS) // POUR EFFET VISUEL

            try {
                val resourceUri = inputData.getString(KEY_IMAGE_URI)

                // CHARGE L’IMAGE TRAITÉE DEPUIS L’URI
                val bitmap = BitmapFactory.decodeStream(
                    resourceUri?.let {
                        applicationContext.contentResolver.openInputStream(it.toUri())
                    }
                )

                // INSÈRE L’IMAGE DANS LA GALERIE
                val imageUrl = MediaStore.Images.Media.insertImage(
                    applicationContext.contentResolver,
                    bitmap,
                    title,
                    dateFormatter.format(Date())
                )

                // SI SUCCÈS → RETOURNE L’URI POUR QUE LE ViewModel PUISSE L’AFFICHER
                if (!imageUrl.isNullOrEmpty()) {
                    Result.success(workDataOf(KEY_IMAGE_URI to imageUrl))
                } else {
                    Log.e(TAG, applicationContext.getString(R.string.writing_to_mediaStore_failed))
                    Result.failure()
                }

            } catch (e: Exception) {
                Log.e(TAG, applicationContext.getString(R.string.error_saving_image), e)
                Result.failure()
            }
        }
    }
}