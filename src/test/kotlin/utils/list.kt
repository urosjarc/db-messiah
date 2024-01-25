package utils

fun printLists(first: MutableList<Any>, second: MutableList<Any>){
    first.forEachIndexed { index, objProperty ->
        println(objProperty)
        println(second[index])
        println()
    }
}
