package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.StoreConfig;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpayCalendar.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpayCalendar.models.StoreConfig;
import com.retailmanager.rmpayCalendar.repositories.StoreConfigRepository;
import com.retailmanager.rmpayCalendar.services.DTO.StoreConfigDTO;

@Service
public class StoreConfigService implements IStoreConfigService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Autowired
    private StoreConfigRepository repository;

    @Override
    @Transactional
    public ResponseEntity<?> save(StoreConfigDTO dto) {
        Long id = dto.getId();
        StoreConfig entity = this.mapper.map(dto, StoreConfig.class);
        if (id == null) {
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, StoreConfigDTO.class), HttpStatus.CREATED);
        } else {
            StoreConfig existing = repository.findById(id).orElse(null);
            if (existing == null) {
                throw new EntidadNoExisteException("StoreConfig con id " + id + " no existe en la Base de datos");
            }
            entity.setId(id);
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, StoreConfigDTO.class), HttpStatus.OK);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long id, StoreConfigDTO dto) {
        StoreConfig existing = repository.findById(id).orElse(null);
        if (existing == null) {
            throw new EntidadNoExisteException("StoreConfig con id " + id + " no existe en la Base de datos");
        }
        StoreConfig updated = this.mapper.map(dto, StoreConfig.class);
        updated.setId(id);
        repository.save(updated);
        return new ResponseEntity<>(this.mapper.map(updated, StoreConfigDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        StoreConfig entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("StoreConfig con id " + id + " no existe en la Base de datos");
        }
        repository.delete(entity);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long id) {
        StoreConfig entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("StoreConfig con id " + id + " no existe en la Base de datos");
        }
        return new ResponseEntity<>(this.mapper.map(entity, StoreConfigDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        List<StoreConfigDTO> list = StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .map(e -> this.mapper.map(e, StoreConfigDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
