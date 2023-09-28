package net.arvandor.numinoustreasury.measurement;

import static java.lang.Math.round;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.Map;

@SerializableAs("Weight")
public final class Weight implements Comparable<Weight>, ConfigurationSerializable {

    private final double value;
    private final WeightUnit unit;

    public Weight(double value, WeightUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public WeightUnit getUnit() {
        return unit;
    }

    public Weight to(WeightUnit unit) {
        return new Weight((getValue() / getUnit().getScaleFactor()) * unit.getScaleFactor(), unit);
    }

    public Weight multiply(int amount) {
        return new Weight(getValue() * amount, getUnit());
    }

    @Override
    public int compareTo(Weight weight) {
        return (int) round((getValue() / getUnit().getScaleFactor()) - (weight.getValue() / weight.getUnit().getScaleFactor()));
    }

    @Override
    public String toString() {
        if (getValue() == (long) getValue()) {
            return String.format("%.0f%s", getValue(), getUnit().getName());
        } else {
            return String.format("%s%s", getValue(), getUnit().getName());
        }
    }

    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "value", getValue(),
                "unit", getUnit().getName()
        );
    }

    public static Weight deserialize(Map<String, Object> serialized) {
        return new Weight(
                ((Number) serialized.get("value")).doubleValue(),
                WeightUnit.getByName((String) serialized.get("unit"))
        );
    }
}
