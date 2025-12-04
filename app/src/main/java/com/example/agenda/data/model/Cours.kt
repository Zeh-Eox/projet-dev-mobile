package com.example.agenda.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Entité représentant un cours ou une activité dans l'agenda
 * Implémente Serializable pour faciliter le passage entre activités
 */
@Entity(tableName = "cours_table")
data class Cours(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    val nomCours: String,           // Nom du cours/activité
    val professeur: String,          // Nom du professeur
    val salle: String,               // Numéro/nom de la salle
    val jour: String,                // Jour de la semaine (Lundi, Mardi, etc.)
    val heureDebut: String,          // Format "HH:mm"
    val heureFin: String,            // Format "HH:mm"
    val typeCours: TypeCours,        // CM, TD, TP, Autre
    val couleur: String = "#2196F3", // Couleur pour l'affichage (format hexadécimal)
    val notificationActive: Boolean = false  // Si une notification est activée pour ce cours
) : Serializable

/**
 * Enum représentant les différents types de cours
 */
enum class TypeCours {
    CM,    // Cours Magistral
    TD,    // Travaux Dirigés
    TP,    // Travaux Pratiques
    AUTRE; // Autres activités

    companion object {
        fun fromString(value: String): TypeCours {
            return when(value.uppercase()) {
                "CM" -> CM
                "TD" -> TD
                "TP" -> TP
                else -> AUTRE
            }
        }
    }

    override fun toString(): String {
        return when(this) {
            CM -> "CM"
            TD -> "TD"
            TP -> "TP"
            AUTRE -> "Autre"
        }
    }
}

/**
 * Enum pour les jours de la semaine
 */
enum class JourSemaine(val displayName: String) {
    LUNDI("Lundi"),
    MARDI("Mardi"),
    MERCREDI("Mercredi"),
    JEUDI("Jeudi"),
    VENDREDI("Vendredi"),
    SAMEDI("Samedi"),
    DIMANCHE("Dimanche");

    companion object {
        fun fromString(value: String): JourSemaine {
            return values().find {
                it.displayName.equals(value, ignoreCase = true)
            } ?: LUNDI
        }

        fun getAllJours(): List<String> {
            return values().map { it.displayName }
        }
    }
}