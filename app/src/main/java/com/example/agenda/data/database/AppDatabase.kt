package com.example.agenda.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.agenda.data.model.Cours

/**
 * Classe de base de données Room
 * Gère la création et l'accès à la base de données SQLite
 */
@Database(
    entities = [Cours::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Fournit l'accès au DAO des cours
     */
    abstract fun coursDao(): CoursDao

    companion object {
        // Singleton pour éviter d'avoir plusieurs instances de la base de données
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Récupère l'instance unique de la base de données
         * Utilise le pattern Singleton avec double-checked locking
         * @param context Le contexte de l'application
         * @return L'instance de la base de données
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agenda_database"
                )
                    // Stratégies de migration (pour les mises à jour futures)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Convertisseurs de types pour Room
 * Permet de stocker les enums dans la base de données
 */
class Converters {
    @androidx.room.TypeConverter
    fun fromTypeCours(value: com.example.agenda.data.model.TypeCours): String {
        return value.name
    }

    @androidx.room.TypeConverter
    fun toTypeCours(value: String): com.example.agenda.data.model.TypeCours {
        return com.example.agenda.data.model.TypeCours.valueOf(value)
    }
}