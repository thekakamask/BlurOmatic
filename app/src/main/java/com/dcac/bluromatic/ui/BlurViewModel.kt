package com.dcac.bluromatic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dcac.bluromatic.BluromaticApplication
import com.dcac.bluromatic.data.BlurAmountData
import com.dcac.bluromatic.data.BluromaticRepository
import androidx.work.WorkInfo
import com.dcac.bluromatic.KEY_IMAGE_URI
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


// VIEWMODEL QUI GÈRE LA LOGIQUE DE L’IU POUR LE FLOUTAGE D’IMAGE
// OBSERVE L’ÉTAT DES WORKS ET FOURNIT DES ACTIONS POUR LANCER OU ANNULER LE TRAITEMENT

class BlurViewModel(private val bluromaticRepository: BluromaticRepository) : ViewModel() {

    // LISTE DES NIVEAUX DE FLOU DISPONIBLES POUR LES RADIO BUTTONS
    internal val blurAmount = BlurAmountData.blurAmount

    // UI STATE OBSERVÉ PAR L'IU : EN CHARGEMENT, TERMINÉ OU PAR DÉFAUT
    val blurUiState: StateFlow<BlurUiState> = bluromaticRepository.outputWorkInfo
        .map { info ->
            val outputImageUri = info.outputData.getString(KEY_IMAGE_URI)
            when {
                info.state.isFinished && !outputImageUri.isNullOrEmpty() -> {
                    // SI LE WORK EST TERMINÉ AVEC UN RÉSULTAT, AFFICHE L'IMAGE FINALE
                    BlurUiState.Complete(outputUri = outputImageUri)
                }
                info.state == WorkInfo.State.CANCELLED -> {
                    // SI ANNULÉ, REVIENT À L’ÉTAT INITIAL
                    BlurUiState.Default
                }
                else -> BlurUiState.Loading // EN COURS DE TRAITEMENT
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BlurUiState.Default
        )

    // LANCE UNE NOUVELLE CHAÎNE DE TRAITEMENT FLOUTAGE
    fun applyBlur(blurLevel: Int) {
        bluromaticRepository.applyBlur(blurLevel)
    }

    // ANNULER LA CHAÎNE DE WORKS EN COURS
    fun cancelWork() {
        bluromaticRepository.cancelWork()
    }

    // FACTORY POUR INJECTER LE REPOSITORY DEPUIS L’APPLICATION
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val bluromaticRepository =
                    (this[APPLICATION_KEY] as BluromaticApplication).container.bluromaticRepository
                BlurViewModel(
                    bluromaticRepository = bluromaticRepository
                )
            }
        }
    }
}

// ÉTATS POSSIBLES DE L’INTERFACE
sealed interface BlurUiState {
    object Default : BlurUiState        // ÉTAT INITIAL
    object Loading : BlurUiState        // TRAITEMENT EN COURS
    data class Complete(val outputUri: String) : BlurUiState // TRAITEMENT TERMINÉ AVEC URI DE L’IMAGE
}