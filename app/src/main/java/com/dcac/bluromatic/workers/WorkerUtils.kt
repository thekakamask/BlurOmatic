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


// UTILITAIRE POUR LES OPERATIONS COURANTES UTILISEES PAR LES WORKERS (NOTIFICATION, EFFET DE FLOU, SAUVEGARDE)

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun makeStatusNotification(message: String, context: Context) {
    // SI ANDROID 8.0+ : CREE UN CANAL DE NOTIFICATION (OBLIGATOIRE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // ENREGISTRE LE CANAL DE NOTIFICATION
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    // CONSTRUIT ET AFFICHE UNE NOTIFICATION IMMEDIATE
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

@WorkerThread
fun blurBitmap(bitmap: Bitmap, blurLevel: Int): Bitmap {
    // REDUIT LA TAILLE DE L'IMAGE POUR SIMULER UN FLOU (MÉTHODE SIMPLE ET RAPIDE)
    val input = bitmap.scale(bitmap.width / (blurLevel * 5), bitmap.height / (blurLevel * 5))
    // REAGRANDIT À LA TAILLE INITIALE POUR OBTENIR UN EFFET FLOU
    return input.scale(bitmap.width, bitmap.height)
}

@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    // GENERE UN NOM UNIQUE POUR LE FICHIER DE SORTIE
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)

    // CREE LE DOSSIER DE SORTIE SI N'EXISTE PAS
    if (!outputDir.exists()) outputDir.mkdirs()

    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null

    try {
        out = FileOutputStream(outputFile)
        // COMPRESSE ET ÉCRIT LE BITMAP AU FORMAT PNG
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out)
    } finally {
        // FERME LE FLUX DE SORTIE PROPREMENT
        out?.let {
            try {
                it.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    // RETOURNE L’URI DU FICHIER ÉCRIT
    return Uri.fromFile(outputFile)
}