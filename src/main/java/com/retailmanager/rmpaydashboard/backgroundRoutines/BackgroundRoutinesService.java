package com.retailmanager.rmpaydashboard.backgroundRoutines;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
         HashMap<Long, Object> users = new HashMap<Long, Object>();
         LocalDate date = LocalDate.now();
         date=date.minusDays(10);
         List<Terminal> terminals = terminalRepository.getBusinessForPriorNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setPriorNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.priorNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
    }
    @Transactional
    public void lastDayNotificaionEmail(){
        HashMap<Long, Object> users = new HashMap<Long, Object>();
         LocalDate date = LocalDate.now();
         date=date.minusDays(5);
         List<Terminal> terminals = terminalRepository.getBusinessForLastDayNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setLastDayNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.lastDayNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
   }
   @Transactional
   public void afterNotificaionEmail(){
    HashMap<Long, Object> users = new HashMap<Long, Object>();
         LocalDate date = LocalDate.now();
         List<Terminal> terminals = terminalRepository.getBusinessForAfterNotification(date);
         for (Terminal terminal : terminals) {
            if(users.containsKey(terminal.getBusiness().getUser().getUserID())){
                HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(terminal.getBusiness().getUser().getUserID());
                List<String> services=(List<String>) bsuiness.get("services");
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }else{
                HashMap<String, Object> bsuiness = new HashMap<String, Object>();
                List<String> services = new ArrayList<String>();
                services.add("Terminal id: "+terminal.getTerminalId()+" - "+terminal.getService().getServiceName());
                bsuiness.put("services", services);
                bsuiness.put("businessName", terminal.getBusiness().getUser().getName());
                bsuiness.put("userEmail", terminal.getBusiness().getUser().getEmail());
                bsuiness.put("userName", terminal.getBusiness().getUser().getName());
                bsuiness.put("businessId", terminal.getBusiness().getBusinessId());
                users.put(terminal.getBusiness().getUser().getUserID(), bsuiness);
            }
            
            terminal.getBusiness().setAfterNotification(date);
        }
        for (Long key : users.keySet()) {
            HashMap<String, Object> bsuiness = (HashMap<String, Object>) users.get(key);
            emailService.beforeNotificationEmail(bsuiness.get("userEmail").toString(), bsuiness.get("userName").toString(),bsuiness.get("businessName").toString(), (List<String>) bsuiness.get("services"));
        }
         terminalRepository.saveAll(terminals);
    }
}
