package com.example.agenda.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agenda.R
import com.example.agenda.data.model.Cours
import com.example.agenda.data.model.JourSemaine
import com.example.agenda.data.model.TypeCours
import com.example.agenda.databinding.ActivityMainBinding
import com.example.agenda.ui.add.AddCoursActivity
import com.example.agenda.ui.detail.DetailCoursActivity
import com.example.agenda.viewmodel.CoursViewModel
import com.google.android.material.chip.Chip

/**
 * Activité principale affichant la liste des cours
 * Permet la navigation, recherche et filtrage
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CoursViewModel by viewModels()
    private lateinit var adapter: CoursAdapter

    companion object {
        const val REQUEST_ADD_COURS = 1
        const val REQUEST_EDIT_COURS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        setupChipFilters()
        observeViewModel()
    }

    /**
     * Configure la toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Mon Agenda"
    }

    /**
     * Configure le RecyclerView avec son adapter
     */
    private fun setupRecyclerView() {
        adapter = CoursAdapter(
            onItemClick = { cours ->
                // Navigation vers les détails
                val intent = Intent(this, DetailCoursActivity::class.java)
                intent.putExtra("COURS_ID", cours.id)
                startActivityForResult(intent, REQUEST_EDIT_COURS)
            },
            onItemLongClick = { cours ->
                // Affiche un dialogue de confirmation pour supprimer
                showDeleteDialog(cours)
            }
        )

        binding.recyclerViewCours.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    /**
     * Configure le bouton flottant d'ajout
     */
    private fun setupFAB() {
        binding.fabAddCours.setOnClickListener {
            val intent = Intent(this, AddCoursActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_COURS)
        }
    }

    /**
     * Configure les filtres par chips (jour et type)
     */
    private fun setupChipFilters() {
        // Filtre par jour
        binding.chipGroupJours.removeAllViews()

        // Chip "Tous" pour réinitialiser le filtre
        val chipTous = Chip(this).apply {
            text = "Tous"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                viewModel.clearFilter()
            }
        }
        binding.chipGroupJours.addView(chipTous)

        // Ajoute un chip pour chaque jour
        JourSemaine.getAllJours().forEach { jour ->
            val chip = Chip(this).apply {
                text = jour
                isCheckable = true
                setOnClickListener {
                    viewModel.filterByJour(jour)
                }
            }
            binding.chipGroupJours.addView(chip)
        }

        // Filtre par type de cours
        binding.chipGroupTypes.removeAllViews()

        // Chip "Tous"
        val chipTousTypes = Chip(this).apply {
            text = "Tous"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                viewModel.clearFilter()
            }
        }
        binding.chipGroupTypes.addView(chipTousTypes)

        // Ajoute un chip pour chaque type
        TypeCours.values().forEach { type ->
            val chip = Chip(this).apply {
                text = type.toString()
                isCheckable = true
                setOnClickListener {
                    viewModel.filterByType(type.name)
                }
            }
            binding.chipGroupTypes.addView(chip)
        }
    }

    /**
     * Observe les LiveData du ViewModel
     */
    private fun observeViewModel() {
        // Observer tous les cours
        viewModel.allCours.observe(this) { cours ->
            if (viewModel.currentFilter.value == CoursViewModel.FilterType.NONE) {
                adapter.submitList(cours)
                updateEmptyState(cours.isEmpty())
            }
        }

        // Observer les cours filtrés
        viewModel.filteredCours.observe(this) { cours ->
            if (viewModel.currentFilter.value != CoursViewModel.FilterType.NONE) {
                adapter.submitList(cours)
                updateEmptyState(cours.isEmpty())
            }
        }
    }

    /**
     * Met à jour l'affichage quand la liste est vide
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerViewCours.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.recyclerViewCours.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * Affiche un dialogue de confirmation avant suppression
     */
    private fun showDeleteDialog(cours: Cours) {
        AlertDialog.Builder(this)
            .setTitle("Supprimer le cours")
            .setMessage("Voulez-vous vraiment supprimer \"${cours.nomCours}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.delete(cours)
                Toast.makeText(this, "Cours supprimé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // Configure la SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.clearFilter()
                } else {
                    viewModel.searchCours(newText)
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                showDeleteAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Dialogue de confirmation pour supprimer tous les cours
     */
    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer tous les cours")
            .setMessage("Voulez-vous vraiment supprimer tous les cours ?")
            .setPositiveButton("Supprimer tout") { _, _ ->
                viewModel.deleteAll()
                Toast.makeText(this, "Tous les cours ont été supprimés", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_ADD_COURS -> {
                    Toast.makeText(this, "Cours ajouté avec succès", Toast.LENGTH_SHORT).show()
                }
                REQUEST_EDIT_COURS -> {
                    Toast.makeText(this, "Cours mis à jour", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}