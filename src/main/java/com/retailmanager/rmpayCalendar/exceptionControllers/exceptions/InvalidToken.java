package com.retailmanager.rmpayCalendar.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class InvalidToken extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public InvalidToken(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public InvalidToken(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.INVALID_TOKEN.getLlaveMensaje();
    this.codigo = CodigoError.INVALID_TOKEN.getCodigo();
  }
}
