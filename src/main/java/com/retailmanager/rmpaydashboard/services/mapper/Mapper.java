package com.retailmanager.rmpaydashboard.services.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;





@Configuration
public class Mapper {
    
    
    /** 
     * @return ModelMapper
     */
    @Bean(name="mapperbase")
    public ModelMapper modelMapper(){
        ModelMapper objMapper= new ModelMapper();
        objMapper.getConfiguration().setPropertyCondition(context -> 
        !(context.getSource() instanceof org.hibernate.collection.spi.PersistentCollection));
        
        return objMapper;
    }
     
}
