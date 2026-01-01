package com.schoolerz.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class ServiceType(
    val displayName: String,
    val firestoreValue: String
) {
    DOG_WALKING("Dog Walking", "dog_walking"),
    TUTORING("Tutoring", "tutoring"),
    BABYSITTING("Babysitting", "babysitting"),
    LAWN_CARE("Lawn Care", "lawn_care"),
    TECH_HELP("Tech Help", "tech_help"),
    MUSIC_LESSONS("Music", "music_lessons"),
    ESSAY_REVIEW("Essay", "essay_review"),
    CAR_WASH("Car Wash", "car_wash"),
    OTHER("Other", "other");

    companion object {
        fun fromFirestoreValue(value: String): ServiceType? {
            return entries.find { it.firestoreValue == value }
        }
    }
}
