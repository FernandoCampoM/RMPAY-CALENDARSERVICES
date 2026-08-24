package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.EmployeeAvailability;

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
import com.retailmanager.rmpayCalendar.models.EmployeeAvailability;
import com.retailmanager.rmpayCalendar.repositories.EmployeeAvailabilityRepository;
import com.retailmanager.rmpayCalendar.services.DTO.EmployeeAvailabilityDTO;

@Service
public class EmployeeAvailabilityService implements IEmployeeAvailabilityService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Autowired
    private EmployeeAvailabilityRepository repository;

    @Override
    @Transactional
    public ResponseEntity<?> save(EmployeeAvailabilityDTO dto) {
        Long id = dto.getId();
        EmployeeAvailability entity = this.mapper.map(dto, EmployeeAvailability.class);
        if (id == null) {
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, EmployeeAvailabilityDTO.class), HttpStatus.CREATED);
        } else {
            EmployeeAvailability existing = repository.findById(id).orElse(null);
            if (existing == null) {
                throw new EntidadNoExisteException("EmployeeAvailability con id " + id + " no existe en la Base de datos");
            }
            entity.setId(id);
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, EmployeeAvailabilityDTO.class), HttpStatus.OK);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long id, EmployeeAvailabilityDTO dto) {
        EmployeeAvailability existing = repository.findById(id).orElse(null);
        if (existing == null) {
            throw new EntidadNoExisteException("EmployeeAvailability con id " + id + " no existe en la Base de datos");
        }
        EmployeeAvailability updated = this.mapper.map(dto, EmployeeAvailability.class);
        updated.setId(id);
        repository.save(updated);
        return new ResponseEntity<>(this.mapper.map(updated, EmployeeAvailabilityDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        EmployeeAvailability entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("EmployeeAvailability con id " + id + " no existe en la Base de datos");
        }
        repository.delete(entity);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long id) {
        EmployeeAvailability entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("EmployeeAvailability con id " + id + " no existe en la Base de datos");
        }
        return new ResponseEntity<>(this.mapper.map(entity, EmployeeAvailabilityDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        List<EmployeeAvailabilityDTO> list = StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .map(e -> this.mapper.map(e, EmployeeAvailabilityDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getByEmployeeId(Long employeeId) {
        List<EmployeeAvailabilityDTO> list = StreamSupport
                .stream(repository.findByEmployeeID(employeeId).spliterator(), false)
                .map(e -> this.mapper.map(e, EmployeeAvailabilityDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
