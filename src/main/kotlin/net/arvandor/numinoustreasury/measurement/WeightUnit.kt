package net.arvandor.numinoustreasury.measurement

class WeightUnit private constructor(val name: String, scaleFactor: Int) {
    val scaleFactor: Double = scaleFactor.toDouble()

    companion object {
        val KG: WeightUnit = WeightUnit("kg", 1000000)
        val G: WeightUnit = WeightUnit("g", 1000000000)
        val ST: WeightUnit = WeightUnit("st", 157473)
        val LB: WeightUnit = WeightUnit("lb", 2204622)
        val OZ: WeightUnit = WeightUnit("oz", 35273952)

        fun getByName(name: String): WeightUnit? {
            return when (name.lowercase()) {
                "kg" -> KG
                "g" -> G
                "st" -> ST
                "lb" -> LB
                "oz" -> OZ
                else -> null
            }
        }
    }
}
