package com.retailmanager.rmpayCalendar.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class MaxTerminalsReached extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public MaxTerminalsReached(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public MaxTerminalsReached(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.MAX_TERMINALS_REACHED.getLlaveMensaje();
    this.codigo = CodigoError.MAX_TERMINALS_REACHED.getCodigo();
  }
}
