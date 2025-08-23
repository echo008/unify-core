package state

class MiniAppStateManager {
    private val stateStore = mutableMapOf<String, Any>()
    private val listeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    fun setState(key: String, value: Any) {
        stateStore[key] = value
        notifyListeners(key, value)
    }

    fun getState(key: String): Any? = stateStore[key]

    fun subscribe(key: String, listener: (Any) -> Unit) {
        listeners.getOrPut(key) { mutableListOf() }.add(listener)
    }

    private fun notifyListeners(key: String, value: Any) {
        listeners[key]?.forEach { it(value) }
    }
}
