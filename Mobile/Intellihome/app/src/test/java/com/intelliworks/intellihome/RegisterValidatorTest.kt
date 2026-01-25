package com.intelliworks.intellihome

import com.intelliworks.intellihome.data.model.DatosFormularioRegistro
import com.intelliworks.intellihome.utils.RegistroValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RegisterValidatorTest {
    
    private lateinit var validator: RegistroValidator
    private lateinit var datosValidos: DatosFormularioRegistro    
    
    @Before
    fun setUp() {
        // Inicializa el validator antes de cada prueba
        validator = RegistroValidator()
        
        // Datos válidos de ejemplo
        datosValidos = DatosFormularioRegistro(
            username = "usuario123",
            password = "password123",
            confirmPassword = "password456", // password123
            nombre = "Juan",
            apellidos = "Pérez",
            correo = "juan@example.com",
            telefono = "88888888",
            fechaNacimiento = "1990-01-01",
            domicilio = "San José",
            preguntaRecuperacionId = 1,
            respuestaRecuperacion = "Respuesta",
            fingerprintEnabled = false,
            nombreTitular = "Juan Perez",
            numeroEncriptado = "1234567890123456",
            fechaExpiracion = "12/2025",
            hobbiesIds = listOf(1, 2),
            tiposCasaIds = listOf(1)
        )
    }
    
    /**
     *  Método seleccionado: validarFormularioCompleto
     *
     *  Este método valida todos los datos del formulario de registro, incluyendo:
     *   - Verificación de aceptación de términos y condiciones
     *   - Validación del formato del correo electrónico
     *   - Validación de que las contraseñas coincidan
     *   - Verificación de que una imagen de perfil haya sido seleccionada
     *   - Validación de que todos los campos requeridos estén completos
     *   
     *  El método retorna un mensaje de error específico si algún dato es inválido, o null si todos los datos son válidos.
     */

    /**
     * Lista de los assert utilizados:
     *   - assertTrue(condition): Verifica que la condición sea verdadera.
     *   - assertNull(object): Verifica que el objeto sea nulo.
     *   - assertNotNull(object): Verifica que el objeto no sea nulo.
     */

    // ===================================================================================== //

    /**
     * Test 1: Verifica que el formulario con todos los datos correctos sea aceptado.
     *
     * Comprueba que el método retorne null, indicando que no hay errores de validación.
     *
     * Si el test falla, significa que la validación está rechazando datos correctos.
     */
    @Test
    fun Test1ValidarFormularioCompleto() {
        val resultado = validator.validarFormularioCompleto(
            datos = datosValidos,
            terminosAceptados = true,
            imagenSeleccionada = true
        )
        
        // Verifica que el resultado sea null, es decir, que todo sea válido
        assertNull(resultado)
    }
    

    /**
     * Test 2: Verifica que el formulario detecta un correo electrónico inválido.
     *
     * Comprueba que el método retorne un mensaje de error relacionado con el correo.
     *
     * Si el test falla, la validación no está identificando correos inválidos correctamente.
     */
    @Test
    fun Test2ValidarFormularioCompleto() {

        // Modifica el correo a uno inválido
        val datos = datosValidos.copy(correo = "correo-invalido")

        val resultado = validator.validarFormularioCompleto(
            datos = datos,
            terminosAceptados = true,
            imagenSeleccionada = true
        )

        // Verifica que el resultado no sea null, es decir, que haya un error
        assertNotNull(resultado)

        // Verifica que el mensaje de error contenga referencia al correo electrónico
        assertTrue(resultado!!.contains("correo"))
    }
    

    /**
     * Test 3: Verifica que el formulario detecta contraseñas que no coinciden.
     *
     * Comprueba que el método retorne un mensaje de error relacionado con la coincidencia de contraseñas.
     *
     * Si el test falla, la validación no está identificando correctamente contraseñas distintas.
     */
    @Test
    fun Test3ValidarFormularioCompleto() {

        // Modifica las contraseñas para que no coincidan
        val datos = datosValidos.copy(
            password = "password123",
            confirmPassword = "diferente456"
        )
        
        val resultado = validator.validarFormularioCompleto(
            datos = datos,
            terminosAceptados = true,
            imagenSeleccionada = true
        )

        // Verifica que el resultado no sea null, es decir, que haya un error
        assertNotNull(resultado)

        // Verifica que el mensaje de error contenga referencia a las contraseñas que no coinciden
        assertTrue(resultado!!.contains("contraseñas no coinciden"))
    }

}
