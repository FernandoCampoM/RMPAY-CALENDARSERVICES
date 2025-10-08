package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.AvailableSchedulesDTO;

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
import com.retailmanager.rmpayCalendar.models.AvailableSchedules;
import com.retailmanager.rmpayCalendar.repositories.AvailableSchedulesRepository;
import com.retailmanager.rmpayCalendar.services.DTO.AvailableSchedulesDTO;
@Service
public class AvailableSchedulesService implements IAvailableSchedulesService {

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Autowired
    private AvailableSchedulesRepository availableSchedulesRepository;

    /* @Autowired
    private UsersAppRepository employeeRepository; */

    @Override
    @Transactional
    public ResponseEntity<?> save(AvailableSchedulesDTO prmSchedule) {
        Long id = prmSchedule.getAsId();
        AvailableSchedules schedule = this.mapper.map(prmSchedule, AvailableSchedules.class);

        if (id == null) {
           /*  UsersBusiness employee = employeeRepository.findById(prmSchedule.getEmployeeId()).orElse(null);
            if (employee == null) {
                throw new EntidadNoExisteException(
                    "El UsersBusiness con userBusinessId " + prmSchedule.getEmployeeId() + " no existe en la Base de datos");
            } */
            //schedule.setEmployee(employee);
            availableSchedulesRepository.save(schedule);
            prmSchedule = this.mapper.map(schedule, AvailableSchedulesDTO.class);
            //prmSchedule.setEmployeeId(schedule.getEmployee().getUserBusinessId());
            return new ResponseEntity<>(prmSchedule, HttpStatus.CREATED);
        } else {
            schedule = availableSchedulesRepository.findById(id).orElse(null);
            if (schedule == null) {
                throw new EntidadNoExisteException(
                    "El AvailableSchedule con id " + id + " no existe en la Base de datos");
            }
            schedule.setTitle(prmSchedule.getTitle());
            schedule.setDuration(prmSchedule.getDuration());
            availableSchedulesRepository.save(schedule);
            prmSchedule = this.mapper.map(schedule, AvailableSchedulesDTO.class);
            //prmSchedule.setEmployeeId(schedule.getEmployee().getUserBusinessId());
            return new ResponseEntity<>(prmSchedule, HttpStatus.OK);
        }
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        AvailableSchedules schedule = availableSchedulesRepository.findById(id).orElse(null);
        if (schedule == null) {
            throw new EntidadNoExisteException("El AvailableSchedule con id " + id + " no existe en la Base de datos");
        }
        availableSchedulesRepository.delete(schedule);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> findById(Long id) {
        AvailableSchedules schedule = availableSchedulesRepository.findById(id).orElse(null);
        if (schedule == null) {
            throw new EntidadNoExisteException("El AvailableSchedule con id " + id + " no existe en la Base de datos");
        }
        AvailableSchedulesDTO scheduleDTO = this.mapper.map(schedule, AvailableSchedulesDTO.class);
        //scheduleDTO.setEmployeeId(schedule.getEmployee().getUserBusinessId());
        return new ResponseEntity<>(scheduleDTO, HttpStatus.OK);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(Long employeeId) {
        Iterable<AvailableSchedules> schedules = availableSchedulesRepository.findByEmployeeId(employeeId);
        List<AvailableSchedulesDTO> scheduleDTOs = StreamSupport
            .stream(schedules.spliterator(), false)
            .map(schedule -> {
                AvailableSchedulesDTO dto = this.mapper.map(schedule, AvailableSchedulesDTO.class);
                //dto.setEmployeeId(schedule.getEmployee().getUserBusinessId());
                return dto;
            })
            .collect(Collectors.toList());
        return new ResponseEntity<>(scheduleDTOs, HttpStatus.OK);
    }
@Override
@Transactional(readOnly = true)
public ResponseEntity<?> getAllByBusinessId(Long businessId) {
    Iterable<AvailableSchedules> schedules = availableSchedulesRepository.findAll();
    List<AvailableSchedulesDTO> scheduleDTOs = StreamSupport
        .stream(schedules.spliterator(), false)
        .map(schedule -> {
            AvailableSchedulesDTO dto = this.mapper.map(schedule, AvailableSchedulesDTO.class);
            //dto.setEmployeeId(schedule.getEmployee().getUserBusinessId());
            return dto;
        })
        .collect(Collectors.toList());
    return new ResponseEntity<>(scheduleDTOs, HttpStatus.OK);
}

@Override
@Transactional
public ResponseEntity<?> update(Long asId, AvailableSchedulesDTO prmSchedule) {
    AvailableSchedules objSchedule=availableSchedulesRepository.findById(asId).orElse(null);
        if(objSchedule==null){
            throw new EntidadNoExisteException("El ScheduleCalendar con id "+asId+" no existe en la Base de datos");
        }
        objSchedule.setDuration(prmSchedule.getDuration());
        objSchedule.setTitle(prmSchedule.getTitle());
        availableSchedulesRepository.save(objSchedule);
        AvailableSchedulesDTO objScheduleCalendarDTO=this.mapper.map(objSchedule, AvailableSchedulesDTO.class);
        //objScheduleCalendarDTO.setEmployeeId(objSchedule.getEmployee().getUserBusinessId());
        return new ResponseEntity<AvailableSchedulesDTO>(objScheduleCalendarDTO, HttpStatus.OK);
}



}
