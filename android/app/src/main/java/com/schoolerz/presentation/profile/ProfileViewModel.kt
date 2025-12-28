package com.schoolerz.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolerz.domain.model.Profile
import com.schoolerz.domain.model.VerificationStatus
import com.schoolerz.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class ProfileState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val editingDisplayName: String = "",
    val editingSchoolName: String = "",
    val editingGrade: String = "",
    val editingBio: String = "",
    val editingNeighborhood: String = "",
    val editingServices: Set<String> = emptySet()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getCurrentProfile()
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            editingDisplayName = profile?.displayName ?: "",
                            editingSchoolName = profile?.schoolName ?: "",
                            editingGrade = profile?.grade ?: "",
                            editingBio = profile?.bio ?: "",
                            editingNeighborhood = profile?.neighborhood ?: "",
                            editingServices = profile?.services?.toSet() ?: emptySet()
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            repository.getCurrentProfile()
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            profile = profile,
                            editingDisplayName = profile?.displayName ?: "",
                            editingSchoolName = profile?.schoolName ?: "",
                            editingGrade = profile?.grade ?: "",
                            editingBio = profile?.bio ?: "",
                            editingNeighborhood = profile?.neighborhood ?: "",
                            editingServices = profile?.services?.toSet() ?: emptySet()
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isRefreshing = false, error = e.message) }
                }
        }
    }

    fun saveProfile() {
        val currentState = _state.value
        val displayName = currentState.editingDisplayName.trim()

        if (displayName.isEmpty()) {
            _state.update { it.copy(error = "Display name is required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val profile = Profile(
                id = currentState.profile?.id ?: UUID.randomUUID().toString(),
                displayName = displayName,
                schoolName = currentState.editingSchoolName.trim().takeIf { it.isNotEmpty() },
                grade = currentState.editingGrade.trim().takeIf { it.isNotEmpty() },
                bio = currentState.editingBio.trim().takeIf { it.isNotEmpty() },
                neighborhood = currentState.editingNeighborhood.trim().takeIf { it.isNotEmpty() },
                services = currentState.editingServices.toList(),
                avatarPath = currentState.profile?.avatarPath,
                verificationStatus = currentState.profile?.verificationStatus ?: VerificationStatus.UNVERIFIED,
                createdAt = currentState.profile?.createdAt ?: Date()
            )

            repository.saveProfile(profile)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            profile = profile,
                            isEditing = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false, error = e.message) }
                }
        }
    }

    fun startEditing() {
        val profile = _state.value.profile
        _state.update {
            it.copy(
                isEditing = true,
                editingDisplayName = profile?.displayName ?: "",
                editingSchoolName = profile?.schoolName ?: "",
                editingGrade = profile?.grade ?: "",
                editingBio = profile?.bio ?: "",
                editingNeighborhood = profile?.neighborhood ?: "",
                editingServices = profile?.services?.toSet() ?: emptySet()
            )
        }
    }

    fun cancelEditing() {
        val profile = _state.value.profile
        _state.update {
            it.copy(
                isEditing = false,
                editingDisplayName = profile?.displayName ?: "",
                editingSchoolName = profile?.schoolName ?: "",
                editingGrade = profile?.grade ?: "",
                editingBio = profile?.bio ?: "",
                editingNeighborhood = profile?.neighborhood ?: "",
                editingServices = profile?.services?.toSet() ?: emptySet()
            )
        }
    }

    fun updateDisplayName(name: String) {
        _state.update { it.copy(editingDisplayName = name) }
    }

    fun updateSchoolName(name: String) {
        _state.update { it.copy(editingSchoolName = name) }
    }

    fun updateGrade(grade: String) {
        _state.update { it.copy(editingGrade = grade) }
    }

    fun updateBio(bio: String) {
        _state.update { it.copy(editingBio = bio) }
    }

    fun updateNeighborhood(neighborhood: String) {
        _state.update { it.copy(editingNeighborhood = neighborhood) }
    }

    fun toggleService(service: String) {
        _state.update {
            val newServices = it.editingServices.toMutableSet()
            if (newServices.contains(service)) {
                newServices.remove(service)
            } else {
                newServices.add(service)
            }
            it.copy(editingServices = newServices)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
