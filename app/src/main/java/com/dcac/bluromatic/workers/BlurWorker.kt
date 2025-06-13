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

// WORKER QUI APPLIQUE UN FLOU À UNE IMAGE EN ARRIÈRE-PLAN AVEC WORKMANAGER
// CE WORKER EST LANCÉ PAR LE REPOSITORY LORSQU'ON DEMANDE À TRAITER UNE IMAGE

class BlurWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    // MÉTHODE PRINCIPALE EXÉCUTÉE LORSQUE LE WORKER EST LANCÉ
    // ELLE FAIT TOUT : CHARGEMENT DE L'IMAGE, APPLICATION DU FLOU, SAUVEGARDE ET RETOUR DE L'URI
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        // RÉCUPÉRATION DE L’URI DE L’IMAGE ET DU NIVEAU DE FLOU DEPUIS LES DONNÉES D’ENTRÉE
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        Log.d(TAG, "doWork() executing")

        // AFFICHE UNE NOTIFICATION POUR SIGNALER QUE LE FLOUTAGE COMMENCE
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        // TOUT LE TRAITEMENT S’EFFECTUE DANS UN CONTEXTE I/O POUR NE PAS BLOQUER LE THREAD PRINCIPAL
        return withContext(Dispatchers.IO) {

            // SIMULE UN DÉLAI POUR VISUALISER LE TRAITEMENT
            delay(DELAY_TIME_MILLIS)

            return@withContext try {

                // VÉRIFIE QUE L’URI DE L’IMAGE EST VALIDE
                require(!resourceUri.isNullOrBlank()) {
                    val errorMessage = applicationContext.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                // OUVRE UN FLUX VERS L’IMAGE À L’AIDE DU CONTENT RESOLVER
                val resolver = applicationContext.contentResolver
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(resourceUri.toUri())
                )

                // APPLIQUE L’EFFET DE FLOU SELON LE NIVEAU DEMANDÉ
                val output = blurBitmap(picture, blurLevel)

                // SAUVEGARDE L’IMAGE FLOUTÉE DANS UN FICHIER TEMPORAIRE ET RÉCUPÈRE SON URI
                val outputUri = writeBitmapToFile(applicationContext, output)

                // PRÉPARE LES DONNÉES DE SORTIE AVEC L’URI DE L’IMAGE TRAITÉE
                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                // RETOURNE UN SUCCÈS AVEC LES DONNÉES DE SORTIE POUR QUE L’UI PUISSE Y ACCÉDER
                Result.success(outputData)

            } catch (throwable: Throwable) {
                // EN CAS D’ERREUR, ENVOIE UN LOG ET RETOURNE UN ÉCHEC
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )

                Result.failure()
            }
        }
    }
}