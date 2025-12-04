package com.example.agenda.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.agenda.R
import com.example.agenda.data.model.Cours
import com.example.agenda.databinding.ActivityDetailCoursBinding
import com.example.agenda.ui.add.AddCoursActivity
import com.example.agenda.viewmodel.CoursViewModel
import kotlinx.coroutines.launch

/**
 * Activité affichant les détails d'un cours
 * Permet la modification et la suppression
 */
class DetailCoursActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailCoursBinding
    private val viewModel: CoursViewModel by viewModels()
    private var currentCours: Cours? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCoursBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadCoursDetails()
        setupButtons()
    }

    /**
     * Configure la toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Détails du cours"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * Charge les détails du cours
     */
    private fun loadCoursDetails() {
        val coursId = intent.getIntExtra("COURS_ID", -1)
        if (coursId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            currentCours = viewModel.getCoursById(coursId)
            currentCours?.let { displayCoursDetails(it) }
        }
    }

    /**
     * Affiche les détails du cours dans l'UI
     */
    private fun displayCoursDetails(cours: Cours) {
        binding.apply {
            // Titre et informations principales
            tvNomCoursDetail.text = cours.nomCours
            tvProfesseurDetail.text = cours.professeur
            tvSalleDetail.text = cours.salle
            tvJourDetail.text = cours.jour
            tvHoraireDetail.text = "${cours.heureDebut} - ${cours.heureFin}"
            tvTypeCoursDetail.text = cours.typeCours.toString()

            // Applique la couleur
            try {
                cardViewDetail.setCardBackgroundColor(Color.parseColor(cours.couleur))
            } catch (e: IllegalArgumentException) {
                cardViewDetail.setCardBackgroundColor(Color.parseColor("#2196F3"))
            }

            // Icône de notification
            if (cours.notificationActive) {
                ivNotificationIcon.setImageResource(android.R.drawable.ic_lock_idle_alarm)
                tvNotificationStatus.text = "Notification activée"
            } else {
                ivNotificationIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                tvNotificationStatus.text = "Notification désactivée"
            }

            // Description du type de cours
            tvDescriptionType.text = when (cours.typeCours) {
                com.example.agenda.data.model.TypeCours.CM ->
                    "Cours Magistral - Enseignement théorique en amphithéâtre"
                com.example.agenda.data.model.TypeCours.TD ->
                    "Travaux Dirigés - Exercices et applications du cours"
                com.example.agenda.data.model.TypeCours.TP ->
                    "Travaux Pratiques - Mise en pratique et expérimentation"
                com.example.agenda.data.model.TypeCours.AUTRE ->
                    "Autre activité académique"
            }
        }
    }

    /**
     * Configure les boutons d'action
     */
    private fun setupButtons() {
        // Bouton Modifier
        binding.btnEdit.setOnClickListener {
            currentCours?.let { cours ->
                val intent = Intent(this, AddCoursActivity::class.java)
                intent.putExtra("COURS_ID", cours.id)
                startActivityForResult(intent, REQUEST_EDIT)
            }
        }

        // Bouton Supprimer
        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    /**
     * Affiche un dialogue de confirmation de suppression
     */
    private fun showDeleteDialog() {
        currentCours?.let { cours ->
            AlertDialog.Builder(this)
                .setTitle("Supprimer le cours")
                .setMessage("Voulez-vous vraiment supprimer \"${cours.nomCours}\" ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.delete(cours)
                    setResult(RESULT_OK)
                    finish()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareCours()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Partage les informations du cours
     */
    private fun shareCours() {
        currentCours?.let { cours ->
            val shareText = """
                📚 ${cours.nomCours}
                👨‍🏫 Professeur: ${cours.professeur}
                📍 Salle: ${cours.salle}
                📅 ${cours.jour}
                🕐 ${cours.heureDebut} - ${cours.heureFin}
                📝 Type: ${cours.typeCours}
            """.trimIndent()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Partager le cours"))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT && resultCode == RESULT_OK) {
            // Recharge les détails après modification
            loadCoursDetails()
        }
    }

    companion object {
        private const val REQUEST_EDIT = 100
    }
}