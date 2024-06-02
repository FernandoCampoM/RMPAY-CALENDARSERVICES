package com.retailmanager.rmpaydashboard.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class UserDisabled extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public UserDisabled(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public UserDisabled(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.USUARIO_DESACTIVADO.getLlaveMensaje();
    this.codigo = CodigoError.USUARIO_DESACTIVADO.getCodigo();
  }
}
