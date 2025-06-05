/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Mapper;

import Models.Reservation;
import Dto.ReservationDTO;
import Utilities.DataBaseManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationMapper {
  public static ReservationDTO toDTO(Reservation r) {
    ReservationDTO d = new ReservationDTO();
    d.setId(r.getId());
    d.setUserName(r.getUser().getFullName());
    d.setPlace(r.getPlace());
    d.setDateCreated(r.getDateCreated());
    d.setStartTime(r.getStartTime());
    d.setEndTime(r.getEndTime());
    return d;
  }

}

