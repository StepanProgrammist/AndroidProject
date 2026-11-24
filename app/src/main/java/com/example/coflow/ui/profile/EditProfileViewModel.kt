package com.example.coflow.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coflow.repositories.AuthRepository
import com.example.coflow.repositories.FirebaseAuthRepositoryImpl
import com.example.coflow.repositories.FirebaseUserRepositoryImpl
import com.example.coflow.repositories.UserRepository
import com.example.coflow.repositories.dto.UserUpdateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Логика экрана «Изменение профиля».
 *
 * Уже работает: чтение профиля и сохранение имени в users/{uid}.
 * Почта показывается только для чтения: менять её здесь нельзя.
 *
 * TODO(редактирование профиля):
 * - смена почты требует AuthRepository.updateEmail(...) с повторной
 *   аутентификацией, поэтому поле заблокировано;
 * - имя можно дублировать в FirebaseUser.displayName;
 * - ограничение длины имени.
 */

/** Состояние экрана «Изменение профиля». */
data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
) {
    val canSave: Boolean
        get() = !isLoading && !isSaving && name.isNotBlank()
}

/**
 * Логика редактирования профиля.
 *
 * Чтение и запись идут через `users/{uid}` ([UserRepository]).
 * Firebase Auth (логин/пароль/email для входа) здесь не меняется — это ограничение
 * текущего [AuthRepository], поэтому меняется только документ профиля.
 */
class EditProfileViewModel(
    private val userRepository: UserRepository = FirebaseUserRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl(),
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    /** Подставляет текущие имя и почту пользователя в форму. */
    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val uid = authRepository.getCurrentUserUid()
            if (uid.isNullOrBlank()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Пользователь не авторизован",
                    )
                }
                return@launch
            }

            userRepository
                .getById(uid)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            name = user.name,
                            email = user.email,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Не удалось загрузить профиль",
                        )
                    }
                }
        }
    }

    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    /** Проверяет имя и сохраняет его в Firestore. Почта не меняется. */
    fun onSaveClick() {
        val name = _state.value.name.trim()
        val nameError = if (name.isBlank()) "Введите имя" else null

        if (nameError != null) {
            _state.update { it.copy(nameError = nameError) }
            return
        }

        val uid = authRepository.getCurrentUserUid()
        if (uid.isNullOrBlank()) {
            _state.update { it.copy(errorMessage = "Пользователь не авторизован") }
            return
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            userRepository
                .updateUser(
                    id = uid,
                    update = UserUpdateDto(name = name),
                ).onSuccess { user ->
                    _state.update {
                        it.copy(
                            name = user.name,
                            isSaving = false,
                            isSaved = true,
                        )
                    }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Не удалось сохранить профиль",
                        )
                    }
                }
        }
    }

    /** Сбрасывает флаг сохранения после навигации назад. */
    fun consumeSaved() {
        _state.update { it.copy(isSaved = false) }
    }
}
