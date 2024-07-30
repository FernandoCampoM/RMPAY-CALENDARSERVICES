package com.retailmanager.rmpaydashboard.services.services.ShiftService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Shift;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;
import com.retailmanager.rmpaydashboard.repositories.ShiftReporsitory;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UsersAppRepository;
import com.retailmanager.rmpaydashboard.security.TokenUtils;
import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;

@Service
public class ShirftService implements IShiftService{
    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;
    @Autowired
    private TerminalRepository serviceDBTerminal;

    @Autowired
    private UsersAppRepository usersAppDBService;
    @Autowired
    private ShiftReporsitory serviceDBShift;

    @Override
    public ResponseEntity<?> openShift(String authToken, ShiftDTO shiftDTO) {

        Terminal objTerminal=null;
        Business objBusiness=null;
        Long terminalId=TokenUtils.getTerminalId(authToken);
        if(terminalId!=null){
            objTerminal=this.serviceDBTerminal.findById(terminalId).orElse(null);
            if(objTerminal!=null){
                objBusiness=objTerminal.getBusiness();
            }else{
                throw new EntidadNoExisteException("Terminal con ID "+terminalId+" no existe en la Base de datos");
            }
        }else{
           HashMap<String, Object> result = new HashMap<>();
           result.put("message", "El token no es valido, debe indicar el ID de terminal");
           return new ResponseEntity<>(result,HttpStatus.UNAUTHORIZED);
        }
        List<UsersBusiness> objUser=this.usersAppDBService.findByPasswordAndBusinessId(String.valueOf(shiftDTO.getPassword()), objBusiness.getBusinessId());
        if(objUser==null || objUser.isEmpty()){
            throw new EntidadNoExisteException("El Empleado con password "+shiftDTO.getPassword()+" no existe en la Base de datos");
        }
        if(objUser.get(0).getEnable()==false){
            throw new UserDisabled("El Empleado con password "+shiftDTO.getPassword()+" esta deshabilitado");
        }

        Shift objShift=serviceDBShift.findByEmployeeAndTerminal(objUser.get(0).getUserBusinessId(), objTerminal.getTerminalId());
        if(objShift!=null){
            HashMap<String, Object> result = new HashMap<>();
            result.put("message", "Ya hay un turno abierto para el empleado con id "+objUser.get(0).getUserBusinessId() + " en el terminal con id "+objTerminal.getTerminalId());
            return new ResponseEntity<>(result,HttpStatus.BAD_REQUEST);
        }
        objShift=new Shift();
        objShift.setInitialBalance(shiftDTO.getInitialBalance());
        objShift.setStartDate(LocalDate.now());
        objShift.setStartTime(LocalTime.now());
        objShift.setUserBusiness(objUser.get(0));
        objShift.setTerminal(objTerminal);
        objShift=this.serviceDBShift.save(objShift);
        ShiftDTO prmShift=this.mapper.map(objShift, ShiftDTO.class);
        prmShift.setPassword(objShift.getUserBusiness().getPassword());
        return new ResponseEntity<>(prmShift,HttpStatus.CREATED);

    }
    
}
