package com.retailmanager.rmpaydashboard.backgroundRoutines;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;

import jakarta.transaction.Transactional;

@Component
public class BackgroundRoutinesService {
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    IEmailService emailService;
    @Modifying
    public void deactivateExpiredTerminals(){
        List<Terminal> terminals = terminalRepository.findByExpirationDateBefore(LocalDate.now());
        for (Terminal terminal : terminals) {
            terminalRepository.deactivateExpiredTerminals(terminal.getTerminalId());
        }
    }
    @Transactional
    public void priorNotificaionEmail(){
         LocalDate date = LocalDate.now();
         date=date.minusDays(30);
         List<Terminal> terminals = terminalRepository.getBusinessForPriorNotification(date);
         for (Terminal terminal : terminals) {
            emailService.priorNotificationEmail(terminal.getBusiness().getUser().getEmail(), terminal.getBusiness().getUser().getName(), terminal.getBusiness().getName(), terminal.getBusiness().getMerchantId());
            terminal.getBusiness().setPriorNotification(date);
        }
         terminalRepository.saveAll(terminals);
    }
    @Transactional
    public void lastDayNotificaionEmail(){
        LocalDate date = LocalDate.now();
        List<Terminal> terminals = terminalRepository.getBusinessForLastDayNotification(date);
        for (Terminal terminal : terminals) {
           emailService.lastDayNotificationEmail(terminal.getBusiness().getUser().getEmail(), terminal.getBusiness().getUser().getName(), terminal.getBusiness().getName(), terminal.getBusiness().getMerchantId());
           terminal.getBusiness().setLastDayNotification(date);
        }
        terminalRepository.saveAll(terminals);
   }
   @Transactional
   public void afterNotificaionEmail(){
    LocalDate date = LocalDate.now();
    date=date.plusDays(5);
    List<Terminal> terminals = terminalRepository.getBusinessForAfterNotification(date);
    for (Terminal terminal : terminals) {
       emailService.beforeNotificationEmail(terminal.getBusiness().getUser().getEmail(), terminal.getBusiness().getUser().getName(), terminal.getBusiness().getName(), terminal.getBusiness().getMerchantId(), terminal.getExpirationDate());
       terminal.getBusiness().setAfterNotification(date);    
    }
        terminalRepository.saveAll(terminals);
    }
}
