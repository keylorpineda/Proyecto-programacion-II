package Models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class Space {
    protected StringProperty spaceName;
    protected StringProperty spaceId;
    protected IntegerProperty spaceCapacity;
    protected BooleanProperty reserved;

    public Space(String spaceId, String spaceName, int spaceCapacity, boolean isReserved) {
        this.spaceId = new SimpleStringProperty(spaceId);
        this.spaceName = new SimpleStringProperty(spaceName);
        this.spaceCapacity = new SimpleIntegerProperty(spaceCapacity);
        this.reserved = new SimpleBooleanProperty(isReserved);
    }

    public void reserve() {
        reserved.set(true);
    }

    public void unReserve() {
        reserved.set(false);
    }

    public String getStatus() {
        return reserved.get() ? "Reservado" : "Disponible";
    }

    public String getSpaceId() {
        return spaceId.get();
    }

    public void setSpaceId(String spaceId) {
        this.spaceId.set(spaceId);
    }

    public String getSpaceName() {
        return spaceName.get();
    }

    public void setSpaceName(String spaceName) {
        this.spaceName.set(spaceName);
    }

    public int getSpaceCapacity() {
        return spaceCapacity.get();
    }

    public void setSpaceCapacity(int spaceCapacity) {
        this.spaceCapacity.set(spaceCapacity);
    }

    public boolean isReserved() {
        return reserved.get();
    }

    public StringProperty spaceIdProperty() {
        return spaceId;
    }

    public StringProperty spaceNameProperty() {
        return spaceName;
    }

    public IntegerProperty spaceCapacityProperty() {
        return spaceCapacity;
    }

    public BooleanProperty reservedProperty() {
        return reserved;
    }

    public String getInformation() {
        return "Nombre: " + getSpaceName() + "\nID: " + getSpaceId() +
                "\nCapacidad: " + getSpaceCapacity() + "\nEstado: " + getStatus();
    }
}
