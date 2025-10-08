# RMPAYCALENDARSERVICES
 RMPY CALENDAR SERVICES ofrece herramientas y recursos de API que le permiten gestioanar y sincronizar los servicios de Calendario para DASBOARD DE RMPAY
 La documentación de los servicios se encuenra en 
 ```plaintext
   https://documenter.getpostman.com/view/19269592/2sB3QKq9CR
 ```
## Tabla de Contenidos

- [Instalación](#instalación)
- [Configuración](#Configuración)
- [Uso](#uso)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Despliegue en Docker](#despliegue-en-docker)


## Instalación
Asegúrate de tener instalados los siguientes elementos antes de comenzar:

- [Java 20](https://jdk.java.net/20/)
- [SQL Server 2018](https://www.microsoft.com/sql-server/sql-server-downloads)

## Configuración
### Configuración del Servidor

El servidor está configurado para ejecutarse en el puerto 9091. Puedes cambiar el puerto según tus preferencias en `server.port`.

```properties
server.port=9092
```
### Configuración de la Base de Datos

La configuración de la base de datos utilizada en este proyecto se encuentra en el archivo `application.properties`. A continuación, se describen los detalles de la conexión:

- **Nombre de la base de datos**: `POSDPS`
- **Usuario**: `sa`
- **Contraseña**: `********`

La conexión se realiza utilizando SQL Server y se asegura la confianza en el certificado del servidor. La configuración específica es la siguiente:

```properties
spring.datasource.url=jdbc:sqlserver://localhost\\CSE;database=POSDPS;TrustServerCertificate=True
spring.datasource.username=sa
spring.datasource.password=usr_desarrollo
```

## Uso



### Acceso a la Aplicación

1. Abre tu navegador web y visita la siguiente URL:

   ```plaintext
   http://localhost:9092
    ```

## Despliegue en Docker
> ⚠️ **Warning**  
> Antes de proceder a desplegar en el servidor de Producción, asegúrate de que:
> - No elimines las carpetas `C:\RMPAYService\images` y `C:\RMPAYService\sql_data`, ya que contienen la información esencial de la base de datos y el repositorio de imágenes.
> - Estas carpetas son críticas tanto para el funcionamiento de la aplicación como para almacenar las imágenes subidas por los usuarios en sus perfiles y negocios.
Para desplegar `rmpaycalendarService` utilizando Docker, sigue los siguientes pasos:

### 1. Crear la Imagen Docker

Primero, construimos una imagen Docker del proyecto utilizando el plugin `spring-boot-maven-plugin` con el comando:

  ```bash
  mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=rmpaycalendarService 
  ```
Este comando genera una imagen llamada rmpaycalendarService
### 2. Guardar la Imagen Docker
Una vez generada la imagen, la guardamos en un archivo .tar para transferirla al servidor de producción. Ejecuta el siguiente comando para guardar la imagen en la ruta especificada:
  ```bash
  docker save rmpaycalendarService > D:\\Images\\rmpaycalendarService.tar
  ```
Luego, transfiere la imagen al servidor y colócala en la carpeta `C:\\RmpaycalendarService.`
### 3. Cargar y Desplegar la Nueva Imagen en el Servidor
1. Detén el contenedor Docker en ejecución para reemplazarlo con la nueva imagen:
  Puedes realizarlo desde Docker desktop o desde un terminal:
  ```bash
  docker stop <nombre-del-contenedor>
  ```
2. Carga la imagen Docker desde el archivo .tar que transferiste:
  ```bash
  docker load -i C:\\RmpaycalendarService\\rmpaycalendarService.tar
  ```
3. Finalmente, recrea los contenedores con docker-compose:
  ```bash
  docker stop <nombre-del-contenedor>
  ```
Esto reiniciará los contenedores en el servidor con la nueva versión de la aplicación.
Nota: Asegúrate de que tu archivo docker-compose.yml esté correctamente configurado para desplegar la imagen rmpaycalendarService.

