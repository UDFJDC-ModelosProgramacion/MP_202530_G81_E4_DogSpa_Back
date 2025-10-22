package co.edu.udistrital.mdp.back.controllers;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.dto.ReservationDTO;
import co.edu.udistrital.mdp.back.entities.ReservationEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<ReservationDTO> findAll() {
        List<ReservationEntity> reservations = reservationService.getAllReservations();
        return modelMapper.map(reservations, new TypeToken<List<ReservationDTO>>() {}.getType());
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReservationDTO findOne(@PathVariable("id") Long id) {
        ReservationEntity reservation = reservationService.getReservationById(id);
        return modelMapper.map(reservation, ReservationDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public ReservationDTO create(@RequestBody ReservationDTO reservationDTO) throws IllegalOperationException {
        ReservationEntity reservation = modelMapper.map(reservationDTO, ReservationEntity.class);
        ReservationEntity newReservation = reservationService.createReservation(reservation);
        return modelMapper.map(newReservation, ReservationDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ReservationDTO update(@PathVariable("id") Long id, @RequestBody ReservationDTO reservationDTO)
            throws IllegalOperationException {
        ReservationEntity reservation = modelMapper.map(reservationDTO, ReservationEntity.class);
        ReservationEntity updatedReservation = reservationService.updateReservation(id, reservation);
        return modelMapper.map(updatedReservation, ReservationDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
    }
}