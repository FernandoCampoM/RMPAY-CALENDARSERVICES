package com.retailmanager.rmpaydashboard.services.services.ResellerServices;


import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Reseller;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerPaymentDTO;


import com.retailmanager.rmpaydashboard.repositories.ResellerRepository;

@Service
public class ResellerServices implements IResellerService {
    @Autowired
    ResellerRepository resellerRepository;
    @Autowired
    @Qualifier("mapperbase")
    ModelMapper mapper;

    @Override
    @Transactional
    public ResponseEntity<?> save(ResellerDTO prmReseller) {
        
        Reseller objReseller=this.resellerRepository.findByUsername(prmReseller.getUsername()).orElse(null);
        if(objReseller!=null){
            throw new EntidadYaExisteException("El Reseller con username "+prmReseller.getUsername()+" ya existe en la Base de datos");
        }

        objReseller=this.mapper.map(prmReseller, Reseller.class);

        objReseller=this.resellerRepository.save(objReseller);
        return new ResponseEntity<>(this.mapper.map(objReseller, ResellerDTO.class),HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(ResellerDTO prmReseller, Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmReseller.getResellerId()+" ya existe en la Base de datos");  
        }
        if(prmReseller.getUsername().compareTo(objReseller.getUsername())!=0){
            Reseller tempReseller=this.resellerRepository.findByUsername(prmReseller.getUsername()).orElse(null);
            if(tempReseller!=null){
                throw new EntidadYaExisteException("El Reseller con username "+prmReseller.getUsername()+" ya existe en la Base de datos");
            }
        }
        
        objReseller.setFirstname(prmReseller.getFirstname());
        objReseller.setLastname(prmReseller.getLastname());
        objReseller.setUsername(prmReseller.getUsername());
        objReseller.setPassword(prmReseller.getPassword());
        objReseller.setAddress(prmReseller.getAddress());
        objReseller.setCompany(prmReseller.getCompany());
        objReseller.setEmail1(prmReseller.getEmail1());
        objReseller.setEmail2(prmReseller.getEmail2());
        objReseller.setImageprofile(prmReseller.getImageprofile());
        objReseller.setPhone(prmReseller.getPhone());
        objReseller.setStatus(prmReseller.isStatus());
        objReseller=this.resellerRepository.save(objReseller);
        return new ResponseEntity<>(this.mapper.map(objReseller, ResellerDTO.class),HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> get(Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        return new ResponseEntity<>(this.mapper.map(objReseller, ResellerDTO.class),HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        Iterable<Reseller> listReseller=this.resellerRepository.findAll();
        List<Reseller> rtaReseller=new ArrayList<>();
        for(Reseller r:listReseller){
            rtaReseller.add(r);
        }
        List<ResellerDTO> rtaDTO=this.mapper.map(rtaReseller, new TypeToken<List<ResellerDTO>>(){}.getType());
        return new ResponseEntity<>(rtaDTO,HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateStatus(Long prmId, boolean status) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        objReseller.setStatus(status);
        objReseller=this.resellerRepository.save(objReseller);
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> delete(Long prmId) {
        Reseller objReseller=this.resellerRepository.findById(prmId).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con resellerId "+prmId+" no existe en la Base de datos");  
        }
        this.resellerRepository.delete(objReseller);
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> exist(Long prmId) {
        return new ResponseEntity<>(this.resellerRepository.existsById(prmId),HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUsername(String userName) {
        Reseller objReseller=this.resellerRepository.findByUsername(userName).orElse(null);
        if(objReseller==null){
            throw new EntidadNoExisteException("El Reseller con username "+userName+" no existe en la Base de datos");  
        }
        return new ResponseEntity<>(this.mapper.map(objReseller, ResellerDTO.class),HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAccountsSold(String prmResellerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountsSold'");
    }

    @Override
    public ResponseEntity<?> getQRcode(Long prmResellerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQRcode'");
    }

    @Override
    public ResponseEntity<?> getAccountsReport() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountsReport'");
    }

    @Override
    public ResponseEntity<?> getAccountsReport(Long prmResellerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountsReport'");
    }

    @Override
    public ResponseEntity<?> getUnpaidAccounts(Long prmResellerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUnpaidAccounts'");
    }

    @Override
    public ResponseEntity<?> doPayment(ResellerPaymentDTO prmPayment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doPayment'");
    }
    
}
