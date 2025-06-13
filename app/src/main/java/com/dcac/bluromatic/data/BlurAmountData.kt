package com.dcac.bluromatic.data

import com.dcac.bluromatic.R


// OBJET QUI FOURNIT UNE LISTE PRÉDÉFINIE DES NIVEAUX DE FLOU DISPONIBLES DANS L'APPLICATION
// CETTE LISTE EST UTILISÉE DANS L'IU POUR AFFICHER DES BOUTONS RADIO (RADIO BUTTONS) AU CHOIX DE L'UTILISATEUR

object BlurAmountData {

    // LISTE DES OPTIONS DE FLOU — CHAQUE ÉLÉMENT CONTIENT UN TEXTE (RESOURCE STRING) ET UNE VALEUR ENTIER (NIVEAU DE FLOU)
    val blurAmount = listOf(
        BlurAmount(
            blurAmountRes = R.string.blur_lv_1, // LIBELLÉ "FAIBLE" PAR EXEMPLE
            blurAmount = 1                      // NIVEAU DE FLOU ASSOCIÉ
        ),
        BlurAmount(
            blurAmountRes = R.string.blur_lv_2, // LIBELLÉ "MOYEN"
            blurAmount = 2
        ),
        BlurAmount(
            blurAmountRes = R.string.blur_lv_3, // LIBELLÉ "FORT"
            blurAmount = 3
        )
    )
}