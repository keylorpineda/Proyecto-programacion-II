package Services;

import Dto.RoomLayoutDTO;
import Dto.SpaceDTO;
import mapper.SpaceMapper;
import java.util.List;
import java.util.stream.Collectors;

public class RoomLayoutService {

    private final RoomService roomService = new RoomService();

    public RoomLayoutDTO getLayout(Long roomId) {
        var room = roomService.findById(roomId);
        List<SpaceDTO> spaces = room.getSpaces()
                                    .stream()
                                    .map(SpaceMapper::toDTO)
                                    .collect(Collectors.toList());
        return new RoomLayoutDTO(spaces, room.getRows(), room.getCols());
    }
}
