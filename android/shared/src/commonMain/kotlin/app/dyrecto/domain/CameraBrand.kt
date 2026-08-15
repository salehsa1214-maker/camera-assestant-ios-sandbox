package app.dyrecto.domain

enum class CameraBrand(val displayName: String, val filters: List<String>) {
    SONY("Sony", listOf("FX3", "ILME-FX3", "DSC", "ILCE", "Sony")),
    UNKNOWN("", emptyList());

    companion object {
        fun detect(name: String): CameraBrand =
            entries.firstOrNull { b -> b.filters.any { name.contains(it, ignoreCase = true) } }
                ?: UNKNOWN
    }
}
