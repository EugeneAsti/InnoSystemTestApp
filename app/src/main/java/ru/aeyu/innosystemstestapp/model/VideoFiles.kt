package ru.aeyu.innosystemstestapp.model

class VideoFiles : ArrayList<VideoFileItem>() {
    private var position: Int = -1

    /**
     * Циклический обход массиво. Начинается с нулевого индекса
     * Каждый вызов данной процедуры увеличивает индекс на 1
     * @return next VideoFileItem
     */
    fun nextFile(): VideoFileItem {
        position++
        println("---!!!--- nextFile: $position")
        if(position >= (this.size))
            position = 0
        return this[position]
    }

    fun currentFile(): VideoFileItem {
        println("---!!!--- currentFile: $position")
        if(position < 0)
            position ++
        return this[position]
    }
}