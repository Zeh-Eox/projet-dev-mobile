package com.example.agenda.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.agenda.data.model.Cours
import com.example.agenda.data.repository.CoursRepository
import com.example.agenda.data.database.AppDatabase
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer la logique métier et l'état de l'UI
 * Survit aux changements de configuration (rotation d'écran, etc.)
 */
class CoursViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CoursRepository

    // LiveData observée par l'UI
    val allCours: LiveData<List<Cours>>

    // LiveData pour les filtres
    private val _filteredCours = MutableLiveData<List<Cours>>()
    val filteredCours: LiveData<List<Cours>> = _filteredCours

    // État du filtre actif
    private val _currentFilter = MutableLiveData<FilterType>(FilterType.NONE)
    val currentFilter: LiveData<FilterType> = _currentFilter

    init {
        val coursDao = AppDatabase.getDatabase(application).coursDao()
        repository = CoursRepository(coursDao)
        allCours = repository.allCours
    }

    /**
     * Insère un nouveau cours
     */
    fun insert(cours: Cours) = viewModelScope.launch {
        repository.insert(cours)
    }

    /**
     * Met à jour un cours
     */
    fun update(cours: Cours) = viewModelScope.launch {
        repository.update(cours)
    }

    /**
     * Supprime un cours
     */
    fun delete(cours: Cours) = viewModelScope.launch {
        repository.delete(cours)
    }

    /**
     * Supprime tous les cours
     */
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    /**
     * Recherche des cours
     */
    fun searchCours(query: String) {
        _currentFilter.value = FilterType.SEARCH
        viewModelScope.launch {
            repository.searchCours(query).observeForever { cours ->
                _filteredCours.value = cours
            }
        }
    }

    /**
     * Filtre les cours par jour
     */
    fun filterByJour(jour: String) {
        _currentFilter.value = FilterType.JOUR
        viewModelScope.launch {
            repository.getCoursByJour(jour).observeForever { cours ->
                _filteredCours.value = cours
            }
        }
    }

    /**
     * Filtre les cours par type
     */
    fun filterByType(type: String) {
        _currentFilter.value = FilterType.TYPE
        viewModelScope.launch {
            repository.getCoursByType(type).observeForever { cours ->
                _filteredCours.value = cours
            }
        }
    }

    /**
     * Réinitialise les filtres
     */
    fun clearFilter() {
        _currentFilter.value = FilterType.NONE
        _filteredCours.value = emptyList()
    }

    /**
     * Récupère un cours par son ID
     */
    suspend fun getCoursById(id: Int): Cours? {
        return repository.getCoursById(id)
    }

    /**
     * Valide les données d'un cours avant insertion/mise à jour
     * @return Pair<Boolean, String> - (isValid, errorMessage)
     */
    fun validateCours(
        nomCours: String,
        professeur: String,
        salle: String,
        jour: String,
        heureDebut: String,
        heureFin: String
    ): Pair<Boolean, String> {

        // Vérification que tous les champs sont remplis
        if (nomCours.isBlank()) {
            return Pair(false, "Le nom du cours est obligatoire")
        }
        if (professeur.isBlank()) {
            return Pair(false, "Le nom du professeur est obligatoire")
        }
        if (salle.isBlank()) {
            return Pair(false, "La salle est obligatoire")
        }
        if (jour.isBlank()) {
            return Pair(false, "Le jour est obligatoire")
        }
        if (heureDebut.isBlank() || heureFin.isBlank()) {
            return Pair(false, "Les horaires sont obligatoires")
        }

        // Vérification de la cohérence des horaires
        if (!isValidTimeFormat(heureDebut) || !isValidTimeFormat(heureFin)) {
            return Pair(false, "Format d'heure invalide (HH:mm)")
        }

        if (heureDebut >= heureFin) {
            return Pair(false, "L'heure de fin doit être après l'heure de début")
        }

        return Pair(true, "")
    }

    /**
     * Vérifie si une chaîne est au format HH:mm valide
     */
    private fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(time)
    }

    /**
     * Types de filtres possibles
     */
    enum class FilterType {
        NONE,
        JOUR,
        TYPE,
        SEARCH
    }
}