package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

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

@Service
public class EmailService implements IEmailService{
    private  String SENDGRID_API_KEY = "";
     
    private Sys_general_configRepository sys_general_configRepository;
    private EmailConfigData emailConfigData;
    public EmailService(){
        emailConfigData =sys_general_configRepository.getEmailConfig();
        SENDGRID_API_KEY = emailConfigData.getKey();
    }
    
    @Override
    public void sendHtmlEmail(List<String> toList, String subject, String htmlBody) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendHtmlEmail'");
    }

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
                    Attachments attachments = new Attachments.Builder(attachmentFileName+".pdf", pdfInputStream)
                                                         .withType("application/pdf")
                                                         .build();
                    mail.addAttachments(attachments);
            }
        

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        
            Response response = sg.api(request);
            if (response.getStatusCode() != 202) {
                System.out.println("Error al enviar el correo: " + response.getBody());
            }
        } catch (IOException ex) {
            System.out.println("Error al enviar el correo: " + ex.getMessage());
        }
        
    }

    @Override
    public void notifyPaymentATHMovil(EmailBodyData emailData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyPaymentATHMovil'");
    }

    @Override
    public void notifyPaymentBankAccount(EmailBodyData emailData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyPaymentBankAccount'");
    }

    @Override
    public void notifyNewRegister(EmailBodyData emailData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyNewRegister'");
    }

    @Override
    public void notifyRejectedPayment(EmailBodyData emailData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyRejectedPayment'");
    }

    @Override
    public void notifyErrorRegister(EmailBodyData emailData) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifyErrorRegister'");
    }

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
                + "                                                            <strong>FECHA DE RENOVACION:</strong>" + emailData.getExpirationDate().toString() + "\n"
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
    private String createBodyRegistrationError(EmailBodyData emailData){
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
"                                                   <p>Fecha de solicitud: \" + fechaActual.toString() </p>\n" +
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
"                                                   \" + business.getCustomerName() ,</p>\n" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>NEGOCIO: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                max-width: 41.66667%;text-align:left;margin:0 0 0 10px; \">\n" +
"                                                   \" + business.getBussinessname() ,</p>\n" +
"                                             </div>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong># MERCHANT: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   \" + business.getMerchantnumber() ,</p>\n" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong> SERVICIOS SOLICITADOS: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   \" + business.getMerchantnumber() ,</p>\n" +
"                                             </div> <br>\n" +
"                                             <div\n" +
"                                                style=\"display: flex;flex-wrap: wrap;width:100%; margin-right: 0px;margin-left: -15px; \">\n" +
"                                                <p class=\"col1\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%; text-align:right;margin:0px\">\n" +
"                                                   <strong>CANTIDAD DE TERMINALES: </strong>\n" +
"                                                </p>\n" +
"                                                <p class=\"col2\"\n" +
"                                                   style=\"flex: 0 0 41.66667%;width:41.66667%;                                                                    max-width: 41.66667%;text-align:left ;margin:0 0 0 10px; \">\n" +
"                                                   \" + business.getMerchantnumber() ,</p>\n" +
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
    
}
