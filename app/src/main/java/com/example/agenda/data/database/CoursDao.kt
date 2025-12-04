package com.example.agenda.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.agenda.data.model.Cours

/**
 * DAO (Data Access Object) pour les opérations de base de données sur les cours
 * Room génère automatiquement l'implémentation de cette interface
 */
@Dao
interface CoursDao {

    /**
     * Insère un nouveau cours dans la base de données
     * @param cours Le cours à insérer
     * @return L'ID du cours inséré
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cours: Cours): Long

    /**
     * Met à jour un cours existant
     * @param cours Le cours à mettre à jour
     */
    @Update
    suspend fun update(cours: Cours)

    /**
     * Supprime un cours de la base de données
     * @param cours Le cours à supprimer
     */
    @Delete
    suspend fun delete(cours: Cours)

    /**
     * Supprime tous les cours de la base de données
     */
    @Query("DELETE FROM cours_table")
    suspend fun deleteAll()

    /**
     * Récupère tous les cours triés par jour et heure de début
     * @return LiveData contenant la liste de tous les cours
     */
    @Query("SELECT * FROM cours_table ORDER BY " +
            "CASE jour " +
            "WHEN 'Lundi' THEN 1 " +
            "WHEN 'Mardi' THEN 2 " +
            "WHEN 'Mercredi' THEN 3 " +
            "WHEN 'Jeudi' THEN 4 " +
            "WHEN 'Vendredi' THEN 5 " +
            "WHEN 'Samedi' THEN 6 " +
            "WHEN 'Dimanche' THEN 7 " +
            "END, heureDebut ASC")
    fun getAllCours(): LiveData<List<Cours>>

    /**
     * Recherche des cours par nom, professeur ou salle
     * @param searchQuery La requête de recherche
     * @return LiveData contenant les cours correspondants
     */
    @Query("SELECT * FROM cours_table WHERE " +
            "nomCours LIKE '%' || :searchQuery || '%' OR " +
            "professeur LIKE '%' || :searchQuery || '%' OR " +
            "salle LIKE '%' || :searchQuery || '%' " +
            "ORDER BY CASE jour " +
            "WHEN 'Lundi' THEN 1 " +
            "WHEN 'Mardi' THEN 2 " +
            "WHEN 'Mercredi' THEN 3 " +
            "WHEN 'Jeudi' THEN 4 " +
            "WHEN 'Vendredi' THEN 5 " +
            "WHEN 'Samedi' THEN 6 " +
            "WHEN 'Dimanche' THEN 7 " +
            "END, heureDebut ASC")
    fun searchCours(searchQuery: String): LiveData<List<Cours>>

    /**
     * Filtre les cours par jour
     * @param jour Le jour à filtrer
     * @return LiveData contenant les cours du jour spécifié
     */
    @Query("SELECT * FROM cours_table WHERE jour = :jour ORDER BY heureDebut ASC")
    fun getCoursByJour(jour: String): LiveData<List<Cours>>

    /**
     * Filtre les cours par type
     * @param type Le type de cours (CM, TD, TP, AUTRE)
     * @return LiveData contenant les cours du type spécifié
     */
    @Query("SELECT * FROM cours_table WHERE typeCours = :type ORDER BY " +
            "CASE jour " +
            "WHEN 'Lundi' THEN 1 " +
            "WHEN 'Mardi' THEN 2 " +
            "WHEN 'Mercredi' THEN 3 " +
            "WHEN 'Jeudi' THEN 4 " +
            "WHEN 'Vendredi' THEN 5 " +
            "WHEN 'Samedi' THEN 6 " +
            "WHEN 'Dimanche' THEN 7 " +
            "END, heureDebut ASC")
    fun getCoursByType(type: String): LiveData<List<Cours>>

    /**
     * Récupère un cours par son ID
     * @param id L'ID du cours
     * @return Le cours correspondant ou null
     */
    @Query("SELECT * FROM cours_table WHERE id = :id")
    suspend fun getCoursById(id: Int): Cours?

    /**
     * Récupère tous les cours avec notification active
     * @return Liste des cours avec notification
     */
    @Query("SELECT * FROM cours_table WHERE notificationActive = 1")
    suspend fun getCoursWithNotifications(): List<Cours>
}