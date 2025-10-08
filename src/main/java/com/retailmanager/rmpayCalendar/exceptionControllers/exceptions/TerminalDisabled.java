package com.retailmanager.rmpayCalendar.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class TerminalDisabled extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public TerminalDisabled(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public TerminalDisabled(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.TERMINAL_DISABLED.getLlaveMensaje();
    this.codigo = CodigoError.TERMINAL_DISABLED.getCodigo();
  }
}
