package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ReservationDTO;
import co.edu.udistrital.mdp.back.entities.BranchEntity;
import co.edu.udistrital.mdp.back.entities.ServiceEntity;
import co.edu.udistrital.mdp.back.entities.ReservationEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ReservationDTO> findAll(@RequestParam(required = false) Long userId) {
        List<ReservationEntity> reservations;
        if (userId != null) {
            reservations = reservationService.getReservationsByUserId(userId);
        } else {
            reservations = reservationService.getAllReservations();
        }
        List<ReservationDTO> dtos = modelMapper.map(reservations, new TypeToken<List<ReservationDTO>>() {
        }.getType());
        // ensure foreign key ids are present even if nested objects are null
        for (int i = 0; i < reservations.size(); i++) {
            var entity = reservations.get(i);
            var dto = dtos.get(i);
            dto.setBranchId(entity.getBranch() != null ? entity.getBranch().getId() : null);
            dto.setServiceId(entity.getService() != null ? entity.getService().getId() : null);
            dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        }
        return dtos;
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReservationDTO findOne(@PathVariable("id") Long id) {
        ReservationEntity reservation = reservationService.getReservationById(id);
        ReservationDTO dto = modelMapper.map(reservation, ReservationDTO.class);
        dto.setBranchId(reservation.getBranch() != null ? reservation.getBranch().getId() : null);
        dto.setServiceId(reservation.getService() != null ? reservation.getService().getId() : null);
        dto.setUserId(reservation.getUser() != null ? reservation.getUser().getId() : null);
        return dto;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ReservationDTO create(@RequestBody ReservationDTO reservationDTO) throws IllegalOperationException {
        ReservationEntity reservation = dtoToEntity(reservationDTO);
        ReservationEntity newReservation = reservationService.createReservation(reservation);
        ReservationDTO dto = modelMapper.map(newReservation, ReservationDTO.class);
        dto.setBranchId(newReservation.getBranch() != null ? newReservation.getBranch().getId() : null);
        dto.setServiceId(newReservation.getService() != null ? newReservation.getService().getId() : null);
        dto.setUserId(newReservation.getUser() != null ? newReservation.getUser().getId() : null);
        return dto;
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReservationDTO update(@PathVariable("id") Long id, @RequestBody ReservationDTO reservationDTO)
            throws IllegalOperationException {
        ReservationEntity reservation = dtoToEntity(reservationDTO);
        ReservationEntity updatedReservation = reservationService.updateReservation(id, reservation);
        ReservationDTO dto = modelMapper.map(updatedReservation, ReservationDTO.class);
        dto.setBranchId(updatedReservation.getBranch() != null ? updatedReservation.getBranch().getId() : null);
        dto.setServiceId(updatedReservation.getService() != null ? updatedReservation.getService().getId() : null);
        dto.setUserId(updatedReservation.getUser() != null ? updatedReservation.getUser().getId() : null);
        return dto;
    }

    // helper to convert DTO -> Entity without confusing ModelMapper about branchId
    // vs branch.id
    private ReservationEntity dtoToEntity(ReservationDTO dto) {
        ReservationEntity entity = new ReservationEntity();
        entity.setReservationDate(dto.getReservationDate());
        entity.setReservationStatus(dto.getReservationStatus());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setPetName(dto.getPetName());

        if (dto.getBranch() != null && dto.getBranch().getId() != null) {
            BranchEntity b = new BranchEntity();
            b.setId(dto.getBranch().getId());
            entity.setBranch(b);
        } else if (dto.getBranchId() != null) {
            BranchEntity b = new BranchEntity();
            b.setId(dto.getBranchId());
            entity.setBranch(b);
        }

        if (dto.getService() != null && dto.getService().getId() != null) {
            ServiceEntity s = new ServiceEntity();
            s.setId(dto.getService().getId());
            entity.setService(s);
        } else if (dto.getServiceId() != null) {
            ServiceEntity s = new ServiceEntity();
            s.setId(dto.getServiceId());
            entity.setService(s);
        }

        if (dto.getUserId() != null) {
            co.edu.udistrital.mdp.back.entities.UserEntity u = new co.edu.udistrital.mdp.back.entities.UserEntity();
            u.setId(dto.getUserId());
            entity.setUser(u);
        }

        return entity;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
    }
}