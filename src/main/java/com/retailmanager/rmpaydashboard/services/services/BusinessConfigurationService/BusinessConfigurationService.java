package com.retailmanager.rmpaydashboard.services.services.BusinessConfigurationService;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.BusinessConfiguration;
import com.retailmanager.rmpaydashboard.repositories.BusinessConfigurationRepository;
import com.retailmanager.rmpaydashboard.repositories.BusinessRepository;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessConfigurationDTO;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessConfigurationMini;

@Service
public class BusinessConfigurationService implements IBusinessConfigurationService {
    @Autowired
    private BusinessConfigurationRepository configRepository;
    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    /**
     * Creates a new BusinessConfiguration entity using the provided BusinessConfigurationDTO.
     *
     * @param config the BusinessConfigurationDTO containing the configuration data
     * @return a ResponseEntity containing the saved BusinessConfigurationDTO or an error message
     * @throws EntidadNoExisteException if the Business with the given businessId does not exist
     * @throws EntidadYaExisteException if the BusinessConfiguration with the given key already exists for the Business with the given businessId
     */
    @Override
    @Transactional
    public ResponseEntity<?> create(BusinessConfigurationDTO config) {
        Business business = businessRepository.findById(config.getBusinessId()).orElse(null);

        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + config.getBusinessId() + " no existe en la Base de datos");
        }
        BusinessConfiguration newConfig=null;
        newConfig= configRepository.findByKey( config.getConfigKey(),config.getBusinessId());

        if(newConfig!=null){
            throw new EntidadYaExisteException("El BusinessConfiguration con key " + config.getConfigKey() + " ya existe en la Base de datos para el Business con businessId "+config.getBusinessId());
        }
        newConfig=this.mapper.map(config, BusinessConfiguration.class);
        newConfig.setBusiness(business);
        newConfig.setCreatedAt(LocalDateTime.now());
        newConfig.setUpdatedAt(LocalDateTime.now());
        newConfig=configRepository.save(newConfig);
        config.setConfigurationid(newConfig.getConfigurationid());
        config.setCreatedAt(newConfig.getCreatedAt());
        config.setUpdatedAt(newConfig.getUpdatedAt());

        return new ResponseEntity<>(config,HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public ResponseEntity<?> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    @Override
    public ResponseEntity<?> update(Long id, BusinessConfigurationDTO config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public ResponseEntity<?> delete(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long businessId, List<BusinessConfigurationMini> config) {
        Business business = businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        for (BusinessConfigurationMini conf : config) {
            BusinessConfiguration newConfig=null;
            newConfig= configRepository.findByKey( conf.getConfigKey(),businessId);
            if(newConfig!=null){
                newConfig.setValue(conf.getValue());
                newConfig.setUpdatedAt(LocalDateTime.now());
                configRepository.save(newConfig);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets all BusinessConfigurations by a given Start key for a given Business.
     * 
     * @param configKey the key to search for
     * @param businessId the ID of the Business to find the configurations for
     * @return a ResponseEntity containing a list of BusinessConfigurationMini objects
     *         or throws an EntidadNoExisteException if the Business does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllByKey(String configKey, Long businessId) {
        Business business = businessRepository.findById(businessId).orElse(null);

        if (business == null) {
            throw new EntidadNoExisteException("El Business con businessId " + businessId + " no existe en la Base de datos");
        }
        if(configKey.contains(".")){
            configKey=configKey.split("\\.")[0];
        }
        configKey=configKey+"%";
        
        List<BusinessConfiguration> config =  configRepository.findByStartKey(configKey,businessId);
        List<BusinessConfigurationMini> configMini = this.mapper.map(config,new TypeToken<List<BusinessConfigurationMini>>(){}.getType());
        
        return new ResponseEntity<>(configMini,HttpStatus.OK);
    }
}
