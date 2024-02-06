package com.retailmanager.rmpaydashboard.exceptionControllers.exceptions;

import lombok.Getter;

@Getter
public class ConfigurationNotFoundException extends RuntimeException {
    private final String llaveMensaje;
  private final String codigo;

  public ConfigurationNotFoundException(CodigoError code) {
    super(code.getCodigo());
    this.llaveMensaje = code.getLlaveMensaje();
    this.codigo = code.getCodigo();
  }

  public ConfigurationNotFoundException(final String message) {
    super(message);
    this.llaveMensaje = CodigoError.CONFIGURATION_NOT_FOUND.getLlaveMensaje();
    this.codigo = CodigoError.CONFIGURATION_NOT_FOUND.getCodigo();
  }
}
