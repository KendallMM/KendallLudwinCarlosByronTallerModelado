import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
import pytest
from Servicios.Usuario_Servicio import Usuario_Servicio


#----------------- Pruebas unitarias para el método login_usuario ------------------

# Bases de datos dummy
class DummyUsuario_login:
    def __init__(self):
        self.contrasena = Usuario_Servicio.pwd_context.hash("ludwin123")
        self.estado_cuenta = "activo"
        self.intentos_fallidos = 0
        self.id = 1
        self.username = "ludwinr"
        self.correo = "ludwinr@gmail.com"
        self.telefono = "1234567890"
        self.nombre = "ludwin"
        self.apellidos = "Ramos"
        self.rol_id = 2

class DummyDBLogin_UsuarioExistente():
    def __init__(self):
        self.data = {}
        self.committed = False
        self.closed = False

    def query(self, model):
        class DummyQuery:
            def filter(self_inner, *args, **kwargs):
                # Se retorna a sí mismo para poder llamar a first()
                return self_inner
            def first(self_inner):
                # retorna un objeto que contiene la información de un usuario (como una fila de la Tabla usuario)
                return DummyUsuario_login()
        return DummyQuery()

    def commit(self):
        self.committed = True
    def rollback(self):
        self.committed = False
    def close(self):
        self.closed = True

class DummyDBLogin_UsuarioInexistente():
    def __init__(self):
        self.data = {}
        self.committed = False
        self.closed = False

    def query(self, model):
        class DummyQuery:
            def filter(self_inner, *args, **kwargs):
                # Se retorna a sí mismo para poder llamar a first()
                return self_inner
            def first(self_inner):
                return None  # Simula que no se encontró el usuario
        return DummyQuery()

    # Métodos simulados de la base de datos (dummy)
    def commit(self):
        self.committed = True
    def rollback(self):
        self.committed = False
    def close(self):
        self.closed = True

# Casos de prueba:
# 1. Login exitoso con credenciales válidas.
def test_Test1LoginUsuario():
    db = DummyDBLogin_UsuarioExistente()
    resultado = Usuario_Servicio.login_usuario(db, "ludwinr", "ludwin123")
    assert "username" in resultado
    assert resultado["username"] == "ludwin"

# 2. Login fallido por contraseña incorrecta.
def test_Test2LoginUsuario():
    db = DummyDBLogin_UsuarioExistente()
    resultado = Usuario_Servicio.login_usuario(db, "ludwinr", "contraseña_incorrecta")
    assert "errores" in resultado
    assert "contrasena" in resultado["errores"]

# 3. Login fallido por usuario (correo, nombre de usuario, telefono) inexistente.
def test_Test3LoginUsuario():
    db = DummyDBLogin_UsuarioInexistente()
    resultado = Usuario_Servicio.login_usuario(db, "ludwinr", "ludwin123")
    assert "errores" in resultado
    assert "identificador" in resultado["errores"]


#----------------- Pruebas unitarias para el método restablecer_contraseña ------------------

# Bases de datos dummy
class DummyUsuario_Restablecer:
    def __init__(self):
        self.username = "ludwinr"
        self.correo = "ludwinr@gmail.com"
        self.telefono = "1234567890"
        self.contrasena = Usuario_Servicio.pwd_context.hash("vieja_contrasena")
        self.pregunta_recuperacion_id = 1
        self.respuesta_recuperacion = "respuesta_correcta"
        self.intentos_fallidos = 0
        self.estado_cuenta = "activo"

class DummyDBRestablecer():
    def __init__(self):
        self.data = {}
        self.committed = False
        self.closed = False

    def query(self, model):
        class DummyQuery:
            def filter(self_inner, *args, **kwargs):
                # Se retorna a sí mismo para poder llamar a first()
                return self_inner
            def first(self_inner):
                return DummyUsuario_Restablecer()
        return DummyQuery()

    # Métodos simulados de la base de datos (dummy)
    def commit(self):
        self.committed = True
    def rollback(self):
        self.committed = False
    def close(self):
        self.closed = True

# Casos de prueba:
# 1. Restablecimiento de contraseña exitoso por respuesta correcta.
def test_Test1RestablerContrasena():
    db = DummyDBRestablecer()
    resultado = Usuario_Servicio.restablecer_contrasena(
        db,
        identificador="ludwinr@gmail.com",
        nueva_contrasena="nueva_contrasena123",
        respuesta_recuperacion="respuesta_correcta"
    )
    assert "mensaje" in resultado
    assert resultado["mensaje"] == "Contraseña restablecida exitosamente"

# 2. Restablecimiento de contraseña fallido por respuesta incorrecta.
def test_Test2RestablerContrasena():
    db = DummyDBRestablecer()
    resultado = Usuario_Servicio.restablecer_contrasena(
        db,
        identificador="ludwinr@gmail.com",
        nueva_contrasena="nueva_contrasena123",
        respuesta_recuperacion="respuesta_incorrecta"
    )
    assert "errores" in resultado
    assert "respuesta_recuperacion" in resultado["errores"]