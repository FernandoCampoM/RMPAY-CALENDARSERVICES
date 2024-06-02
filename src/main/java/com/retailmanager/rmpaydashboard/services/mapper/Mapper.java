package com.retailmanager.rmpaydashboard.services.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.retailmanager.rmpaydashboard.models.Sale;
import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;




@Configuration
public class Mapper {
    
    
    /** 
     * @return ModelMapper
     */
    @Bean(name="mapperbase")
    public ModelMapper modelMapper(){
        ModelMapper objMapper= new ModelMapper();


        return objMapper;
    }
}
