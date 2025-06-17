package com.dcac.bluromatic.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dcac.bluromatic.CHANNEL_ID
import com.dcac.bluromatic.NOTIFICATION_ID
import com.dcac.bluromatic.NOTIFICATION_TITLE
import com.dcac.bluromatic.OUTPUT_PATH
import com.dcac.bluromatic.R
import com.dcac.bluromatic.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
import com.dcac.bluromatic.VERBOSE_NOTIFICATION_CHANNEL_NAME
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import androidx.core.graphics.scale

private const val TAG = "WorkerUtils"


// WORKERUTILS.KT — FONCTIONS UTILITAIRES UTILISÉES PAR LES WORKERS
// FOURNIT LES OUTILS POUR GÉRER LES NOTIFICATIONS, LE FLOUTAGE SIMPLIFIÉ D'IMAGES, ET LA SAUVEGARDE DANS UN FICHIER LOCAL

/**
 * AFFICHE UNE NOTIFICATION IMMÉDIATE POUR SIGNALER L’ÉTAT DU TRAITEMENT (EX: FLOUTAGE, SAUVEGARDE…)
 * DOIT ÊTRE APPELÉ AVEC LA PERMISSION DE NOTIFICATION (API 33+).
 *
 * @param message Texte du message à afficher dans la notification.
 * @param context Contexte d’application (souvent fourni par le Worker).
 */
@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun makeStatusNotification(message: String, context: Context) {
    // CRÉE LE CANAL DE NOTIFICATION SI NÉCESSAIRE (ANDROID 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    // CONSTRUIT ET ENVOIE LA NOTIFICATION
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0)) // PAS DE VIBRATION

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

/**
 * APPLIQUE UN "FAUX FLOU" EN RÉDUISANT PUIS RÉAGRANDISSANT UNE IMAGE.
 * CETTE MÉTHODE NE UTILISE PAS DE GAUSSIENNE, MAIS DONNE UN EFFET VISUEL RAPIDE ET LÉGER.
 *
 * @param bitmap Image source à flouter.
 * @param blurLevel Niveau de flou demandé (plus la valeur est grande, plus l’effet est fort).
 * @return Une nouvelle Bitmap modifiée avec effet de flou.
 */
@WorkerThread
fun blurBitmap(bitmap: Bitmap, blurLevel: Int): Bitmap {
    val input = bitmap.scale(
        bitmap.width / (blurLevel * 5),
        bitmap.height / (blurLevel * 5)
    )
    return input.scale(bitmap.width, bitmap.height)
}

/**
 * SAUVEGARDE UN BITMAP DANS UN FICHIER LOCAL .PNG DANS LE DOSSIER DE SORTIE DE L’APPLICATION.
 * CRÉÉ AUTOMATIQUEMENT UN NOM UNIQUE POUR CHAQUE IMAGE.
 *
 * @param applicationContext Contexte de l’application.
 * @param bitmap Image bitmap à écrire dans un fichier.
 * @return L’URI du fichier sauvegardé, utilisable par les autres Workers.
 * @throws FileNotFoundException en cas d’échec d’écriture.
 */
@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)

    if (!outputDir.exists()) outputDir.mkdirs()

    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null

    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out)
    } finally {
        try {
            out?.close()
        } catch (e: IOException) {
            Log.e(TAG, e.message.toString())
        }
    }

    return Uri.fromFile(outputFile)
}