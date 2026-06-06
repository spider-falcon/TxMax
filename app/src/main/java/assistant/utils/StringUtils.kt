package assistant.utils

object StringUtils {
    // Calculates character difference between two words
    fun getLevenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val costs = IntArray(s2.length + 1) { it }
        for (i in 1..s1.length) {
            var nw = i - 1
            costs[0] = i
            for (j in 1..s2.length) {
                val cj = minOf(
                    1 + minOf(costs[j], costs[j - 1]),
                    if (s1[i - 1] == s2[j - 1]) nw else nw + 1
                )
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs[s2.length]
    }

    // Returns true if a word is very close to the target (e.g., 1 or 2 typos)
    fun isFuzzyMatch(input: String, target: String, maxDistance: Int = 2): Boolean {
        return getLevenshteinDistance(input, target) <= maxDistance
    }
}