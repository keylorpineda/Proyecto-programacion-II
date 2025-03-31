package Models;

import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SpaceManager {
    private ObservableList<Space> spaces;

    public SpaceManager() {
        this.spaces = FXCollections.observableArrayList();
    }

    public boolean addSpace(Space space) {
        for (Space spaceAux : spaces) {
            if (spaceAux.getSpaceId().equals(space.getSpaceId())) {
                return false;
            }
        }
        spaces.add(space);
        return true;
    }

    public boolean deleteSpace(String spaceId) {
        for (Space space : spaces) {
            if (space.getSpaceId().equals(spaceId)) {
                spaces.remove(space);
                return true;
            }
        }
        return false;
    }

    public Space getSpaceById(String spaceId) {
        for (Space space : spaces) {
            if (space.getSpaceId().equals(spaceId)) {
                return space;
            }
        }
        return null;
    }

    public ObservableList<Space> getAvailableSpaces(LocalDateTime from, LocalDateTime to) {
        ObservableList<Space> availableSpaces = FXCollections.observableArrayList();
        for (Space space : spaces) {
            if (!space.isReserved()) {
                availableSpaces.add(space);
            }
        }
        return availableSpaces;
    }

    public boolean reserveSpace(String spaceId) {
        Space space = getSpaceById(spaceId);
        if (space != null && !space.isReserved()) {
            space.reserve();
            return true;
        }
        return false;
    }

    public ObservableList<Space> getSpaces() {
        return spaces;
    }
}
