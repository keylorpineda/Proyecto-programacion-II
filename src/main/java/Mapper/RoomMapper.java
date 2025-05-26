/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Mapper;
import Models.Room;
import Dto.RoomTableDTO;
public class RoomMapper {
  public static RoomTableDTO toDTO(Room r) {
    RoomTableDTO d = new RoomTableDTO();
    d.setId(r.getId());
    d.setRoomName(r.getRoomName());
    d.setFloor(r.getFloor());
    d.setRows(r.getRows());
    d.setCols(r.getCols());
    return d;
  }
}


