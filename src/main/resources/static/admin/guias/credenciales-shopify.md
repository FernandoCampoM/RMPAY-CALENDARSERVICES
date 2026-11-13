# Credenciales y permisos de Shopify

Esta guía explica dónde se consigue cada dato que pide el servicio para conectarse con Shopify,
qué permisos necesita la app y cómo reconocer cada error. Al final hay un **historial de
hallazgos** con lo que hemos ido descubriendo; cada vez que aprendamos algo nuevo sobre cómo
conseguir credenciales o permisos se agrega ahí y se corrigen los pasos de arriba.

> Regla del proyecto: cualquier cambio que afecte a las credenciales, los permisos o la conexión
> con Shopify actualiza esta guía en el mismo commit.

## Qué datos necesita el servicio

| Dato | Dónde se escribe | Ejemplo |
|---|---|---|
| Tienda | Instalador, página *Shopify* | `mitienda.myshopify.com` |
| Client ID | Instalador, página *Shopify* | `84a80e…` (32 caracteres) |
| Client secret | Instalador, página *Shopify* | `shpss_…` |
| Versión de la API | Instalador, página *Shopify* | `2026-10` (la misma que la app) |
| Llave de firma de webhooks | Panel → *Webhooks* → *Llave de firma* (o el instalador) | la que muestra Shopify |

El servicio pide su propio token a Shopify con el Client ID y el Client secret
(`grant_type=client_credentials` contra `https://<tienda>/admin/oauth/access_token`). No hace
falta copiar ningún token `shpat_…` a mano.

## 1. Encontrar el nombre de la tienda (`.myshopify.com`)

1. Entre al admin de Shopify de la tienda.
2. Vaya a *Configuración → Dominios*. El dominio que termina en `.myshopify.com` es el que va en
   el instalador, aunque la tienda use un dominio propio.
3. También aparece en la barra de direcciones del admin: `admin.shopify.com/store/<nombre>` →
   la tienda es `<nombre>.myshopify.com`.

## 2. Crear o abrir la app (Dev Dashboard)

Las apps nuevas se crean en el **Dev Dashboard** de Shopify (`dev.shopify.com/dashboard`). La
opción antigua *Configuración → Apps → Desarrollar apps* ya no permite crear apps nuevas.

1. En el admin de la tienda: *Configuración → Apps → Desarrollar apps → Crear app en el Dev
   Dashboard* (o entre directo a `dev.shopify.com/dashboard`).
2. La app y la tienda deben pertenecer a **la misma organización** de Shopify: solo así funciona
   el inicio de sesión del servicio con Client ID y Client secret.
3. Cree la app (nombre sugerido: *RMPAY Service*).

## 3. Permisos (alcances) de la app

En la app: *Versiones → Crear versión → Acceso → Alcances* (scopes). Escriba exactamente:

```
read_customers,write_inventory,read_locations,read_orders,write_products,write_publications,customer_read_orders
```

| Alcance | Para qué lo usa el servicio |
|---|---|
| `write_products` | Crear y actualizar productos de RM en Shopify |
| `write_inventory` | Actualizar existencias |
| `read_locations` | Saber en qué ubicación se cargan las existencias |
| `write_publications` | Publicar los productos en los canales de venta |
| `read_orders` | Leer los pedidos para facturarlos en RM |
| `read_customers` | Leer el cliente del pedido (nombre y teléfono que van en la factura) |
| `customer_read_orders` | Alcance de la *Customer Account API* (cuentas de clientes). El servicio no lo usa; está en la app configurada y no estorba |

En la misma versión, *Versión de la API de webhooks*: **2026-10**. En el instalador, campo
*Versión de la API de Shopify*, use la misma versión.

Después: **Publicar** (*Release*) la versión e **instalar o actualizar** la app en la tienda
(en la app: *Inicio → Instalar app*, elegir la tienda). Si la app ya estaba instalada, Shopify
pide aceptar los permisos nuevos al actualizar.

## 4. Datos protegidos de clientes (necesario para leer pedidos)

Los alcances **no bastan** para leer pedidos. El pedido es un *objeto protegido* y la consulta
del servicio también lee nombre y teléfono del cliente y la dirección de facturación. Shopify
exige, aparte de `read_orders`, que la app tenga aprobado el **acceso a datos protegidos de
clientes**.

Sin esa aprobación, el registro del servicio muestra:
`This app is not approved to access the Order object`.

Dónde se solicita (en este orden, según dónde se haya creado la app):

1. En la configuración de la app, buscar la sección *Datos protegidos de clientes* /
   *Protected customer data access* → *Solicitar acceso*:
   - nivel 1: *Datos del cliente*, con el motivo (facturación de pedidos en el POS);
   - nivel 2: campos **Nombre**, **Teléfono** y **Dirección**.
   Guardar y publicar una versión nueva de la app.
2. Si la app es del Partner Dashboard: `partners.shopify.com → Apps → (la app) → Solicitudes de
   acceso a API → Datos protegidos de clientes`.
3. Si no aparece en ninguno de los dos (nos pasó, ver historial): preguntar al soporte de
   Shopify Partners citando el error exacto.

**Alternativa que no depende de esa aprobación:** los webhooks creados en el admin de la tienda
(*Configuración → Notificaciones → Webhooks*) envían el pedido al servicio sin pasar por la app.
Se configuran en el panel → *Webhooks*, que trae los pasos. Para eso la tienda necesita llegar
al servicio por HTTPS público: ver [la guía del túnel](tunel.md).

## 5. Client ID y Client secret

1. En el Dev Dashboard abra la app → *Configuración* (*Settings*).
2. Copie **Client ID** y **Client secret** (este último empieza por `shpss_`; use el botón para
   mostrarlo o copiarlo).
3. Péguelos en el instalador, página *Shopify*. Para cambiarlos después, vuelva a ejecutar el
   instalador: conserva el resto de la configuración.

Si se regenera el secreto en Shopify, el anterior deja de servir: hay que actualizarlo en el
instalador.

## 6. Llave de firma de los webhooks

- Webhooks creados en el admin de la tienda: la llave aparece **debajo de la lista de webhooks**
  (*Tus webhooks se firmarán con …*). Se pega en el panel → *Webhooks* → *Llave de firma*.
- Webhooks creados desde la app: se firman con el **Client secret** de la app.

## Cómo comprobar que las credenciales funcionan

En el registro `logs\RMPAYService.out.log` de la carpeta de instalación:

| Mensaje | Qué significa | Qué hacer |
|---|---|---|
| `productSet` con `userErrors: []` | Productos funcionando | Nada |
| `Access denied` / `Required access` | Falta un alcance | Revisar el paso 3 y publicar la versión |
| `not approved to access the Order object` | Faltan datos protegidos de clientes | Paso 4 |
| `API Access has been disabled` | La app está desactivada o desinstalada en esa tienda, o es otra app | Comprobar en la tienda (*Configuración → Apps*) que la app instalada es la del Client ID configurado |
| `invalid_client` al pedir el token | Client ID o Client secret incorrectos, o la app no es de la misma organización que la tienda | Pasos 2 y 5 |

## Historial de hallazgos

Cada entrada dice qué aprendimos y si está comprobado en una tienda real.

| Fecha | Hallazgo | Estado |
|---|---|---|
| 2026-11 | Con la tienda anterior (`n126yv-yy.myshopify.com`) el servicio leía pedidos sin pedir datos protegidos. Al pasar a `retailmanager.myshopify.com` (historial de `application.properties`) empezó el error `not approved to access the Order object`. El código y la versión de la API (2026-04) son los mismos. | Comprobado |
| 2026-11 | Con los alcances `write_products,write_inventory,read_locations,write_publications,read_orders` los productos funcionan: 1408 creados sin error y 0 errores de alcance en el registro. El bloqueo de pedidos no es de alcances. | Comprobado |
| 2026-11 | En la app del Dev Dashboard no apareció la opción *Protected customer data access → Request access*. | Pendiente de soporte de Shopify |
| 2026-11-12 | La app con Client ID `84a80e…` responde `API Access has been disabled` en `retailmanager.myshopify.com`: esa app no es la que está activa en la tienda. | Comprobado |
| 2026-11 | Los webhooks creados en el admin de la tienda no dependen de la aprobación de la app. | Por confirmar en `retailmanager` |
| 2026-11-12 | **Engañoso:** si la búsqueda no encuentra ningún pedido, Shopify responde `"orders": []` sin error aunque la app no tenga aprobados los datos protegidos. El `not approved to access the Order object` solo aparece cuando hay pedidos que devolver. Que la consulta "funcione" no prueba el permiso: probar con un rango de fechas que tenga pedidos. En `retailmanager` el permiso **sigue sin aprobar** (22:29 y 22:48, con la fecha movida al 12-nov). Desde esta fecha, si Shopify niega la consulta el registro muestra `Error al sincronizar facturas: Shopify rechazó la consulta de pedidos: …` y el servicio **no avanza** `lastSyncDate`: al aprobarse el permiso, los pedidos pendientes se leen solos. | Comprobado |
| 2026-11-12 | A las 23:15, con `lastSyncDate` en el inicio del 12-nov, Shopify devolvió 5 pedidos de `retailmanager`: la app **ya puede leer pedidos** con datos protegidos. Entre las 22:48 (denegado) y las 23:15 se reinstaló el servicio; falta confirmar si el permiso cambió en Shopify en ese lapso. | Comprobado (causa por confirmar) |
| 2026-11-12 | Si la consulta de pedidos responde bien pero trae 0 pedidos, revisar en la respuesta de Shopify el bloque `search → warnings`: con `created_at:>2026-11-13T03:06:18Z` sin comillas Shopify avisa `Invalid search field` y solo usa la fecha. Es un error del filtro del servicio, no de permisos. Corregido: el servicio ahora envía la fecha entre comillas (`created_at:>'…'`). Para recuperar pedidos que quedaron atrás se puede mover `config.lastSyncDate` (tabla `Sys_general_config`) hacia atrás: los pedidos ya facturados no se repiten. | Comprobado |
| 2026-11-12 | Configuración que funciona en `retailmanager` (productos y pedidos): versión de la API **2026-10** y alcances `read_customers,write_inventory,read_locations,read_orders,write_products,write_publications,customer_read_orders`. Frente a la lista anterior se agregaron `read_customers` (cliente del pedido) y `customer_read_orders` (Customer Account API, no lo usa el servicio). El valor por defecto del instalador sigue siendo 2026-04: escribir 2026-10 al instalar. | Comprobado con estos alcances; la versión que usa el servicio instalado está por confirmar |
