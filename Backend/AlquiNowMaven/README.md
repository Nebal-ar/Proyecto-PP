# AlquiNow — Backend Java (Maven) + Frontend HTML5

Proyecto web para alquileres temporales, conectado a la base **AlquiNow** (MySQL).
Backend con **Servlets + JDBC**, frontend en **HTML5 + JS**.
Versión **Maven**: las librerías se descargan solas, no se arrastran .jar.

---

## 1. La gran ventaja de Maven

No tenés que descargar NINGÚN .jar a mano. Todo está declarado en `pom.xml`:

- API de Servlets (la marca como "provided" → la pone Tomcat)
- Conector MySQL (`mysql-connector-j` 9.7.0)
- jBCrypt (hash de contraseñas)

Cuando abrís el proyecto en NetBeans, Maven baja las 3 automáticamente.
**Olvidate de la carpeta `lib` y del "Add JAR/Folder".**

> Si ya tenías el conector MySQL en "Dependencies", es porque ya estaba
> en el pom.xml. No hay nada que duplicar.

---

## 2. Requisitos

| Qué            | Versión                          |
|----------------|----------------------------------|
| Apache NetBeans| cualquiera reciente              |
| JDK            | 17 o superior                    |
| Apache Tomcat  | **10.1** (usa jakarta.servlet)   |
| MySQL          | 8.0 (admin. con Workbench)       |

> El código usa `jakarta.servlet.*`, que corresponde a **Tomcat 10+**.
> Tenés Tomcat 10.1.56 → perfecto, no hay que cambiar nada.

---

## 3. Cómo abrir el proyecto en NetBeans

1. NetBeans → *File* → *Open Project*.
2. Navegá hasta la carpeta `AlquiNowMaven` (la que tiene el `pom.xml`).
3. Abrila. NetBeans la reconoce como proyecto Maven y empieza a bajar
   las dependencias (esperá a que termine, se ve abajo a la derecha).
4. Clic derecho en el proyecto → *Clean and Build*. Si compila sin errores,
   las librerías ya están OK.

---

## 4. Conectar Tomcat

1. NetBeans → *Tools* → *Servers* → *Add Server*.
2. Elegí **Apache Tomcat or TomEE** → *Next*.
3. En *Server Location* (Catalina Home) apuntá a tu carpeta de Tomcat
   (ej: `C:\apache-tomcat-10.1.56`).
4. Si pide usuario/contraseña podés dejarlos vacíos o poner uno cualquiera
   (no es necesario para correr la app). *Finish*.

Luego: clic derecho en el proyecto → *Properties* → *Run* → elegí el
servidor Tomcat que agregaste.

---

## 5. Base de datos

1. En MySQL Workbench, ejecutá tu script `BDPracticas.sql`
   (crea la base `AlquiNow` y las tablas).
2. Editá `src/main/java/com/alquinow/util/Conexion.java` y poné tu
   usuario y contraseña de MySQL:

```java
private static final String USUARIO = "root";
private static final String PASSWORD = "tu_password"; // <-- cambiar
```

---

## 6. Ejecutar

Clic derecho en el proyecto → *Run*.
Tomcat arranca y se abre el navegador en:

**http://localhost:8080/AlquiNow**

---

## 7. Rutas del backend

| Método | Ruta            | Qué hace                          |
|--------|-----------------|-----------------------------------|
| POST   | `/registro`     | Crea usuario (comprador/vendedor) |
| POST   | `/login`        | Inicia sesión                     |
| GET    | `/logout`       | Cierra sesión                     |
| GET    | `/propiedades`  | Lista / busca propiedades (JSON)  |
| POST   | `/propiedades`  | Crea propiedad (vendedor)         |
| GET    | `/reservas`     | Reservas del comprador (JSON)     |
| POST   | `/reservas`     | Crea / cancela reserva            |

---

## 8. Flujo de prueba

1. Abrí `http://localhost:8080/AlquiNow` → *Crear cuenta* como anfitrión.
2. Login → panel del anfitrión → publicá una propiedad.
3. Cerrá sesión → creá una cuenta como huésped.
4. Buscá la propiedad → *Ver y reservar* → elegí fechas.

---

## 9. Seguridad

- **Contraseñas:** hasheadas con BCrypt, nunca en texto plano.
- **Sesión:** el login guarda el usuario en HttpSession; el
  `AutenticacionFiltro` protege rutas bajo `/privado/*`.
- **SQL Injection:** todo con `PreparedStatement` parametrizado.
