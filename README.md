# RMPAYDASHBOARD
 RMPY DASHBOARD ofrece  herramientas y recursos de API que le permiten gestioanar y sincronizar los servicios ofrecidos por la app de RMPAY.
 La documentación de los servicios se encuenra en 
 ```plaintext
   https://documenter.getpostman.com/view/19269592/2sA2r9WiES
 ```
## Tabla de Contenidos

- [Instalación](#instalación)
- [Configuración](#Configuración)
- [Uso](#uso)
- [Estructura del Proyecto](#estructura-del-proyecto)


## Instalación
Asegúrate de tener instalados los siguientes elementos antes de comenzar:

- [Java 20](https://jdk.java.net/20/)
- [SQL Server 2018](https://www.microsoft.com/sql-server/sql-server-downloads)

## Configuración
### Configuración del Servidor

El servidor está configurado para ejecutarse en el puerto 9091. Puedes cambiar el puerto según tus preferencias en `server.port`.

```properties
server.port=9091 
```
### Configuración de la Base de Datos

La configuración de la base de datos utilizada en este proyecto se encuentra en el archivo `application.properties`. A continuación, se describen los detalles de la conexión:

- **Nombre de la base de datos**: `RMPAY`
- **Usuario**: `usr_desarrollo`
- **Contraseña**: `usr_desarrollo`

La conexión se realiza utilizando SQL Server y se asegura la confianza en el certificado del servidor. La configuración específica es la siguiente:

```properties
spring.datasource.url=jdbc:sqlserver://localhost;database=RMPAY;TrustServerCertificate=True
spring.datasource.username=usr_desarrollo
spring.datasource.password=usr_desarrollo
```

## Uso

Después de iniciar la aplicación, se crea automáticamente un usuario por defecto para la autenticación. Utiliza las siguientes credenciales para acceder a la aplicación:

- **Nombre de usuario:** rmpayadmin
- **Contraseña:** rmpayadmin

### Acceso a la Aplicación

1. Abre tu navegador web y visita la siguiente URL:

   ```plaintext
   http://localhost:9091
    ```
