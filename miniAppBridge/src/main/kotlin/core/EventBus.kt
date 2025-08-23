package core

class MiniAppEventBus {
    private val handlers = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    fun on(event: String, handler: (Any) -> Unit) {
        handlers.getOrPut(event) { mutableListOf() }.add(handler)
    }

    fun off(event: String, handler: (Any) -> Unit) {
        handlers[event]?.remove(handler)
    }

    fun emit(event: String, data: Any) {
        handlers[event]?.forEach { it(data) }
    }
}
