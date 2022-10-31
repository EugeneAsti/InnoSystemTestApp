package ru.aeyu.innosystemstestapp.model

class VideoFiles : ArrayList<VideoFileItem>() {
    private var position: Int = -1

    /**
     * Функция возвращает следующий элемент массива. Обход начинается с -1
     * Каждый вызов данной процедуры сначала увеличивает индекс на 1 и после
     * возвращает элемент массива с полученным индексом
     * При достижении конца масива, индекс сбрасывается на 0
     * @return next VideoFileItem
     */
    fun nextFile(): VideoFileItem {
        position++
        println("---!!!--- nextFile: $position")
        if(position >= (this.size))
            position = 0
        return this[position]
    }

    /**
     * Данная функция возвращает текущий элемент массива.
     * Если не было вызова функции nextFile(), то будет возвращен элемент с индексом 0
     * @return next VideoFileItem
     */
    fun currentFile(): VideoFileItem {
        println("---!!!--- currentFile: $position")
        if(position < 0)
            position ++
        return this[position]
    }
}