package com.example.agenda.ui.add

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agenda.R
import com.example.agenda.data.model.Cours
import com.example.agenda.data.model.JourSemaine
import com.example.agenda.data.model.TypeCours
import com.example.agenda.databinding.ActivityAddCoursBinding
import com.example.agenda.viewmodel.CoursViewModel
import kotlinx.coroutines.launch
import java.util.*

/**
 * Activité pour ajouter ou modifier un cours
 */
class AddCoursActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCoursBinding
    private val viewModel: CoursViewModel by viewModels()
    private var coursToEdit: Cours? = null
    private var heureDebut: String = ""
    private var heureFin: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCoursBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSpinners()
        setupTimePickers()
        setupSaveButton()

        // Vérifie si on édite un cours existant
        checkEditMode()
    }

    /**
     * Configure la toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Ajouter un cours"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Configure les spinners (jour et type de cours)
     */
    private fun setupSpinners() {
        // Spinner pour le jour
        val joursAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            JourSemaine.getAllJours()
        )

        binding.spinnerJour.setAdapter(joursAdapter)


        // Spinner pour le type de cours
        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listOf("CM", "TD", "TP", "Autre")
        )

        binding.spinnerTypeCours.setAdapter(typeAdapter)

    }

    /**
     * Configure les sélecteurs de temps
     */
    private fun setupTimePickers() {
        val calendar = Calendar.getInstance()

        // Heure de début
        binding.btnSelectHeureDebut.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    heureDebut = String.format("%02d:%02d", hourOfDay, minute)
                    binding.btnSelectHeureDebut.text = heureDebut
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Heure de fin
        binding.btnSelectHeureFin.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    heureFin = String.format("%02d:%02d", hourOfDay, minute)
                    binding.btnSelectHeureFin.text = heureFin
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    /**
     * Configure le bouton d'enregistrement
     */
    private fun setupSaveButton() {
        binding.btnSaveCours.setOnClickListener {
            saveCours()
        }
    }

    /**
     * Vérifie si on édite un cours existant
     */
    private fun checkEditMode() {
        val coursId = intent.getIntExtra("COURS_ID", -1)
        if (coursId != -1) {
            supportActionBar?.title = "Modifier le cours"
            binding.btnSaveCours.text = "Mettre à jour"

            lifecycleScope.launch {
                // Utilise une variable locale au lieu de réaffecter coursToEdit
                val cours = viewModel.getCoursById(coursId)
                cours?.let {
                    coursToEdit = it // Pas de problème ici car c'est dans le même scope
                    fillFormWithCours(it)
                }
            }
        }
    }

    /**
     * Remplit le formulaire avec les données d'un cours existant
     */
    private fun fillFormWithCours(cours: Cours) {
        binding.etNomCours.setText(cours.nomCours ?: "")
        if (!cours.nomCours.isNullOrEmpty()) {
            binding.etNomCours.setSelection(cours.nomCours.length)
        }

        binding.etProfesseur.setText(cours.professeur ?: "")
        if (!cours.professeur.isNullOrEmpty()) {
            binding.etProfesseur.setSelection(cours.professeur.length)
        }

        binding.etSalle.setText(cours.salle ?: "")
        if (!cours.salle.isNullOrEmpty()) {
            binding.etSalle.setSelection(cours.salle.length)
        }

        val jourPosition = JourSemaine.getAllJours().indexOf(cours.jour)
        if (jourPosition >= 0) binding.spinnerJour.setSelection(jourPosition)

        val typePosition = TypeCours.values().indexOf(cours.typeCours)
        if (typePosition >= 0) binding.spinnerTypeCours.setSelection(typePosition)

        heureDebut = cours.heureDebut
        heureFin = cours.heureFin
        binding.btnSelectHeureDebut.text = heureDebut
        binding.btnSelectHeureFin.text = heureFin

        binding.switchNotification.isChecked = cours.notificationActive
    }



    /**
     * Sauvegarde le cours (ajout ou modification)
     */
    private fun saveCours() {
        val nomCours = binding.etNomCours.text.toString().trim()
        val professeur = binding.etProfesseur.text.toString().trim()
        val salle = binding.etSalle.text.toString().trim()
        val jour = binding.spinnerJour.text.toString()
        val typeCours = TypeCours.fromString(binding.spinnerTypeCours.text.toString())
        val notificationActive = binding.switchNotification.isChecked

        // Validation
        val (isValid, errorMessage) = viewModel.validateCours(
            nomCours, professeur, salle, jour, heureDebut, heureFin
        )

        if (!isValid) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            return
        }

        // Crée ou met à jour le cours
        val cours = Cours(
            id = coursToEdit?.id ?: 0,
            nomCours = nomCours,
            professeur = professeur,
            salle = salle,
            jour = jour,
            heureDebut = heureDebut,
            heureFin = heureFin,
            typeCours = typeCours,
            couleur = getColorForType(typeCours),
            notificationActive = notificationActive
        )

        if (coursToEdit != null) {
            viewModel.update(cours)
        } else {
            viewModel.insert(cours)
        }

        setResult(RESULT_OK)
        finish()
    }

    /**
     * Retourne une couleur selon le type de cours
     */
    private fun getColorForType(type: TypeCours): String {
        return when (type) {
            TypeCours.CM -> "#2196F3"  // Bleu
            TypeCours.TD -> "#4CAF50"  // Vert
            TypeCours.TP -> "#FF9800"  // Orange
            TypeCours.AUTRE -> "#9C27B0" // Violet
        }
    }
}