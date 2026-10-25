package com.example.coflow.data.repositories

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.coflow.repositories.AuthRepository
import com.example.coflow.repositories.FirebaseAuthRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthRepositoryImplTest {

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = FirebaseAuthRepositoryImpl()
    }

    @Test
    fun testRegisterAndLogin_Success() = runBlocking {
        val randomEmail = "test_${System.currentTimeMillis()}@example.com"
        val password = "password123"
        val name = "Тестовый Пользователь"

        // 1. Тестируем регистрацию
        val registerResult = repository.register(randomEmail, password, name)
        assertTrue("Регистрация должна пройти успешно", registerResult.isSuccess)

        val uid = registerResult.getOrNull()
        assertNotNull("UID не должен быть пустым", uid)

        // 2. Тестируем вход
        val loginResult = repository.login(randomEmail, password)
        assertTrue("Вход должен пройти успешно", loginResult.isSuccess)
        assertEquals("UID после входа должен совпадать", uid, loginResult.getOrNull())

        // 3. Тестируем получение текущего пользователя
        val currentUid = repository.getCurrentUserUid()
        assertEquals("Текущий UID должен совпадать", uid, currentUid)
    }

    @Test
    fun testLogin_WrongPassword_Failure() = runBlocking {
        val email = "test_wrong_${System.currentTimeMillis()}@example.com"
        val password = "password123"

        // Сначала регистрируем
        repository.register(email, password, "Test")

        // Пытаемся войти с неверным паролем
        val result = repository.login(email, "wrong_password")

        assertTrue("Вход с неверным паролем должен вернуть ошибку", result.isFailure)
        println("Текст ошибки от Firebase: ${result.exceptionOrNull()?.message}")
    }
}