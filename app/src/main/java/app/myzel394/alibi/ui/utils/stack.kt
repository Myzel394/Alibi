package app.myzel394.alibi.ui.utils

// A stack that allows you to randomly pop items from it
class RandomStack<T> {
    private val stack = mutableListOf<T>()

    fun push(item: T) {
        stack.add(item)
    }

    fun pop(): T {
        val index = stack.size - 1
        val item = stack[index]

        stack.removeAt(index)

        return item
    }

    fun popRandom(): T {
        val index = (0..<stack.size).random()
        val item = stack[index]

        stack.removeAt(index)

        return item
    }

    fun isEmpty() = stack.isEmpty()

    companion object {
        fun <T> of(items: Iterable<T>): RandomStack<T> {
            val stack = RandomStack<T>()

            items.forEach(stack::push)

            return stack
        }
    }
}