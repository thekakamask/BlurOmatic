package com.dcac.bluromatic.data

import com.dcac.bluromatic.R



// OBJET QUI CONTIENT LA LISTE DES NIVEAUX DE FLOU DISPONIBLES DANS L’APPLICATION
// CETTE LISTE EST UTILISÉE POUR GÉNÉRER LES BOUTONS RADIO DANS L’INTERFACE UTILISATEUR
// CHAQUE ÉLÉMENT COMBINE UN TEXTE À AFFICHER ET UNE VALEUR NUMÉRIQUE À TRANSMETTRE AU WORKER

object BlurAmountData {

    // LISTE DES OPTIONS PRÉDÉFINIES DE FLOU
    val blurAmount = listOf(
        BlurAmount(
            blurAmountRes = R.string.blur_lv_1, // TEXTE AFFICHÉ POUR CE NIVEAU (ex : "Faible")
            blurAmount = 1                      // NIVEAU DE FLOU TRANSMIS AU WORKER
        ),
        BlurAmount(
            blurAmountRes = R.string.blur_lv_2, // TEXTE "Moyen"
            blurAmount = 2
        ),
        BlurAmount(
            blurAmountRes = R.string.blur_lv_3, // TEXTE "Fort"
            blurAmount = 3
        )
    )
}