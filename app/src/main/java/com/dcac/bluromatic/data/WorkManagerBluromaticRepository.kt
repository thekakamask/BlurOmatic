package com.dcac.bluromatic.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.asFlow
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

// CLASSE QUI IMPLÉMENTE L’INTERFACE BluromaticRepository ET GÈRE LE LANCEMENT DES WORKERS AVEC WorkManager
class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    // STOCKE L’URI DE L’IMAGE À TRAITER (DANS CETTE VERSION : UNE IMAGE EN DUR)
    private var imageUri: Uri = context.getImageUri()

    // RÉCUPÈRE L’INSTANCE CENTRALE DE WORKMANAGER POUR PLANIFIER DES CHAÎNES DE TÂCHES
    private val workManager = WorkManager.getInstance(context)

    // UN FLUX QUI OBSERVE LE STATUT DES TÂCHES ASSOCIÉES À LA BALISE TAG_OUTPUT.
    // IL CONVERTIT LiveData<List<WorkInfo>> EN Flow<WorkInfo?> EN PRENANT LE PREMIER ÉLÉMENT SI DISPONIBLE.
    // UTILISABLE POUR METTRE À JOUR L’UI EN TEMPS RÉEL (EX : AFFICHER LE RÉSULTAT FINAL DU FLOU).
    override val outputWorkInfo: Flow<WorkInfo> =
        workManager.getWorkInfosByTagLiveData(TAG_OUTPUT).asFlow().mapNotNull {
            if (it.isNotEmpty()) it.first() else null
        }

    // MÉTHODE PRINCIPALE POUR LANCER LE PROCESSUS DE FLOUTAGE + SAUVEGARDE DE L’IMAGE
    @SuppressLint("SuspiciousIndentation", "EnqueueWork")
    override fun applyBlur(blurLevel: Int) {

        // LOG POUR AIDER AU DEBUGGING DANS LOGCAT
        Log.d("BluromaticRepo", "applyBlur() called, launching BlurWorker")

        // 1. DÉMARRE UNE CHAÎNE DE TRAVAIL AVEC UNE TÂCHE DE NETTOYAGE
        // IL PEUT Y AVOIR PLUSIEURS CHAINES EN MEME TEMPS
        /*var continuation = workManager
            .beginWith(
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )*/

        // 1. DÉMARRE UNE CHAÎNE DE TRAVAIL UNIQUE AVEC UNE TÂCHE DE NETTOYAGE
        // UTILISE `beginUniqueWork` POUR S'ASSURER QU'UNE SEULE CHAÎNE DE TRAVAIL
        // PORTE LE NOM `IMAGE_MANIPULATION_WORK_NAME` À LA FOIS.
        // SI UNE CHAÎNE EXISTANTE PORTE CE NOM, ELLE SERA REMPLACÉE (POLITIQUE: REPLACE).
        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )

        // 2. CONSTRUIT LA TÂCHE QUI APPLIQUE LE FLOU
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()

        // 3. FOURNIT LES DONNÉES NÉCESSAIRES AU WORKER : URI + NIVEAU DE FLOU
        blurBuilder.setInputData(createInputDataForWorkRequest(blurLevel, imageUri))

        // 4. AJOUTE LA TÂCHE DE FLOUTAGE DANS LA CHAÎNE
        continuation = continuation.then(blurBuilder.build())

        // 5. CONSTRUIT LA TÂCHE DE SAUVEGARDE DANS LE MEDIACONTENT (Galerie)
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()

        // 6. AJOUTE CETTE TÂCHE À LA SUITE DE LA TÂCHE DE FLOUTAGE
        continuation = continuation.then(save)

        // 7. DÉMARRE EFFECTIVEMENT LA CHAÎNE DE TRAVAILS (NETTOYAGE → FLOUTAGE → SAUVEGARDE)
        continuation.enqueue()
    }

    // MÉTHODE VIDE POUR LE MOMENT — PERMETTRA PLUS TARD D’ANNULER LES WORKS EN COURS
    override fun cancelWork() {}

    // MÉTHODE UTILITAIRE QUI CRÉE L’OBJET `Data` ATTENDU PAR LE BlurWorker
    // CET OBJET CONTIENT L’IMAGE D’ENTRÉE ET LE NIVEAU DE FLOU À APPLIQUER
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()) // STOCKE L’URI EN TEXTE
        builder.putInt(KEY_BLUR_LEVEL, blurLevel)             // STOCKE LE NIVEAU DE FLOU
        return builder.build()
    }
}