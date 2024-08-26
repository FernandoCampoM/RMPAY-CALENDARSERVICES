package com.retailmanager.rmpaydashboard.services.services.ResellerServices;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.models.Invoice;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerPaymentDTO;

public interface IResellerService {
     /**
     * Guarda un nuevo reseller en la base de datos.
     *
     * @param prmReseller El objeto ResellerDTO que se va a guardar.
     * @return El ResellerDTO guardado.
     */
    public ResponseEntity<?> save(ResellerDTO prmReseller);

    /**
     * Actualiza la información de un reseller existente en la base de datos.
     *
     * @param prmReseller El objeto ResellerDTO con la información actualizada.
     * @param prmId       El identificador del reseller que se va a actualizar.
     * @return El ResellerDTO actualizado.
     */
    public ResponseEntity<?> update(ResellerDTO prmReseller, Long prmId);

    /**
     * Obtiene la información de un reseller basado en su identificador.
     *
     * @param prmId El identificador del reseller que se desea obtener.
     * @return El ResellerDTO correspondiente al identificador proporcionado.
     */
    public ResponseEntity<?> get(Long prmId);

    /**
     * Obtiene una lista de todos los resellers almacenados en la base de datos.
     *
     * @return Una lista de ResellerDTO.
     */
    public ResponseEntity<?> getAll();

    /**
     * Actualiza el estado (status) de un reseller en la base de datos.
     *
     * @param prmId    El identificador del reseller cuyo estado se va a actualizar.
     * @param status   El nuevo estado que se asignará al reseller.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    public ResponseEntity<?> updateStatus(Long prmId, boolean status);

    /**
     * Elimina un reseller de la base de datos basado en su identificador.
     *
     * @param prmId El identificador del reseller que se va a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     */
    public ResponseEntity<?> delete(Long prmId);

    /**
     * Verifica si un reseller con el identificador proporcionado existe en la base de datos.
     *
     * @param prmId El identificador del reseller que se desea verificar.
     * @return true si el reseller existe, false en caso contrario.
     */
    public ResponseEntity<?> exist(Long prmId);

    

    /**
     * Busca un Reseller por nombre de usuario.
     *
     * @param userName El nombre de usuario del Reseller que se desea buscar.
     * @return Un objeto ResponseEntity que contiene los detalles del Reseller encontrado.
     */
    public ResponseEntity<?> findByUsername(String userName);

    

    /**
     * Obtiene las cuentas vendidas de un reseller.
     *
     * @param prmResellerId El identificador del reseller.
     * @return Un objeto ResponseEntity que contiene la lista de cuentas vendidas.
     */
    public ResponseEntity<?> getAccountsSold(Long prmResellerId);

    /**
     * Obtiene el código QR de un reseller.
     *
     * @param prmResellerId Identificador del Reseller.
     * @return Un objeto ResponseEntity que contiene el código QR.
     */
    public ResponseEntity<?> getQRcode(Long prmResellerId);

    /**
     * Obtiene un reporte de cuentas de todos los revendedores.
     *
     * @return Un objeto ResponseEntity que contiene el reporte de cuentas.
     */
    public ResponseEntity<?> getAccountsReport();

    /**
     * Obtiene un reporte de cuentas para un solo Reseller.
     *
     * @param prmResellerId Identificador del Reseller.
     * @return Un objeto ResponseEntity que contiene el reporte de cuentas.
     */
    public ResponseEntity<?> getAccountsReport(Long prmResellerId);

    /**
     * Obtiene una lista de cuentas no pagadas para un reseller específico.
     *
     * @param prmResellerId Identificador del reseller para el cual se obtienen las cuentas no pagadas.
     * @return ResponseEntity con Lista de HashMaps que representan las cuentas no pagadas.
     */
    public ResponseEntity<?> getUnpaidAccounts(Long prmResellerId);

    /**
     * Realiza un pago asociado a un revendedor.
     *
     * @param prmPayment Objeto ResellerPaymentDTO que contiene la información del pago.
     * @return ResponseEntity con Objeto ResellerPaymentDTO actualizado con la información del pago realizado.
     */
    public ResponseEntity<?> doPayment(ResellerPaymentDTO prmPayment);

    public ResponseEntity<?> getPaymentHistory(Long prmResellerId);

    public ResponseEntity<?> addResellerSales(Long prmResellerId, String prmMerchantId, Double prmTotalValue,Double prmCommission, Invoice prmInvoice, String prmServiceDescription); 
}
