package com.intelliworks.intellihome

import com.intelliworks.intellihome.data.api.UsuarioApi
import com.intelliworks.intellihome.data.model.LoginResponseDto
import com.intelliworks.intellihome.data.model.PreguntaResponseDto
import com.intelliworks.intellihome.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class UnitTesting {

    @Mock
    lateinit var mockApi: UsuarioApi

    private lateinit var repository: UsuarioRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = UsuarioRepository(mockApi)
    }

    // Función 1: loginUsuario

    // Test 1: Login exitoso
    @Test
    fun Test1LoginUsuario() = runTest {
        val mockUser = LoginResponseDto(
            id = 1,
            username = "patos",
            correo = "pato@email.com",
            telefono = "8888-8888",
            nombre = "Patricio",
            apellidos = "Jacinto",
            rolId = 2,
            estadoCuenta = "activo",
            errores = null,
            mensaje = "Éxito"
        )

        Mockito.`when`(mockApi.loginUsuario("pato", "1234"))
            .thenReturn(Response.success(mockUser))

        val response = repository.loginUsuario("pato", "1234")

        assertTrue(response.isSuccessful)
        assertEquals("pato", response.body()?.username)
    }

    // Test 2: Credenciales inválidas (401)
    @Test
    fun Test2LoginUsuario() = runTest {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())

        Mockito.`when`(mockApi.loginUsuario("pato", "mal"))
            .thenReturn(Response.error(401, errorBody))

        val response = repository.loginUsuario("pato", "mal")

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
    }

    // Test 3: Error de red
    @Test
    fun Test3LoginUsuario() = runTest {
        Mockito.`when`(mockApi.loginUsuario("pato", "1234"))
            .thenThrow(RuntimeException("Error de red"))

        try {
            repository.loginUsuario("pato", "1234")
            fail("Se esperaba una excepción")
        } catch (e: Exception) {
            assertEquals("Error de red", e.message)
        }
    }

    // Función 2: obtenerPregunta

    // Test 1: Obtención exitosa de la pregunta
    @Test
    fun Test1ObtenerPregunta() = runTest {
        val preguntaMock = PreguntaResponseDto(
            identificador = "pato",
            preguntaId = 1,
            pregunta = "¿Nombre de tu mascota?",
            errores = null
        )

        Mockito.`when`(mockApi.obtenerPregunta("pato"))
            .thenReturn(Response.success(preguntaMock))

        val response = mockApi.obtenerPregunta("pato")

        assertTrue(response.isSuccessful)
        assertEquals("¿Nombre de tu mascota?", response.body()?.pregunta)
    }

    // Test 2: Usuario no encontrado (400)
    @Test
    fun Test2ObtenerPregunta() = runTest {
        val errorBody = "{}".toResponseBody("application/json".toMediaType())

        Mockito.`when`(mockApi.obtenerPregunta("desconocido"))
            .thenReturn(Response.error(400, errorBody))

        val response = mockApi.obtenerPregunta("desconocido")

        assertFalse(response.isSuccessful)
        assertEquals(400, response.code())
    }

    // Test 3: Error de red
    @Test
    fun Test3ObtenerPregunta() = runTest {
        Mockito.`when`(mockApi.obtenerPregunta("pato"))
            .thenThrow(RuntimeException("Error de conexión"))

        try {
            mockApi.obtenerPregunta("pato")
            fail("Se esperaba una excepción")
        } catch (e: Exception) {
            assertEquals("Error de conexión", e.message)
        }
    }
}
