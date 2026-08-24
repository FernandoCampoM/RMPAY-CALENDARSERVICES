package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.StaffingRequirement;

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
import com.retailmanager.rmpayCalendar.models.StaffingRequirement;
import com.retailmanager.rmpayCalendar.repositories.StaffingRequirementRepository;
import com.retailmanager.rmpayCalendar.services.DTO.StaffingRequirementDTO;

@Service
public class StaffingRequirementService implements IStaffingRequirementService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Autowired
    private StaffingRequirementRepository repository;

    @Override
    @Transactional
    public ResponseEntity<?> save(StaffingRequirementDTO dto) {
        Long id = dto.getId();
        StaffingRequirement entity = this.mapper.map(dto, StaffingRequirement.class);
        if (id == null) {
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, StaffingRequirementDTO.class), HttpStatus.CREATED);
        } else {
            StaffingRequirement existing = repository.findById(id).orElse(null);
            if (existing == null) {
                throw new EntidadNoExisteException("StaffingRequirement con id " + id + " no existe en la Base de datos");
            }
            entity.setId(id);
            repository.save(entity);
            return new ResponseEntity<>(this.mapper.map(entity, StaffingRequirementDTO.class), HttpStatus.OK);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> update(Long id, StaffingRequirementDTO dto) {
        StaffingRequirement existing = repository.findById(id).orElse(null);
        if (existing == null) {
            throw new EntidadNoExisteException("StaffingRequirement con id " + id + " no existe en la Base de datos");
        }
        StaffingRequirement updated = this.mapper.map(dto, StaffingRequirement.class);
        updated.setId(id);
        repository.save(updated);
        return new ResponseEntity<>(this.mapper.map(updated, StaffingRequirementDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        StaffingRequirement entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("StaffingRequirement con id " + id + " no existe en la Base de datos");
        }
        repository.delete(entity);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long id) {
        StaffingRequirement entity = repository.findById(id).orElse(null);
        if (entity == null) {
            throw new EntidadNoExisteException("StaffingRequirement con id " + id + " no existe en la Base de datos");
        }
        return new ResponseEntity<>(this.mapper.map(entity, StaffingRequirementDTO.class), HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll() {
        List<StaffingRequirementDTO> list = StreamSupport
                .stream(repository.findAll().spliterator(), false)
                .map(e -> this.mapper.map(e, StaffingRequirementDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
