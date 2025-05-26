package mapper;

import Models.Space;
import Dto.SpaceDTO;

public class SpaceMapper {
  public static SpaceDTO toDTO(Space s) {
    SpaceDTO d = new SpaceDTO();
    d.setId(s.getId());
    d.setName(s.getName());
    d.setType(s.getType());
    d.setStartRow(s.getStartRow());
    d.setStartCol(s.getStartCol());
    d.setWidth(s.getWidth());
    d.setHeight(s.getHeight());
    d.setAvailable(s.getAvailable());
    return d;
  }
}

