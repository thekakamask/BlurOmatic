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

// WORKER QUI SAUVEGARDE UNE IMAGE FLOUTÉE DANS LA GALERIE DU TÉLÉPHONE (MediaStore)

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    // TITRE DE L’IMAGE ET FORMAT POUR LA DATE DE SAUVEGARDE
    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    // MÉTHODE PRINCIPALE EXÉCUTÉE PAR WORKMANAGER
    // ELLE RÉCUPÈRE L’IMAGE TRAITÉE ET L’INSÈRE DANS LA GALERIE
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // AFFICHE UNE NOTIFICATION POUR INDIQUER QUE LA SAUVEGARDE COMMENCE
        makeStatusNotification(
            applicationContext.resources.getString(R.string.saving_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            // DÉLAI POUR SIMULER UN TRAITEMENT
            delay(DELAY_TIME_MILLIS)

            val resolver = applicationContext.contentResolver

            return@withContext try {
                // RÉCUPÉRATION DE L’IMAGE DEPUIS L’URI PASSÉE EN ENTRÉE
                val resourceUri = inputData.getString(KEY_IMAGE_URI)
                val bitmap = BitmapFactory.decodeStream(
                    resourceUri?.let { resolver.openInputStream(it.toUri()) }
                )

                // SAUVEGARDE DANS LE MediaStore (GALERIE)
                val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, title, dateFormatter.format(Date())
                )

                // SI L’IMAGE EST BIEN INSÉRÉE, RETOURNE L’URI EN SORTIE
                if (!imageUrl.isNullOrEmpty()) {
                    val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                    Result.success(output)
                } else {
                    // ERREUR DE SAUVEGARDE
                    Log.e(TAG, applicationContext.getString(R.string.writing_to_mediaStore_failed))
                    Result.failure()
                }
            } catch (exception: Exception) {
                // EXCEPTION GÉNÉRALE : LOG + ÉCHEC
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_saving_image),
                    exception
                )
                Result.failure()
            }
        }
    }
}