package me.zaksen.nineHearts.menu.api

enum class MenuType(private val totalSize: Int) {
    BASE_9(9),
    BASE_18(18),
    BASE_27(27),
    BASE_36(36),
    BASE_45(45),
    BASE_54(54);

    fun size(): Int {
        return this.totalSize
    }
}