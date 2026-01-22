import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
import pytest
from Servicios.Usuario_Servicio import Usuario_Servicio

#-----------------Clases Dummy para simular la base de datos y otros componentes------------------

# Clase Dummy para simular consultas a la base de datos
class DummyQuery:
    def __init__(self, result=None):
        self._result = result
    def filter_by(self, **kwargs):
        return self
    def first(self):
        return self._result

# Clase Dummy para simular la sesión de la base de datos
class DummyDB:
    def __init__(self):
        self.data = {}
        self.committed = False
        self.closed = False
    def add(self, obj):
        self.data['user'] = obj
    def commit(self):
        self.committed = True
    def rollback(self):
        self.committed = False
    def close(self):
        self.closed = True
    def query(self, model):
        # Simula que no hay resultados para unicidad
        return DummyQuery(result=None)
    
# Clase Dummy para simular la carga de archivos
class DummyUploadFile:
    filename = "test.png"
    def __init__(self):
        self.file = self
    def read(self):
        return b"fake image data"
    def seek(self, offset, whence=0):
        pass
    def tell(self):
        return 0

# Clase Dummy para simular una pregunta de recuperación
class DummyPreguntaRecuperacion:
    id = 1
    texto = "¿Cuál es tu color favorito?"

# Clase Dummy para simular la base de datos con PreguntaRecuperacion
class DummyDBWithPregunta(DummyDB):
    def query(self, model):
        # Si se consulta PreguntaRecuperacion, devolver un objeto válido
        if model.__name__ == "PreguntaRecuperacion":
            class Query:
                def filter_by(self_inner, **kwargs):
                    class Result:
                        def first(self):
                            return DummyPreguntaRecuperacion()
                    return Result()
            return Query()
        return super().query(model)

 
#-----------------Tests------------------

# Pruebas para el método registrar_usuario
# Casos de prueba:
# 1. Registro exitoso con datos válidos.
def test_Test1RegistrarUsuario():
    db = DummyDBWithPregunta()
    resultado = Usuario_Servicio.registrar_usuario(
        db=db,
        nombre="Juan",
        apellidos="Pérez",
        username="juanperez",
        correo="juan@example.com",
        telefono="1234567890",
        fecha_nacimiento="2000-01-01",
        domicilio="Calle 1",
        contrasena="Password123",
        imagen_perfil=DummyUploadFile(),
        hobbies_ids=[],
        tipos_casa_ids=[],
        pregunta_recuperacion_id=1,
        respuesta_recuperacion="respuesta",
        permitir_huella=0,
        nombre_titular="Juan Pérez",
        numero_tarjeta="4111111111111111",
        fecha_expiracion="12/2030",
        token_publico=None
    )
    assert 'mensaje' in resultado
    assert resultado['mensaje'] == 'Usuario registrado exitosamente'

    # Prueba de test fallida intencionalmente para demostrar el funcionamiento del test
    #assert resultado['mensaje'] == 'Registro fallido' 

# 2. Registro fallido por contraseña débil.
def test_Test2RegistrarUsuario():
    db = DummyDB()
    resultado = Usuario_Servicio.registrar_usuario(
        db=db,
        nombre="Juan",
        apellidos="Pérez",
        username="juanperez",
        correo="juan@example.com",
        telefono="1234567890",
        fecha_nacimiento="2000-01-01",
        domicilio="Calle 1",
        contrasena="123",
        imagen_perfil=None,
        hobbies_ids=[],
        tipos_casa_ids=[],
        pregunta_recuperacion_id=1,
        respuesta_recuperacion="respuesta",
        permitir_huella=0,
        nombre_titular="Juan Pérez",
        numero_tarjeta="4111111111111111",
        fecha_expiracion="12/2030",
        token_publico=None
    )
    assert 'errores' in resultado
    assert 'contrasena' in resultado['errores']

# 3. Registro fallido por username ya existente.
def test_Test3RegistrarUsuario():
    class DummyDBUsername(DummyDB):
        def query(self, model):
            # Simula que el username ya existe
            class DummyQueryUsername:
                def filter_by(self_inner, **kwargs):
                    class DummyResult:
                        def first(self):
                            if 'username' in kwargs:
                                return True  # Simula que existe
                            return None
                    return DummyResult()
            return DummyQueryUsername()
    db = DummyDBUsername()
    resultado = Usuario_Servicio.registrar_usuario(
        db=db,
        nombre="Juan",
        apellidos="Pérez",
        username="juanperez",
        correo="juan@example.com",
        telefono="1234567890",
        fecha_nacimiento="2000-01-01",
        domicilio="Calle 1",
        contrasena="Password123",
        imagen_perfil=None,
        hobbies_ids=[],
        tipos_casa_ids=[],
        pregunta_recuperacion_id=1,
        respuesta_recuperacion="respuesta",
        permitir_huella=0,
        nombre_titular="Juan Pérez",
        numero_tarjeta="4111111111111111",
        fecha_expiracion="12/2030",
        token_publico=None
    )
    assert 'errores' in resultado
    assert 'username' in resultado['errores']

# Pruebas para el método _validar_nombres_obscenos
# Casos de prueba:
# 1. Nombres y username sin obscenidades.
def test_Test1ValidarNombresObscenos():
    errores = {}
    Usuario_Servicio._validar_nombres_obscenos("Juan", "Pérez", "juanperez", errores)
    assert errores == {}

# 2. Nombre con obscenidad.
def test_Test2ValidarNombresObscenos():
    errores = {}
    Usuario_Servicio._validar_nombres_obscenos("puta", "Pérez", "juanperez", errores)
    assert 'nombre' in errores

# 3. Username con obscenidad.
def test_Test3ValidarNombresObscenos():
    errores = {}
    Usuario_Servicio._validar_nombres_obscenos("Juan", "Pérez", "maricon", errores)
    assert 'username' in errores
