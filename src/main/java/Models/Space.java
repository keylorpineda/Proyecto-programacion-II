package Models;

public abstract class Space {
    protected String spaceName;
    protected String spaceId;
    protected int spaceCapacity;
    protected boolean isReserved;

    public Space(String spaceId, String spaceName, int spaceCapacity, boolean isReserved) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.spaceCapacity = spaceCapacity;
        this.isReserved = isReserved;
    }

    public void reserve() {
        isReserved = true;
    }

    public String getStatus(){
        if(isReserved) {
            return "Reservado";
        }
        return "Disponible";
    }
    public void unReserve() {
        isReserved = false;
    }

    public String getInformacion(){
        return "Nombre: " + spaceName +"\n ID: " + spaceId + "\n Capacidad: " + spaceCapacity + "\n Estado: "+ getStatus();
    }
}
