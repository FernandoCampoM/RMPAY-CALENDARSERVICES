package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConfigurationNotFoundException;
import com.retailmanager.rmpaydashboard.models.FileModel;
import com.retailmanager.rmpaydashboard.repositories.FileRepository;
import com.retailmanager.rmpaydashboard.repositories.Sys_general_configRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import jakarta.annotation.PostConstruct;

@Service
public class EmailService implements IEmailService{
    private  String SENDGRID_API_KEY = "";
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private Sys_general_configRepository sys_general_configRepository;
    private EmailConfigData emailConfigData;
    /**
     * Método de inicialización que carga la configuración de correo electrónico y maneja los casos en los que
     * no hay una configuración válida.
     */
    @PostConstruct
    public void init() {
        // Aquí puedes ejecutar el código que necesitas después de que sys_general_configRepository haya sido inicializado
        this.emailConfigData = loadAndValid();
        if (this.emailConfigData != null) {
            this.SENDGRID_API_KEY = emailConfigData.getAppKey();
        } else {
            // Si no hay una configuración válida, maneja la situación según tus necesidades
            this.SENDGRID_API_KEY = "";
        }
    }
    
    
    
    
    @Override
    public void sendHtmlEmail(List<String> toList, String subject, String htmlBody) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendHtmlEmail'");
    }

    /**
     * Sends an HTML email with attachments and carbon copy recipients.
     *
     * @param  toList             list of email recipients
     * @param  subject            email subject
     * @param  htmlBody           HTML content of the email
     * @param  cc                 list of carbon copy recipients
     * @param  attachmentData     data of the attachment
     * @param  attachmentFileName name of the attachment file
     */
    @Override
    public void sendHtmlEmailWithAttachmentAndCCO(List<String> toList, String subject, String htmlBody, List<String> cc,
            byte[] attachmentData, String attachmentFileName)  {
                try {
        Email from = new Email(emailConfigData.getEmailFrom()); 

        // Configurar destinatarios
        Personalization personalization = new Personalization();
        for (String recipient : toList) {
            personalization.addTo(new Email(recipient));
        }

        // Configurar destinatarios en copia (CC)
        if (cc != null) {
            for (String ccRecipient : cc) {
                personalization.addCc(new Email(ccRecipient));
            }
        }

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(new Content("text/html", htmlBody));
        mail.addPersonalization(personalization);

        // Adjuntar archivos
        if (attachmentData != null && attachmentFileName != null && !attachmentFileName.isEmpty()) {
            InputStream pdfInputStream = new ByteArrayInputStream(attachmentData);
                    Attachments attachments = new Attachments.Builder(attachmentFileName, pdfInputStream)
                                                         .withType("application/"+obtenerExtension(attachmentFileName))
                                                         .build();
                    mail.addAttachments(attachments);
                    System.out.println("Adjuntando archivo: " + attachmentFileName);
            }
        

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        System.out.println("Enviando correo...");
            Response response = sg.api(request);
            if (response.getStatusCode() != 202) {
                System.out.println("Error al enviar el correo: " + response.getBody());
            }
            if(response.getStatusCode() == 202){
                System.out.println("Correo enviado exitosamente");
            }
        } catch (IOException ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }catch (Exception ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }
        
    }

    /**
     * Notify payment via ATH Movil.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyPaymentATHMovil(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "RECIBO DE PAGO VIA ATH MOVIL";
        String htmlBody = createBodyEmailATHMovil(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }

    /**
     * Notify the payment bank account via email.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyPaymentBankAccount(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "RECIBO DE PAGO VIA CUENTA BANCARIA";
        String htmlBody = createBodyEmailBankAccount(emailData);
        Optional<FileModel> fileModel=this.fileRepository.findById(emailData.getChequeVoidId());
        byte[] file=null;
        if(fileModel.isPresent()){
            file=fileModel.get().getContenido();
        }
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, file, fileModel.get().getNombre());
    }

    /**
     * Notify new register with email data.
     *
     * @param  emailData  email data for the new register
     * @return            void
     */
    @Override
    public void notifyNewRegister(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "NUEVO CLIENTE REGISTRADO EN RMPAY";
        
        String htmlBody = createBodyNewRegistry(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    
    }
    @Override
    public void notifyNewBusiness(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "NUEVO NEGOCIO REGISTRADO EN RMPAY";
        
        String htmlBody = createBodyNewRegistry(emailData);
        htmlBody=htmlBody.replace("-paymethod-", emailData.getPaymethod());
        htmlBody=htmlBody.replace("NUEVO CLIENTE REGISTRADO EN RMPAY", "NUEVO NEGOCIO REGISTRADO EN RMPAY");
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    
    }

    /**
     * Notify rejected payment with email data.
     *
     * @param  emailData  the email body data
     * @return            void
     */
    @Override
    public void notifyRejectedPayment(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "EL PAGO HA SIDO RECHAZADO";
        
        String htmlBody = createBodyPaymentRejected(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    
    }

    /**
     * Notifies an error during registration via email.
     *
     * @param  emailData  the email body data
     */
    @Override
    public void notifyErrorRegister(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailConfigData.getEmailTo());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "CLIENTE SE HA INTENTADO REGISTRAR EN RMPAY PERO FALLÓ";
        
        String htmlBody = createBodyRegistrationError(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }

    /**
     * Notify payment via credit card.
     *
     * @param  emailData   the email body data
     * @return             void
     */
    @Override
    public void notifyPaymentCreditCard(EmailBodyData emailData) {
        List<String> toList = Arrays.asList(emailData.getEmail());
        List<String> cc = Arrays.asList(emailConfigData.getEmailCCO());
        String subject = "RECIBO DE PAGO VIA TARJETA";
        if(emailData.getInvoiceNumber()!=0){
            subject = "RECIBO #"+emailData.getInvoiceNumber()+" DE PAGO CON TARJETA ";
        }
        String htmlBody = createBodyEmailCreditCard(emailData);
        sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, null, null);
    }
    /**
     * Creates an email body for a credit card payment with the provided email data.
     *
     * @param  emailData   the email data used to create the email body
     * @return             the email body for the credit card payment
     */
    private String createBodyEmailCreditCard(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        String mes = "";
        switch (emailData.getExpDateMonth()) {
            case "1":
                mes = "Enero";
                break;
            case "2":
                mes = "Febrero";
                break;
            case "3":
                mes = "Marzo";
                break;
            case "4":
                mes = "Abril";
                break;
            case "5":
                mes = "Mayo";
                break;
            case "6":
                mes = "Junio";
                break;
            case "7":
                mes = "Julio";
                break;
            case "8":
                mes = "Agosto";
                break;
            case "9":
                mes = "Septiembre";
                break;
            case "10":
                mes = "Octubre";
                break;
            case "11":
                mes = "Noviembre";
                break;
            case "12":
                mes = "Diciembre";
                break;
        }
        DecimalFormat formato = new DecimalFormat("#.00");

        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }
        String message = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <style>\n"
                + "        .row{\n"
                + "            margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\n"
                + "        }\n"
                + "        .col1{\n"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%; text-align:right; margin:0px;\n"
                + "        }\n"
                + "        .col2{\n"
                + "            flex: 0 0 41.66667%;max-width: 41.66667%;text-align:left;margin:0 0 0 10px\n"
                + "        }\n"
                + "    </style>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;\">\n"
                + "    <div>\n"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n"
                + "            <tbody>\n"
                + "                <tr>\n"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">\n"
                + "                        <div align=\"center\">\n"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n"
                + "                                <tbody>\n"
                + "                                    <tr>\n"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: gainsboro\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n"
                + "                                                                    <u></u><u></u></span></p>\n"
                + "                                                        </td>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: white\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;margin: 0 auto;padding: 40px;background-color: white;\n"
                + "                                                        border: 3px solid black;\">\n"
                + "                                                            <div class=\"header\" style=\" display: flex;\n"
                + "                                                            justify-content: space-between;\n"
                + "                                                            align-items: center;\">\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;\n"
                + "                                                                text-align: right;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"\" style=\"flex: 2;\n"
                + "                                                                text-align: center; text-align: right;\n"
                + "                                                                margin-left: 10%;\">\n"
                + "                                                                    <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n"
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto;\">\n"
                + "                                                                </div>\n"
                + "                                                            </div><br>\n"
                + "                                                            <u>RECIBO DE PAGO VIA TARJETA DE CREDITO</u>\n"
                + "                                                            <br>\n"
                + "                                                             <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> "
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                                \n"
                + "                                                            \n"
                + "                                                            <u>Información de Cliente:</u>\n"
                + "                                                            <br><br>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \"> \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>\n"
                + "                                                                </div> <br>\n"
                + "                                                            <div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">\n"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>\n"
                + "                                                            </div>\n"
                + "                                                            \n"
                + "                                                            <div style=\"margin-right: 0px;width: 100%;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 100%; max-width: 100%;width: 100%; text-align:right; margin:0px;\">\n";
        if (emailData.getRejectedPayments().isEmpty()) {
            message = message + "                                               <p><strong>" + emailData.getServiceDescription() + ": " + emailData.getServiceValue() + "</strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong><u>+ TERMINALES ADICIONALES: " + (emailData.getAdditionalTerminals() - 1) + " X $" + emailData.getAdditionalTerminalsValue() + "</u> </strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong>TOTAL DE PAGO: $" + emailData.getAmount() + " </strong> \n"
                    + "                                                                    </p>\n";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>\n"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>\n"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br> \n";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br> \n";
        }
        message = message + "                                                   </div>\n"
                + "                                                            </div>\n"                                                         
                + "                                                            <br><br>\n"
                + "                                                            <u>Información de Pago:</u>\n"
                + "                                                            <div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">Tarjeta de Crédito</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">   \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de la tarjeta:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getNameoncard() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de Tarjeta de Crédito:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCreditcarnumber() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Fecha de Expiración:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + mes + " " + emailData.getExpDateYear() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>CVV2:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getSecuritycode() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Tipo de Tarjeta:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getCardType() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Referencia de Pago:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getReferenceNumber() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Total pagado:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">$" + emailData.getAmount() + "</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;\n"
                + "                                                            text-align: justify;\">\n"
                + "                                                                <p>\n"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda hacernos. No dude en ponerse en contacto con nuestro\n"
                + "                                                                    equipo de soporte al cliente en <a href=\\\"mailto:info@retailmanagerpr.com\\\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Atentamente,\n"
                + "                                                                </p>\n"
                + "                                                            </div>\n"
                + "                                                            <div style=\"display: flex;\n"
                + "                                                            align-items: center;\n"
                + "                                                            margin-top: 0px;\">\n"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;\n"
                + "                                                                text-align: center;\n"
                + "                                                                margin-left: 40%;\">\n"
                + "                                                                    <img src=\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\"\n"
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\"max-width: 170px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                    <strong>\n"
                + "                                                                        <p>787-466-2091 <br>\n"
                + "                                                                            601 Ave. Andalucia<br>\n"
                + "                                                                            San Juan PR 00920</p>\n"
                + "                                                                    </strong>\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                    <p>Copyright © IvuControlPR Todos los derechos reservados.</p>\n"
                + "                                                            </div>\n"
                + "                                                        </div>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%; background: #666c74\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td width=\"100%\" valign=\"top\"\n"
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">\n"
                + "                                                                <p class=\"v1MsoNormal\"><span\n"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n"
                + "                                                                        favor no responder a este email. Los correos\n"
                + "                                                                        electrónicos enviados a esta dirección no serán\n"
                + "                                                                        respondidos. <br /><br />Copyright ©\n"
                + "                                                                        IvuControlPR Todos los derechos\n"
                + "                                                                        reservados.</span><u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">\n"
                + "                                                <u></u><u></u>\n"
                + "                                            </p>\n"
                + "                                        </td>\n"
                + "                                    </tr>\n"
                + "                                </tbody>\n"
                + "                            </table>\n"
                + "                        </div>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody>\n"
                + "        </table>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
        return message;
    }
    /**
     * Creates an email body for a bank account with the provided data.
     *
     * @param  emailData  the email body data
     * @return            the constructed email message
     */
    private String createBodyEmailBankAccount(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        String mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ")";
        if (emailData.isAutomaticPayments()) {
            mensageServicio = "Pago por saldo actual($" + emailData.getAmount() + ") y pagos siguientes automatizados";
        }

        DecimalFormat formato = new DecimalFormat("#.00");
        String message = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "\n"
                + "<head>\n"
                + "    \n"
                + "</head>\n"
                + "\n"
                + "<body style=\"font-family: Arial, sans-serif;\n"
                + "margin: 0;\n"
                + "padding: 0;\n"
                + "background-color: #f4f4f4;\">\n"
                + "    <div>\n"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n"
                + "            <tbody>\n"
                + "                <tr>\n"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">\n"
                + "                        <div align=\"center\">\n"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n"
                + "                                <tbody>\n"
                + "                                    <tr>\n"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: gainsboro\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n"
                + "                                                                    <u></u><u></u></span></p>\n"
                + "                                                        </td>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: white\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;\n"
                + "                                                        margin: 0 auto;\n"
                + "                                                        padding: 40px;\n"
                + "                                                        background-color: white;\n"
                + "                                                        border: 3px solid black;\n"
                + "                                                        /* Añade el borde negro */\">\n"
                + "                                                            <div class=\"header\" style=\" display: flex;\n"
                + "                                                            justify-content: space-between;\n"
                + "                                                            align-items: center;\">\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;\n"
                + "                                                                text-align: right;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"\" style=\"flex: 2;\n"
                + "                                                                text-align: center; text-align: right;\n"
                + "                                                                margin-left: 10%;\">\n"
                + "                                                                    <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n"
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto; \">\n"
                + "                                                                </div>\n"
                + "                                                            </div><br>\n"
                + "                                                            <u>RECIBO DE PAGO VIA CUENTA BANCARIA</u>\n"
                + "                                                            <br><br>\n"
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> \n"
                + "                                                                    <p>Fecha de solicitud: " + fechaActual.toString() + "</p>\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                            <br>\n"
                + "                                                            <u>Información de Cliente:</u>\n"
                + "                                                            <br><br>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \"> \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>\n"
                + "                                                                </div> <br>\n"
                + "                                                            <div style=\" margin-right: 0px; display: flex;flex-wrap: wrap;\">\n"
                + "                                                                <u>Servicios solicitados:</u><p style=\"margin:0 0 0 10px;\"><strong>" + mensageServicio + "</strong> </p>\n"
                + "                                                            </div>\n"
                + "                                                            \n"
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">\n";
        if (emailData.getRejectedPayments().isEmpty()) {
            message = message + "                                               <p><strong>" + emailData.getServiceDescription() + ": " + emailData.getServiceValue() + "</strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong><u>+ TERMINALES ADICIONALES: " + (emailData.getAdditionalTerminals() - 1) + " X $" + emailData.getAdditionalTerminalsValue() + "</u> </strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong>TOTAL DE PAGO: $" + formato.format(emailData.getAmount()) + " </strong> \n"
                    + "                                                                    </p>\n";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>\n"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>\n"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br> \n";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br> \n";
        }

        message = message + "                                             </div>\n"
                + "                                                            </div>\n"
                + "                                                            <br><br>\n"
                + "                                                            <u>Información de Pago:</u><br><br>\n"
                + "                                                            \n"
                + "                                                            <div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Método de Pago: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">Cuenta Bancaria</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">   \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Nombre de cuenta: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getAccountNameBank() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de cuenta:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getAccountNumberBank() + "</p>\n"
                + "                                                            </div><div style=\" width:100%;margin-right: 0px; display: flex;flex-wrap: wrap;\">  \n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:right; margin:0px;width:41.66667%;\"><strong>Número de ruta y transito:</strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%; max-width: 41.66667%; text-align:left; margin:0 0 0 10px;width:41.66667%;\">" + emailData.getRouteNumberBank() + "</p>\n"
                + "                                                            </div>\n"
                + "\n"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;\n"
                + "                                                            text-align: justify;\">\n"
                + "                                                                <p>\n"
                + "                                                                    El pago estará siendo procesado por el equipo de contabilidad, una vez esté confirmado el mismo sera aplicado a su cuenta.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Los pagos por ACH pueden tardar de 3 a 5 dias laborales para reflejarse en su cuenta de IVU control.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda tener. No dude en ponerse en contacto con nuestro\n"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Atentamente,\n"
                + "                                                                </p>\n"
                + "\n"
                + "                                                            </div>\n"
                + "                                                            <div style=\"display: flex;\n"
                + "                                                            \n"
                + "                                                            align-items: center;\n"
                + "                                                            margin-top: 0px;\">\n"
                + "\n"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;\n"
                + "                                                                text-align: center;\n"
                + "                                                                 margin-left: 40%;\">\n"
                + "                                                                    <img src=\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\"\n"
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\" max-width: 170px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                    <strong>\n"
                + "                                                                        <p>787-466-2091 <br>\n"
                + "                                                                            601 Ave. Andalucia<br>\n"
                + "                                                                            San Juan PR 00920</p>\n"
                + "                                                                    </strong>\n"
                + "\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                        <p>Copyright © IvuControlPR Todos los derechos reservados.</p>\n"
                + "                                                                </div>\n"
                + "                                                        </div>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%; background: #666c74\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td width=\"100%\" valign=\"top\"\n"
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">\n"
                + "                                                                <p class=\"v1MsoNormal\"><span\n"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n"
                + "                                                                        favor no responder a este email. Los correos\n"
                + "                                                                        electrónicos enviados a esta dirección no serán\n"
                + "                                                                        respondidos. <br /><br />Copyright ©\n"
                + "                                                                        IvuControlPR Todos los derechos\n"
                + "                                                                        reservados.</span><u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">\n"
                + "                                                <u></u><u></u>\n"
                + "                                            </p>\n"
                + "                                        </td>\n"
                + "                                    </tr>\n"
                + "                                </tbody>\n"
                + "                            </table>\n"
                + "                        </div>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody>\n"
                + "        </table>\n"
                + "    </div>\n"
                + "\n"
                + "</body>\n"
                + "\n"
                + "</html>";
        return message;
    }
    
    /**
     * Creates the body of an email for ATH Movil.
     *
     * @param  emailData   the data needed to create the email body
     * @return             the HTML body of the email
     */
    private String createBodyEmailATHMovil(EmailBodyData emailData) {
        LocalDate fechaActual = LocalDate.now();
        DecimalFormat formato = new DecimalFormat("#.00");
        String message = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "\n"
                + "<head>\n"
                + "    \n"
                + "</head>\n"
                + "\n"
                + "<body style=\"font-family: Arial, sans-serif;\n"
                + "margin: 0;\n"
                + "padding: 0;\n"
                + "background-color: #f4f4f4;\">\n"
                + "    <div>\n"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n"
                + "            <tbody>\n"
                + "                <tr>\n"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">\n"
                + "                        <div align=\"center\">\n"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n"
                + "                                <tbody>\n"
                + "                                    <tr>\n"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: gainsboro\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n"
                + "                                                                    <u></u><u></u></span></p>\n"
                + "                                                        </td>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: white\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;\n"
                + "                                                        margin: 0 auto;\n"
                + "                                                        padding: 40px;\n"
                + "                                                        background-color: white;\n"
                + "                                                        border: 3px solid black;\n"
                + "                                                        /* Añade el borde negro */\">\n"
                + "                                                            <div class=\"header\" style=\" display: flex;\n"
                + "                                                            justify-content: space-between;\n"
                + "                                                            align-items: center;\">\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;\n"
                + "                                                                text-align: right;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"\" style=\"flex: 2;\n"
                + "                                                                text-align: center; text-align: right;\n"
                + "                                                                margin-left: 10%;\">\n"
                + "                                                                    <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n"
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto; \">\n"
                + "                                                                </div>\n"
                + "                                                            </div><br>\n"
                + "                                                            <u>RECIBO DE PAGO VIA <img src='https://www.ivucontrolpr.com/static/media/ath-movile-logo.png'\n"
                + "                                                                alt='Logo' class='logo' style=\"max-width: 100px;\n"
                + "                                                                height: auto;\n"
                + "                                                                margin: 0 auto; \"></u>\n"
                + "                                                                <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                    <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> \n"
                + "                                                                        <p>Fecha de solicitud: " + fechaActual.toString() + "</p>\n"
                + "                                                                    </div>\n"
                + "                                                                </div>\n"
                + "                                                            <u>Información de Cliente:</u>\n"
                + "                                                            <br><br>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \"> \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>\n"
                + "                                                                </div> <br>\n"
                + "                                                            <div style=\" margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap;\">\n"
                + "                                                                <u>Servicios solicitados:</u>"
                + "                                                            </div>\n"
                + "                                                            \n"
                + "                                                            <div style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">\n";
//Valida si el pago corresponde a un pago rechazado
        if (emailData.getRejectedPayments().isEmpty()) {
            message = message + "                                                   <p><strong>" + emailData.getServiceDescription() + ": " + emailData.getServiceValue() + "</strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong><u>+ TERMINALES ADICIONALES: " + (emailData.getAdditionalTerminals() - 1) + " X $" + emailData.getAdditionalTerminalsValue() + "</u> </strong> \n"
                    + "                                                                    </p>\n"
                    + "                                                                    <p><strong>TOTAL DE PAGO: $" + emailData.getAmount() + " </strong> </p> \n";
        } else {
            for (int i = 0; i < emailData.getRejectedPayments().size(); i++) {
                message = message + "                                               <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getRejectedPayments().get(i).getInvoiceNumber() + ": $" + emailData.getRejectedPayments().get(i).getTotalAmount() + "</strong></p>\n"
                        + "                                                                <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>\n"
                        + "                                                                <p><strong>TOTAL DE FACTURA: $" + (emailData.getRejectedPayments().get(i).getTotalAmount() + 25.00) + "</strong></p><br> \n";
            }
            message = message + "                                                    <p><strong>TOTAL DE PAGO: $" + (emailData.getAmount()) + "</strong></p><br> \n";
        }
        message = message + "                                          </div>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;\n"
                + "                                                            text-align: justify;\">\n"
                + "                                                                <p>\n"
                + "                                                                    El pago estará siendo revisado por el equipo de contabilidad, una vez esté confirmado el mismo sera aplicado a su cuenta.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Los pagos por ATH móvil pueden tardar 1 o 2 dias laborales para reflejarse en su cuenta de IVU control.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Quedamos a su disposición para cualquier consulta que pueda tener. No dude en ponerse en contacto con nuestro\n"
                + "                                                                    equipo de soporte al cliente en <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a> o al 1-787-466-2091 en cualquier momento.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Atentamente,\n"
                + "                                                                </p>\n"
                + "\n"
                + "                                                            </div>\n"
                + "                                                            <div style=\"display: flex;\n"
                + "                                                            \n"
                + "                                                            align-items: center;\n"
                + "                                                            margin-top: 0px;\">\n"
                + "\n"
                + "                                                                <div class=\"logo-column\" style=\"flex: 1;\n"
                + "                                                                text-align: center;\n"
                + "                                                                 margin-left: 40%;\">\n"
                + "                                                                    <img src=\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\"\n"
                + "                                                                        alt=\"Logo 1\" class=\"logo\" style=\" max-width: 170px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                    <strong>\n"
                + "                                                                        <p>787-466-2091 <br>\n"
                + "                                                                            601 Ave. Andalucia<br>\n"
                + "                                                                            San Juan PR 00920</p>\n"
                + "                                                                    </strong>\n"
                + "\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                            <div class=\"info-column\" style=\" flex: 1;\n"
                + "                                                                text-align: center; margin-left: 0;\">\n"
                + "                                                                        <p>Copyright © IvuControlPR Todos los derechos reservados.</p>\n"
                + "                                                                </div>\n"
                + "                                                        </div>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%; background: #666c74\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td width=\"100%\" valign=\"top\"\n"
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">\n"
                + "                                                                <p class=\"v1MsoNormal\"><span\n"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n"
                + "                                                                        favor no responder a este email. Los correos\n"
                + "                                                                        electrónicos enviados a esta dirección no serán\n"
                + "                                                                        respondidos. <br /><br />Copyright ©\n"
                + "                                                                        IvuControlPR Todos los derechos\n"
                + "                                                                        reservados.</span><u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">\n"
                + "                                                <u></u><u></u>\n"
                + "                                            </p>\n"
                + "                                        </td>\n"
                + "                                    </tr>\n"
                + "                                </tbody>\n"
                + "                            </table>\n"
                + "                        </div>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody>\n"
                + "        </table>\n"
                + "    </div>\n"
                + "\n"
                + "</body>\n"
                + "\n"
                + "</html>";
        return message;
    }
    /**
     * A method to create a body registration error email message.
     *
     * @param  emailData   the email body data used to populate the message
     * @return             the formatted HTML email message
     */
    private String createBodyRegistrationError(EmailBodyData emailData){
        LocalDate fechaActual = LocalDate.now();
        String msg="<!DOCTYPE html>\n" +
"<html>\n" +
"\n" +
"<head> </head>\n" +
"\n" +
"<body style=\"font-family: Arial, sans-serif;margin: 0;padding: 0;background-color: #f4f4f4;\">\n" +
"   <div>\n" +
"      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n" +
"         <tbody>\n" +
"            <tr>\n" +
"               <td style=\"padding: 0in 0in 0in 0in\">\n" +
"                  <div align=\"center\">\n" +
"                     <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n" +
"                        <tbody>\n" +
"                           <tr>\n" +
"                              <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>\n" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                    style=\"width: 100.0%; background: gainsboro\">\n" +
"                                    <tbody>\n" +
"                                       <tr>\n" +
"                                          <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n" +
"                                             <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n" +
"                                                   <u></u><u></u></span></p>\n" +
"                                          </td>\n" +
"                                       </tr>\n" +
"                                    </tbody>\n" +
"                                 </table>\n" +
"                                 <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                    style=\"width: 100.0%; background: white\">\n" +
"                                    <tbody>\n" +
"                                       <tr>\n" +
"                                          <div class=\"container\"\n" +
"                                             style=\"max-width: 600px;                                                        margin: 0 auto;                                                        padding: 40px;                                                        background-color: white;                                                        border: 3px solid black;                                                        /* Añade el borde negro */\">\n" +
"                                             <div class=\"header\"\n" +
"                                                style=\" display: flex;                                                        justify-content: space-between;                                                        align-items: center;\">\n" +
"                                                <div class=\"info-column\"\n" +
"                                                   style=\" flex: 2;                                                        text-align: right;\">\n" +
"                                                </div>\n" +
"                                                <div class=\"\"\n" +
"                                                   style=\"flex: 2;                                                        text-align: center; text-align: right;                                                        margin-left: 10%;\">\n" +
"                                                   <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n" +
"                                                      alt='Logo' class='logo'\n" +
"                                                      style=\"max-width: 300px;                                                                        height: auto;                                                                        margin: 0 auto; \">\n" +
"                                                </div>\n" +
"                                             </div><br> <u>CLIENTE SE HA INTENTADO REGISTRAR EN RMPAY PERO\n" +
"                                                <strong>FALLÓ</strong> </u>\n" +
"                                             <div\n" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n" +
"                                                <div\n" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">\n" +
"                                                   <p>Fecha de solicitud: " + fechaActual.toString() +"</p>"+
"                                                </div>\n" +
"                                             </div>\n" +
"                                             <u>Información de Cliente:</u> <br><br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right; margin:0px;\">\n" +
"                                                   <strong>NOMBRE: </strong>\n" +
"                                                </p>\n" +
"                                                <p\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">\n" +
"                                                   " + emailData.getName() +" ,</p>" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>NEGOCIO: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getBusinessName() +",</p>" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong># MERCHANT: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getMerchantId()+" ,</p>" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong> SERVICIOS SOLICITADOS: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getServiceDescription() +",</p>" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>CANTIDAD DE TERMINALES: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getAdditionalTerminals() +",</p>" +
"                                             </div> <br>\n" +
"                                             <u>Información de Pago:</u> <br><br>\n" +
"                                             -paymethod-\n" +
"                                             <p>Cordilmente, <br> Equipo de Soporte de RMPAY</p>\n" +
"                                             <div\n" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n" +
"                                                <div\n" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">\n" +
"\n" +
"\n" +
"                                                   <div\n" +
"                                                      style=\"display: flex;\n" +
"                                                                                                               \n" +
"                                                                                                               align-items: center;\n" +
"                                                                                                               margin-top: 0px;\">\n" +
"                                                      <div class=\"logo-column\"\n" +
"                                                         style=\"flex: 1;\n" +
"                                                                                                                      text-align: center;\n" +
"                                                                                                                       margin-left: 40%;\">\n" +
"                                                         <img\n" +
"                                                            src=\\\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\\\"\n" +
"                                                            alt=\\\" Logo 1\\\" class=\\\"logo\\\" style=\\\" max-width: 170px;\n" +
"                                                            height: auto; margin: 0 auto;\\\">\n" +
"                                                      </div>\n" +
"                                                      <div class=\"info-column\"\n" +
"                                                         style=\" flex: 1;\n" +
"                                                                                                                      text-align: center; margin-left: 0;\">\n" +
"                                                         <strong>\n" +
"                                                            <p>787-466-2091 <br> 601 Ave. Andalucia<br> San\n" +
"                                                               Juan PR 00920</p>\n" +
"                                                         </strong>\n" +
"                                                      </div>\n" +
"                                                   </div>\n" +
"                                                   <div class=\"info-column\"\n" +
"                                                      style=\" flex: 1;\n" +
"                                                                                                                   text-align: center; margin-left: 0;\">\n" +
"                                                      <p>Copyright © IvuControlPR Todos los derechos reservados.</p>\n" +
"                                                   </div>\n" +
"                                                </div>\n" +
"                                       </tr>\n" +
"                                    </tbody>\n" +
"                                 </table>\n" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>\n" +
"                                 <div align=\"center\">\n" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                       style=\"width: 100.0%; background: #666c74\">\n" +
"                                       <tbody>\n" +
"                                          <tr>\n" +
"                                             <td width=\"100%\" valign=\"top\"\n" +
"                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n" +
"                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n" +
"                                             </td>\n" +
"                                          </tr>\n" +
"                                       </tbody>\n" +
"                                    </table>\n" +
"                                 </div>\n" +
"                                 <div align=\"center\">\n" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                       style=\"width: 100.0%\">\n" +
"                                       <tbody>\n" +
"                                          <tr>\n" +
"                                             <td style=\"padding: 5.25pt 0in 0in 0in\">\n" +
"                                                <p class=\"v1MsoNormal\"><span\n" +
"                                                      style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n" +
"                                                      favor no responder a este email. Los correos electrónicos enviados\n" +
"                                                      a esta dirección no serán respondidos. <br /><br />Copyright ©\n" +
"                                                      IvuControlPR Todos los derechos reservados.</span><u></u><u></u>\n" +
"                                                </p>\n" +
"                                             </td>\n" +
"                                          </tr>\n" +
"                                       </tbody>\n" +
"                                    </table>\n" +
"                                 </div>\n" +
"                                 <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\"> <u></u><u></u> </p>\n" +
"                              </td>\n" +
"                           </tr>\n" +
"                        </tbody>\n" +
"                     </table>\n" +
"                  </div>\n" +
"               </td>\n" +
"            </tr>\n" +
"         </tbody>\n" +
"      </table>\n" +
"   </div>\n" +
"</body>\n" +
"\n" +
"</html>";
        return msg;
    }
    
    /**
     * Creates a body for a payment rejected email based on the provided email data.
     *
     * @param  emailData  the email data used to create the email body
     * @return           the HTML body for the payment rejected email
     */
    private String createBodyPaymentRejected(EmailBodyData emailData){
        if (emailData.getAdditionalTerminals() == 0) {
            emailData.setAdditionalTerminals(1);
        }
        DecimalFormat formato = new DecimalFormat("#.00");

        emailData.setAmount(emailData.getAmount() + 25);

        String body = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif;\n"
                + "margin: 0;\n"
                + "padding: 0;\n"
                + "background-color: #f4f4f4;\">\n"
                + "    <div>\n"
                + "        <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n"
                + "            <tbody>\n"
                + "                <tr>\n"
                + "                    <td style=\"padding: 0in 0in 0in 0in\">\n"
                + "                        <div align=\"center\">\n"
                + "                            <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n"
                + "                                <tbody>\n"
                + "                                    <tr>\n"
                + "                                        <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: gainsboro\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n"
                + "                                                            <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n"
                + "                                                                    <u></u><u></u></span></p>\n"
                + "                                                        </td>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n"
                + "                                            <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                style=\"width: 100.0%; background: white\">\n"
                + "                                                <tbody>\n"
                + "                                                    <tr>\n"
                + "                                                        <div class=\"container\" style=\"max-width: 600px;\n"
                + "                                                        margin: 0 auto;\n"
                + "                                                        padding: 40px;\n"
                + "                                                        background-color: white;\n"
                + "                                                        border: 3px solid black;\n"
                + "                                                        /* Añade el borde negro */\">\n"
                + "                                                            <div class=\"header\" style=\" display: flex;\n"
                + "                                                            justify-content: space-between;\n"
                + "                                                            align-items: center;\">\n"
                + "                                                                <div class=\"info-column\" style=\" flex: 2;\n"
                + "                                                                text-align: right;\">\n"
                + "                                                                </div>\n"
                + "                                                                <div class=\"\" style=\"flex: 2;\n"
                + "                                                                text-align: center; text-align: right;\n"
                + "                                                                margin-left: 10%;\">\n"
                + "                                                                    <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n"
                + "                                                                        alt='Logo' class='logo' style=\"max-width: 300px;\n"
                + "                                                                        height: auto;\n"
                + "                                                                        margin: 0 auto; \">\n"
                + "                                                                </div>\n"
                + "                                                            </div><br>\n"
                + "                                                            <u>Información de Cliente:</u>\n"
                + "                                                            <br><br>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px; \">\n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right; margin:0px;\"><strong>NOMBRE: </strong></p>\n"
                + "                                                                <p style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">" + emailData.getName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px; \"> \n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%; text-align:right;margin:0px\"><strong>NEGOCIO: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">" + emailData.getBusinessName() + ",</p>\n"
                + "                                                            </div>\n"
                + "                                                            <div  style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;\">\n"
                + "                                                                <p class=\"col1\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%; text-align:right;margin:0px\"><strong># MERCHANT: </strong></p>\n"
                + "                                                                <p class=\"col2\" style=\"flex: 0 0 41.66667%;width:41.66667%;\n"
                + "                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px;\">" + emailData.getMerchantId() + ",</p>\n"
                + "                                                                </div> <br>\n"
                + "                                                            <div class=\"content\" style=\"margin-top: 20px;\n"
                + "                                                            text-align: justify;\">\n"
                + "                                                                <p>\n"
                + "                                                                    Estimado Cliente de ivucontrolpr.com,\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Esperamos que se encuentre bien. Queremos informarle que recientemente se intento procesar un pago a traves de \n"
                + "                                                                    ACH en su cuenta, el cual lamentablemente fue rechazado. Como resultado se aplicara una penalidad de $25 por el pago rechazado,\n"
                + "                                                                    que se añadira al saldo de su cuenta.\n"
                + "                                                                </p>\n"
                + "                                                                <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                    <div  style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\"> \n"
                + "                                                                        <p><strong>VALOR FACTURA ANTERIOR #" + emailData.getInvoiceNumber() + ": $" + formato.format(emailData.getAmount() - 25.00) + "</strong></p>\n"
                + "                                                                        <p><strong>PAGO RECHAZADO: $" + formato.format(25.00) + "<br><u>__________________________________</u></strong></p>\n"
                + "                                                                        <p><strong>TOTAL DE PAGO: $" + formato.format(emailData.getAmount()) + "</strong> \n"
                + "                                                                    </div>\n"
                + "                                                                </div>\n"
                + "                                                                <p>\n"
                + "                                                                    Para evitar futuros inconvenientes y asegurarnos de que su cuenta esté al dia, le solicitamos que nos indique \n"
                + "                                                                    cuándo podemos intentar nuevamente el pago a través de ACH o si tiene la intención de utilizar otro método de pago. \n"
                + "                                                                    Su cooperación es fundamental para mantener su cuenta al corriente y sin interrupciones en los servicios que ofrecemos.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Si tiene alguna pregunta o necesita asistencia adicional, no dude en ponerse en contacto con nuestro equipo de servicio al cliente. \n"
                + "                                                                    Estamos aqui para ayudarle en todo lo que necesite.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Agradecemos su atención a este asunto y esperamos poder resolverlo de manera satisfactoria para ambas partes.\n"
                + "                                                                    <br><br>\n"
                + "                                                                    Atentamente,\n"
                + "                                                                </p>\n"
                + "                                                            </div>\n"
                + "                                                            <div style=\"width:100%;margin-right: 0px; display: flex;flex-wrap: wrap; \">\n"
                + "                                                                <div  style=\"flex: 0 0 50%; width:50%; max-width: 50%; text-align:left; margin:0px;\">\n"
                + "                                                                    <p>\n"
                + "                                                                        ivucontrolpr.com<br>\n"
                + "                                                                        Retail Manager PR LLC <br>\n"
                + "                                                                        Ave. Andalucia 601 San Juan PR 00920<br>\n"
                + "                                                                        787-466-2091<br>\n"
                + "                                                                        <a href=\"mailto:info@retailmanagerpr.com\">info@retailmanagerpr.com</a>\n"
                + "                                                                    </p>\n"
                + "                                                                </div>\n"
                + "                                                                <div  style=\"flex: 0 0 50%; width:50%; max-width: 50%; text-align:right; margin:0px;\n"
                + "                                                                display: flex;align-items: center;justify-content: flex-end;\">\n"
                + "                                                                    <img src=\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\"\n"
                + "                                                                        alt=\"Logo 1\"  style=\" max-width: 170px;height: fit-content;\">\n"
                + "                                                                </div>\n"
                + "                                                            </div>\n"
                + "                                                        </div>\n"
                + "                                                    </tr>\n"
                + "                                                </tbody>\n"
                + "                                            </table>\n"
                + "                                            <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span>\n"
                + "                                            </p>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%; background: #666c74\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td width=\"100%\" valign=\"top\"\n"
                + "                                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n"
                + "                                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <div align=\"center\">\n"
                + "                                                <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
                + "                                                    style=\"width: 100.0%\">\n"
                + "                                                    <tbody>\n"
                + "                                                        <tr>\n"
                + "                                                            <td style=\"padding: 5.25pt 0in 0in 0in\">\n"
                + "                                                                <p class=\"v1MsoNormal\"><span\n"
                + "                                                                        style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n"
                + "                                                                        favor no responder a este email. Los correos\n"
                + "                                                                        electrónicos enviados a esta dirección no serán\n"
                + "                                                                        respondidos. <br /><br />Copyright ©\n"
                + "                                                                        IvuControlPR Todos los derechos\n"
                + "                                                                        reservados.</span><u></u><u></u></p>\n"
                + "                                                            </td>\n"
                + "                                                        </tr>\n"
                + "                                                    </tbody>\n"
                + "                                                </table>\n"
                + "                                            </div>\n"
                + "                                            <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\">\n"
                + "                                                <u></u><u></u>\n"
                + "                                            </p>\n"
                + "                                        </td>\n"
                + "                                    </tr>\n"
                + "                                </tbody>\n"
                + "                            </table>\n"
                + "                        </div>\n"
                + "                    </td>\n"
                + "                </tr>\n"
                + "            </tbody>\n"
                + "        </table>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
        return body;
    }

    /**
     * Creates a new email body for the registry.
     *
     * @param  emailData   the email body data used to populate the email body
     * @return             the newly created email body
     */
    private String createBodyNewRegistry(EmailBodyData emailData){
        String msg="<!DOCTYPE html>\n" +
"<html>\n" +
"\n" +
"<head> </head>\n" +
"\n" +
"<body style=\"font-family: Arial, sans-serif;margin: 0;padding: 0;background-color: #f4f4f4;\">\n" +
"   <div>\n" +
"      <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width: 100.0%; background: whitesmoke\">\n" +
"         <tbody>\n" +
"            <tr>\n" +
"               <td style=\"padding: 0in 0in 0in 0in\">\n" +
"                  <div align=\"center\">\n" +
"                     <table border=\"0\" cellpadding=\"0\" style=\"background: whitesmoke\">\n" +
"                        <tbody>\n" +
"                           <tr>\n" +
"                              <td width=\"640\" style=\"width: 480.0pt; padding: .75pt .75pt .75pt .75pt\">\n" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>\n" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                    style=\"width: 100.0%; background: gainsboro\">\n" +
"                                    <tbody>\n" +
"                                       <tr>\n" +
"                                          <td style=\"padding: 3.75pt 3.75pt 3.75pt 3.75pt\">\n" +
"                                             <p class=\"v1MsoNormal\"><span style=\"font-size: 4.0pt\">\n" +
"                                                   <u></u><u></u></span></p>\n" +
"                                          </td>\n" +
"                                       </tr>\n" +
"                                    </tbody>\n" +
"                                 </table>\n" +
"                                 <p class=\"v1MsoNormal\"><u></u> <u></u></p>\n" +
"                                 <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                    style=\"width: 100.0%; background: white\">\n" +
"                                    <tbody>\n" +
"                                       <tr>\n" +
"                                          <div class=\"container\"\n" +
"                                             style=\"max-width: 600px;                                                        margin: 0 auto;                                                        padding: 40px;                                                        background-color: white;                                                        border: 3px solid black;                                                        /* Añade el borde negro */\">\n" +
"                                             <div class=\"header\"\n" +
"                                                style=\" display: flex;                                                        justify-content: space-between;                                                        align-items: center;\">\n" +
"                                                <div class=\"info-column\"\n" +
"                                                   style=\" flex: 2;                                                        text-align: right;\">\n" +
"                                                </div>\n" +
"                                                <div class=\"\"\n" +
"                                                   style=\"flex: 2;                                                        text-align: center; text-align: right;                                                        margin-left: 10%;\">\n" +
"                                                   <img src='https://ivucontrolpr.com/static/media/logo.1ac3ac3b.png'\n" +
"                                                      alt='Logo' class='logo'\n" +
"                                                      style=\"max-width: 300px;                                                                        height: auto;                                                                        margin: 0 auto; \">\n" +
"                                                </div>\n" +
"                                             </div><br> <u>NUEVO CLIENTE REGISTRADO EN RMPAY </u>\n" +
"                                             <div\n" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n" +
"                                                <div\n" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:right; margin:0px;\">\n" +
"                                                   <p>Fecha de solicitud: " + LocalDate.now().toString() +"</p>" +
"                                                </div>\n" +
"                                             </div>\n" +
"                                             <u>Información de Cliente:</u> <br><br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right; margin:0px;\">\n" +
"                                                   <strong>NOMBRE: </strong>\n" +
"                                                </p>\n" +
"                                                <p\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px;\">\n" +
"                                                   " + emailData.getName() +",</p>" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>NEGOCIO: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getBusinessName()+" ,</p>" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong># MERCHANT: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getMerchantId() +",</p>" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong># TELEFONO: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getPhone() +",</p>" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong># NOMBRE DEL NEGOCIO: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getBusinessName() +",</p>" +
"                                             </div> <br>\n" +
"                                             \n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong> SERVICIOS SOLICITADOS: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getServiceDescription() +",</p>" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>CANTIDAD DE TERMINALES: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   " + emailData.getAdditionalTerminals()+" ,</p>" +
"                                             </div> <br>\n" +
"                                             <u>Información de Pago:</u> <br><br>\n" +
"                                             -paymethod-\n" +
"                                             <p>Cordilmente, <br> Equipo de Soporte de RMPAY</p>\n" +
"                                             <div\n" +
"                                                style=\"width:100%;margin-right: 0px;margin-left: -15px; display: flex;flex-wrap: wrap; \">\n" +
"                                                <div\n" +
"                                                   style=\"flex: 0 0 100%; width:100%; max-width: 100%; text-align:center; margin:0px;\">\n" +
"\n" +
"\n" +
"                                                   <div\n" +
"                                                      style=\"display: flex;\n" +
"                                                                                                               \n" +
"                                                                                                               align-items: center;\n" +
"                                                                                                               margin-top: 0px;\">\n" +
"                                                      <div class=\"logo-column\"\n" +
"                                                         style=\"flex: 1;\n" +
"                                                                                                                      text-align: center;\n" +
"                                                                                                                       margin-left: 40%;\">\n" +
"                                                         <img\n" +
"                                                            src=\\\"http://91500b8596c8.sn.mynetname.net:4200/assets/icono.png\\\"\n" +
"                                                            alt=\\\" Logo 1\\\" class=\\\"logo\\\" style=\\\" max-width: 170px;\n" +
"                                                            height: auto; margin: 0 auto;\\\">\n" +
"                                                      </div>\n" +
"                                                      <div class=\"info-column\"\n" +
"                                                         style=\" flex: 1;\n" +
"                                                                                                                      text-align: center; margin-left: 0;\">\n" +
"                                                         <strong>\n" +
"                                                            <p>787-466-2091 <br> 601 Ave. Andalucia<br> San\n" +
"                                                               Juan PR 00920</p>\n" +
"                                                         </strong>\n" +
"                                                      </div>\n" +
"                                                   </div>\n" +
"                                                   <div class=\"info-column\"\n" +
"                                                      style=\" flex: 1;\n" +
"                                                                                                                   text-align: center; margin-left: 0;\">\n" +
"                                                      <p>Copyright © IvuControlPR Todos los derechos reservados.</p>\n" +
"                                                   </div>\n" +
"                                                </div>\n" +
"                                       </tr>\n" +
"                                    </tbody>\n" +
"                                 </table>\n" +
"                                 <p class=\"v1MsoNormal\"><span style=\"display: none\"><u></u> <u></u></span> </p>\n" +
"                                 <div align=\"center\">\n" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                       style=\"width: 100.0%; background: #666c74\">\n" +
"                                       <tbody>\n" +
"                                          <tr>\n" +
"                                             <td width=\"100%\" valign=\"top\"\n" +
"                                                style=\"width: 100.0%; padding: 7.5pt 22.5pt 7.5pt 22.5pt\">\n" +
"                                                <p class=\"v1MsoNormal\"> <u></u><u></u></p>\n" +
"                                             </td>\n" +
"                                          </tr>\n" +
"                                       </tbody>\n" +
"                                    </table>\n" +
"                                 </div>\n" +
"                                 <div align=\"center\">\n" +
"                                    <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n" +
"                                       style=\"width: 100.0%\">\n" +
"                                       <tbody>\n" +
"                                          <tr>\n" +
"                                             <td style=\"padding: 5.25pt 0in 0in 0in\">\n" +
"                                                <p class=\"v1MsoNormal\"><span\n" +
"                                                      style=\"font-size: 7.5pt; font-family: &quot;Helvetica&quot;,&quot;sans-serif&quot;; color: #999999\">Por\n" +
"                                                      favor no responder a este email. Los correos electrónicos enviados\n" +
"                                                      a esta dirección no serán respondidos. <br /><br />Copyright ©\n" +
"                                                      IvuControlPR Todos los derechos reservados.</span><u></u><u></u>\n" +
"                                                </p>\n" +
"                                             </td>\n" +
"                                          </tr>\n" +
"                                       </tbody>\n" +
"                                    </table>\n" +
"                                 </div>\n" +
"                                 <p class=\"v1MsoNormal\" align=\"center\" style=\"text-align: center\"> <u></u><u></u> </p>\n" +
"                              </td>\n" +
"                           </tr>\n" +
"                        </tbody>\n" +
"                     </table>\n" +
"                  </div>\n" +
"               </td>\n" +
"            </tr>\n" +
"         </tbody>\n" +
"      </table>\n" +
"   </div>\n" +
"</body>\n" +
"\n" +
"</html>";
        return msg;
    }
    public EmailConfigData loadAndValid() throws ConfigurationNotFoundException {
        EmailConfigData valueObject = new EmailConfigData();
        Object[] obj1=this.sys_general_configRepository.getEmailConfig();
        Object[] obj=(Object[]) obj1[0];
        valueObject.setAppKey(obj[0].toString());
        valueObject.setEmailFrom( obj[1].toString());
        valueObject.setEmailTo( obj[2].toString());
        valueObject.setEmailCCO( obj[3].toString());
        HashMap<String, String> map = new HashMap<>();
        boolean bandera=false;
        if (valueObject!=null) {
            if(valueObject.getAppKey() == null){
                map.put("Key", "config.email.AppKey not found.");
                bandera=true;
            }
            if(valueObject.getEmailFrom() == null ){
                map.put("AppType", "config.email.emailFrom not found.");
                bandera=true;
            }
            if(valueObject.getEmailCCO() == null ){
                map.put("cid", "config.email.emailCCO not found.");
                bandera=true;
            }
            if(valueObject.getEmailTo()== null ){
                map.put("mid", "config.email.emailTo not found.");
                bandera=true;
            }
            
            if(bandera){
                Gson gson = new Gson();
                String json = gson.toJson(map);
                throw new ConfigurationNotFoundException("[Debug] Email Service Configuration not found."+json);
            }
        }
        return valueObject;
    }
    /**
     * Returns the file extension of the given file name.
     *
     * @param  nombreArchivo   the name of the file
     * @return                 the file extension
     */
    private String obtenerExtension(String nombreArchivo) {
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }
}
