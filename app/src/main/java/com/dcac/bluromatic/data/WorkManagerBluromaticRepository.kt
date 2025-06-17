package com.dcac.bluromatic.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dcac.bluromatic.IMAGE_MANIPULATION_WORK_NAME
import com.dcac.bluromatic.KEY_BLUR_LEVEL
import com.dcac.bluromatic.KEY_IMAGE_URI
import com.dcac.bluromatic.TAG_OUTPUT
import com.dcac.bluromatic.getImageUri
import com.dcac.bluromatic.workers.BlurWorker
import com.dcac.bluromatic.workers.CleanupWorker
import com.dcac.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

// CLASSE QUI IMPLÉMENTE BluromaticRepository EN UTILISANT WorkManager POUR PLANIFIER ET EXÉCUTER DES TÂCHES EN ARRIÈRE-PLAN
// RESPONSABLE DE CONSTRUIRE ET ENCHAÎNER LES TRAVAUX (Cleanup, Blur, Save), DE GÉRER LES CONTRAINTES ET D’EXPOSER LES RÉSULTATS

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    // URI DE L’IMAGE À TRAITER — FIXÉ ICI POUR SIMPLIFIER
    private var imageUri: Uri = context.getImageUri()

    // INSTANCE DE WorkManager UTILISÉE POUR PLANIFIER LES TÂCHES
    private val workManager = WorkManager.getInstance(context)

    // OBSERVE L'ÉTAT DE LA TÂCHE DE SAUVEGARDE GRÂCE À LA BALISE TAG_OUTPUT
    // CONVERTIT LE LiveData EN Flow POUR S’INTÉGRER À L’ARCHITECTURE MODERNE (Compose + Flow)
    override val outputWorkInfo: Flow<WorkInfo> =
        workManager.getWorkInfosByTagLiveData(TAG_OUTPUT).asFlow().mapNotNull {
            if (it.isNotEmpty()) it.first() else null
        }

    // LANCE UNE NOUVELLE CHAÎNE DE TÂCHES (NETTOYAGE → FLOUTAGE → SAUVEGARDE)
    // AVEC CONTRAINTE : NE PAS S’EXÉCUTER SI LA BATTERIE EST FAIBLE
    @SuppressLint("SuspiciousIndentation", "EnqueueWork")
    override fun applyBlur(blurLevel: Int) {

        Log.d("BluromaticRepo", "applyBlur() called, launching BlurWorker")

        // ÉTAPE 1 : CRÉATION D’UNE CHAÎNE UNIQUE DE TÂCHES, NOMMÉE POUR POUVOIR L’ANNULER OU LA REMPLACER
        var continuation = workManager.beginUniqueWork(
            IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        // ÉTAPE 2 : CONSTRUCTION DE LA TÂCHE DE FLOUTAGE AVEC CONTRAINTE DE BATTERIE
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputDataForWorkRequest(blurLevel, imageUri))
            .setConstraints(constraints)

        // ÉTAPE 3 : AJOUT DU WORKER DE FLOUTAGE À LA CHAÎNE
        continuation = continuation.then(blurBuilder.build())

        // ÉTAPE 4 : CONSTRUCTION DE LA TÂCHE DE SAUVEGARDE AVEC UNE BALISE POUR OBSERVER SON ÉTAT
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()

        // ÉTAPE 5 : AJOUT DU WORKER DE SAUVEGARDE À LA FIN DE LA CHAÎNE
        continuation = continuation.then(save)

        // ÉTAPE 6 : DÉMARRAGE EFFECTIF DE LA CHAÎNE DE TRAVAIL
        continuation.enqueue()
    }

    // ANNULATION DE LA CHAÎNE DE TÂCHES ACTUELLE GRÂCE À SON NOM UNIQUE
    override fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    // CRÉE L’OBJET Data CONTENANT LES INFOS À TRANSMETTRE AU BlurWorker (URI ET NIVEAU DE FLOU)
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()) // ENVOIE L’URI DE L’IMAGE
        builder.putInt(KEY_BLUR_LEVEL, blurLevel)             // ENVOIE LE NIVEAU DE FLOU
        return builder.build()
    }
}