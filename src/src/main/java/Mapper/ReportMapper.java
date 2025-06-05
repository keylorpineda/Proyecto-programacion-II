/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Mapper;
import Models.Report;
import Dto.ReportDTO;

public class ReportMapper {
  public static ReportDTO toDTO(Report r) {
    ReportDTO d = new ReportDTO();
    d.setId(r.getId());
    d.setDescription(r.getDescription());
    d.setDate(r.getDate());
    return d;
  }
}
