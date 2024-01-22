package com.retailmanager.rmpaydashboard.exceptionControllers.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CodigoError {

        ERROR_GENERICO("GC-0001", "ERROR GENERICO"),
        ENTIDAD_YA_EXISTE("GC-0002", "ERROR ENTIDAD YA EXISTE"),
        ENTIDAD_NO_ENCONTRADA("GC-0003", "Entidad no encontrada"),
        CREDENCIALES_INVALIDAS("GC-0005", "Error al iniciar sesión, compruebe sus credenciales y vuelva a intentarlo"),
        USUARIO_DESHABILITADO("GC-0006",
                        "El usuario no ha sido verificado, por favor revise su correo para verificar su cuenta"),
        UNAUTHORISED("GC-0007", "El usuario no tiene acceso a éste recurso");
        private final String codigo;
        private final String llaveMensaje;
}
