package net.kingdommc.darkages.numinoustreasury.measurement;

public final class WeightUnit {

    public static final WeightUnit KG = new WeightUnit("kg", 1000000);
    public static final WeightUnit G = new WeightUnit("g", 1000000000);
    public static final WeightUnit ST = new WeightUnit("st", 157473);
    public static final WeightUnit LB = new WeightUnit("lb", 2204622);
    public static final WeightUnit OZ = new WeightUnit("oz", 35273952);

    private final String name;
    private final double scaleFactor;

    private WeightUnit(String name, int scaleFactor) {
        this.name = name;
        this.scaleFactor = scaleFactor;
    }

    public String getName() {
        return name;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public static WeightUnit getByName(String name) {
        return switch (name.toLowerCase()) {
            case "kg" -> KG;
            case "g" -> G;
            case "st" -> ST;
            case "lb" -> LB;
            case "oz" -> OZ;
            default -> null;
        };
    }

}
