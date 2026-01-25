package com.intelliworks.intellihome

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.intelliworks.intellihome.utils.RegisterHelper
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class RegisterHelperTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockUri: Uri
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var mockCursor: Cursor
    
    @Before
    fun setUp() {
        // Inicializa los mocks antes de cada prueba
        mockContext = mockk()
        mockUri = mockk()
        mockContentResolver = mockk()
        mockCursor = mockk()
        
        // Configura el comportamiento básico del ContentResolver
        every { mockContext.contentResolver } returns mockContentResolver
    }
    
    /**
     *  Método seleccionado: validarImagen
     *
     *  Este método valida que una imagen cumpla con los requisitos de tipo (JPG, PNG, GIF) y tamaño (máximo 1MB).
     *   - Verifica el tipo de archivo por MIME type y extensión
     *   - Verifica que el tamaño no exceda el límite permitido
     *   
     *  El método retorna un par donde: 
     *    - El primer valor indica si la imagen es válida, es decir, cumple con los requisitos
     *    - El segundo es un mensaje de error si no lo es, o null si es válida.
     */

    /**
     * Lista de los assert utilizados:
     *   - assertTrue(condition): Verifica que la condición sea verdadera.
     *   - assertFalse(condition): Verifica que la condición sea falsa.
     *   - assertNull(object): Verifica que el objeto sea nulo.
     *   - assertNotNull(object): Verifica que el objeto no sea nulo.
     */

    // ===================================================================================== //

    /**
     * Test 1: Verifica que una imagen con tipo y tamaño válidos sea aceptada.
     *
     * Comprueba que el método acepte una imagen PNG de 500KB simulada como válida.
     *
     * Si el test falla, la validación está rechazando imágenes correctas.
     */
    @Test
    fun Test1ValidarImagen() {
        // Configurar mock para tipo de imagen válido (PNG)
        every { mockContentResolver.getType(mockUri) } returns "image/png"

        // Configurar mock para tamaño dentro del límite (500KB < 1MB)
        val tamanioValido = 500 * 1024 // 500 * 1024
        val inputStream = ByteArrayInputStream(ByteArray(tamanioValido))
        every { mockContentResolver.openInputStream(mockUri) } returns inputStream

        val (esValida, mensajeError) = RegisterHelper.validarImagen(mockContext, mockUri)
        
        // Verificar que la imagen sea aceptada como válida
        assertTrue(esValida)

        // Verificar que no haya mensaje de error, es decir, sea null
        assertNull(mensajeError)
    }
    
    /**
     * Test 2: Verifica que una imagen con tipo inválido sea rechazada.
     *
     * Comprueba que el método rechace un archivo PDF simulado y retorne un mensaje de error.
     *
     * Si el test falla, la validación no está identificando tipos de archivo incorrectos.
     */
    @Test
    fun Test2ValidarImagen() {
        // Configurar mock para tipo de imagen inválido (PDF)
        every { mockContentResolver.getType(mockUri) } returns "application/pdf"
        
        // Configurar mock para nombre de archivo con extensión inválida
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 0
        every { mockCursor.getString(0) } returns "documento.pdf"
        every { mockCursor.close() } just Runs
        every { mockContentResolver.query(mockUri, null, null, null, null) } returns mockCursor
        
        val (esValida, mensajeError) = RegisterHelper.validarImagen(mockContext, mockUri)
        
        // Verificar que la imagen sea rechazada por tipo inválido
        assertFalse(esValida)

        // Verificar que haya un mensaje de error relacionado con el tipo de imagen, es decir, no sea null
        assertNotNull(mensajeError)

        // Verificar que el mensaje de error mencione los tipos permitidos
        assertTrue(mensajeError!!.contains("JPG") || mensajeError.contains("PNG") || mensajeError.contains("GIF"))
    }
    
    /**
     * Test 3: Verifica que una imagen que excede el tamaño máximo sea rechazada.
     *
     * Comprueba que el método rechace una imagen JPEG simulada de 2MB y retorne un mensaje de error sobre el tamaño.
     *
     * Si el test falla, la validación no está identificando imágenes demasiado grandes.
     */
    @Test
    fun Test3ValidarImagen() {
        // Configurar mock para tipo de imagen válido (JPEG)
        every { mockContentResolver.getType(mockUri) } returns "image/jpeg"
        
        // Configurar mock para tamaño que excede el límite (2MB > 1MB)
        val tamanioExcedido = 2 * 1024 * 1024
        val inputStream = ByteArrayInputStream(ByteArray(tamanioExcedido))
        every { mockContentResolver.openInputStream(mockUri) } returns inputStream
        
        val (esValida, mensajeError) = RegisterHelper.validarImagen(mockContext, mockUri)
        
        // Verificar que la imagen sea rechazada por tamaño excedido
        assertFalse(esValida)

        // Verificar que haya un mensaje de error relacionado con el tamaño, es decir, no sea null
        assertNotNull(mensajeError)

        // Verificar que el mensaje de error mencione el límite de 1MB
        assertTrue(mensajeError!!.contains("1MB") || mensajeError.contains("tamaño"))
    }

}
