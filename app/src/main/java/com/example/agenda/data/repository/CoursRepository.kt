package com.example.agenda.data.repository

import androidx.lifecycle.LiveData
import com.example.agenda.data.database.CoursDao
import com.example.agenda.data.model.Cours

/**
 * Repository pour gérer l'accès aux données des cours
 * Abstraction entre le ViewModel et la source de données (Room)
 * Permet de centraliser la logique d'accès aux données
 */
class CoursRepository(private val coursDao: CoursDao) {

    // LiveData pour observer tous les cours
    val allCours: LiveData<List<Cours>> = coursDao.getAllCours()

    /**
     * Insère un nouveau cours
     * Suspend function pour être appelée depuis une coroutine
     */
    suspend fun insert(cours: Cours): Long {
        return coursDao.insert(cours)
    }

    /**
     * Met à jour un cours existant
     */
    suspend fun update(cours: Cours) {
        coursDao.update(cours)
    }

    /**
     * Supprime un cours
     */
    suspend fun delete(cours: Cours) {
        coursDao.delete(cours)
    }

    /**
     * Supprime tous les cours
     */
    suspend fun deleteAll() {
        coursDao.deleteAll()
    }

    /**
     * Recherche des cours par mot-clé
     */
    fun searchCours(query: String): LiveData<List<Cours>> {
        return coursDao.searchCours(query)
    }

    /**
     * Filtre les cours par jour
     */
    fun getCoursByJour(jour: String): LiveData<List<Cours>> {
        return coursDao.getCoursByJour(jour)
    }

    /**
     * Filtre les cours par type
     */
    fun getCoursByType(type: String): LiveData<List<Cours>> {
        return coursDao.getCoursByType(type)
    }

    /**
     * Récupère un cours par son ID
     */
    suspend fun getCoursById(id: Int): Cours? {
        return coursDao.getCoursById(id)
    }

    /**
     * Récupère les cours avec notifications actives
     */
    suspend fun getCoursWithNotifications(): List<Cours> {
        return coursDao.getCoursWithNotifications()
    }
}