
package Models;

public enum SpaceType {
    AREA_COMUN,
    ESCRITORIO,
    SALA_REUNIONES,
    PASILLO;

    public String getTypeName() {
    String original = this.name().toLowerCase().replace("_", " ");
    return Character.toUpperCase(original.charAt(0)) + original.substring(1);
}
}

