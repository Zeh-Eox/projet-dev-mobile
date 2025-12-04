package com.example.agenda.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.agenda.R
import com.example.agenda.data.model.Cours

/**
 * Adapter pour afficher la liste des cours dans un RecyclerView
 * Utilise ListAdapter pour une gestion efficace des mises à jour
 */
class CoursAdapter(
    private val onItemClick: (Cours) -> Unit,
    private val onItemLongClick: (Cours) -> Unit
) : ListAdapter<Cours, CoursAdapter.CoursViewHolder>(CoursDiffCallback()) {

    /**
     * ViewHolder pour chaque élément de la liste
     */
    inner class CoursViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardViewCours)
        private val tvNomCours: TextView = itemView.findViewById(R.id.tvNomCours)
        private val tvProfesseur: TextView = itemView.findViewById(R.id.tvProfesseur)
        private val tvSalle: TextView = itemView.findViewById(R.id.tvSalle)
        private val tvJour: TextView = itemView.findViewById(R.id.tvJour)
        private val tvHoraire: TextView = itemView.findViewById(R.id.tvHoraire)
        private val tvTypeCours: TextView = itemView.findViewById(R.id.tvTypeCours)
        private val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)

        /**
         * Lie les données d'un cours aux vues
         */
        fun bind(cours: Cours) {
            tvNomCours.text = cours.nomCours
            tvProfesseur.text = "Prof. ${cours.professeur}"
            tvSalle.text = "Salle: ${cours.salle}"
            tvJour.text = cours.jour
            tvHoraire.text = "${cours.heureDebut} - ${cours.heureFin}"
            tvTypeCours.text = cours.typeCours.toString()

            // Applique la couleur personnalisée
            try {
                viewColorIndicator.setBackgroundColor(Color.parseColor(cours.couleur))
            } catch (e: IllegalArgumentException) {
                viewColorIndicator.setBackgroundColor(Color.parseColor("#2196F3"))
            }

            // Style du badge de type de cours
            when (cours.typeCours) {
                com.example.agenda.data.model.TypeCours.CM -> {
                    tvTypeCours.setBackgroundResource(R.drawable.badge_cm)
                }
                com.example.agenda.data.model.TypeCours.TD -> {
                    tvTypeCours.setBackgroundResource(R.drawable.badge_td)
                }
                com.example.agenda.data.model.TypeCours.TP -> {
                    tvTypeCours.setBackgroundResource(R.drawable.badge_tp)
                }
                com.example.agenda.data.model.TypeCours.AUTRE -> {
                    tvTypeCours.setBackgroundResource(R.drawable.badge_autre)
                }
            }

            // Gestion des clics
            cardView.setOnClickListener {
                onItemClick(cours)
            }

            cardView.setOnLongClickListener {
                onItemLongClick(cours)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cours, parent, false)
        return CoursViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoursViewHolder, position: Int) {
        val cours = getItem(position)
        holder.bind(cours)
    }

    /**
     * DiffUtil pour calculer efficacement les différences entre deux listes
     * Évite de recharger toute la liste à chaque mise à jour
     */
    private class CoursDiffCallback : DiffUtil.ItemCallback<Cours>() {
        override fun areItemsTheSame(oldItem: Cours, newItem: Cours): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cours, newItem: Cours): Boolean {
            return oldItem == newItem
        }
    }
}