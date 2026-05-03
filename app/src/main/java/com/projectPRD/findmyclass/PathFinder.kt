package com.projectPRD.findmyclass

object PathFinder {
    // Peta lantai persegi: Sudut A, B, C, D saling terhubung dengan jarak misal 10 meter.
    // Graf ini dibuat dua arah agar bisa bolak-balik
    private val campusGraph = mapOf(
        "A" to mapOf("B" to 10, "D" to 10),
        "B" to mapOf("A" to 10, "C" to 10),
        "C" to mapOf("B" to 10, "D" to 10),
        "D" to mapOf("C" to 10, "A" to 10)
    )

    // Logika Dijkstra disederhanakan
    fun getRoute(startCorner: String, targetCorner: String): String {
        if (startCorner == targetCorner) return "Sudah di tujuan"

        val distances = mutableMapOf<String, Int>().withDefault { Int.MAX_VALUE }
        val previousNodes = mutableMapOf<String, String?>()
        val unvisitedNodes = campusGraph.keys.toMutableSet()

        distances[startCorner] = 0

        while (unvisitedNodes.isNotEmpty()) {
            val currentNode = unvisitedNodes.minByOrNull { distances.getValue(it) } ?: break
            if (currentNode == targetCorner) break
            unvisitedNodes.remove(currentNode)

            campusGraph[currentNode]?.forEach { (neighbor, weight) ->
                val newDist = distances.getValue(currentNode) + weight
                if (newDist < distances.getValue(neighbor)) {
                    distances[neighbor] = newDist
                    previousNodes[neighbor] = currentNode
                }
            }
        }

        val path = mutableListOf<String>()
        var step: String? = targetCorner
        while (step != null) {
            path.add(step)
            step = previousNodes[step]
        }
        return path.reversed().joinToString(" ➡️ ")
    }
}
