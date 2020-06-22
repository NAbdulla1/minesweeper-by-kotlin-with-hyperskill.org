package minesweeper

import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min
import kotlin.random.Random

enum class Visibility {
    VISIBLE, INVISIBLE
}

enum class Flag {
    MARKED, UNMARKED
}

class Cell(var char: Char, val row: Int, val column: Int) {
    private var visibility = Visibility.INVISIBLE
    private var flag = Flag.UNMARKED
        set(value) {
            if (value == Flag.MARKED)
                assert(visibility == Visibility.INVISIBLE)
            field = value
        }

    fun makeVisible() {
        visibility = Visibility.VISIBLE
    }

    fun isVisible(): Boolean {
        return visibility == Visibility.VISIBLE
    }

    fun isMarked(): Boolean {
        return flag == Flag.MARKED
    }

    fun toggleFlag() {
        flag = if (flag == Flag.UNMARKED) Flag.MARKED else Flag.UNMARKED
        char = if (flag == Flag.UNMARKED) '.' else '*'
    }

    override fun toString(): String {
        return char.toString()
    }
}

class MineSweeper(private val rows: Int, private val columns: Int) {
    private val mines: Int
    private val dr = intArrayOf(1, -1, 0, 0, 1, -1, 1, -1)
    private val dc = intArrayOf(0, 0, 1, -1, 1, -1, -1, 1)
    private val board = Array(rows) { row -> Array(columns) { column -> Cell('.', row, column) } }

    private val mineCells = ArrayList<Cell>()

    init {
        print("How many mines do you want on the field? > ")
        mines = readLine()!!.toInt()
        placeMines()
        hideMines()
        play()
    }

    private var marked = 0
    private var visible = 0
    private fun play() {
        val scanner = Scanner(System.`in`)
        var firstMove = true
        printMineField()
        while (true) {
            print("Set/unset mine marks or claim a cell as free: > ")
            val x = scanner.nextInt()
            val y = scanner.nextInt()
            val command = scanner.next()
            val r = y - 1
            val c = x - 1
            val chosenCell = board[r][c]
            if (chosenCell.isVisible()) {
                println("That cell is already explored. Try another")
                continue
            }
            if (command == "mine") {
                if (chosenCell.isMarked()) {
                    marked--
                } else {
                    marked++
                }
                chosenCell.toggleFlag()
            } else if (command == "free") {
                if (chosenCell.isMarked()) {
                    println("That cell is marked. First unmark it or  try another")
                    continue
                }
                if (chosenCell in mineCells) {
                    if (firstMove) {
                        reCreateBoard(chosenCell)
                    } else {
                        showMines()
                        printMineField()
                        println("You stepped on a mine and failed!")
                        break
                    }
                }
                handleMove(chosenCell)
            } else {
                println("Unknown Command try again")
                continue
            }

            printMineField()

            if (marked == mines && checkIfAllMarked() || rows * columns == visible + mines) {
                println("Congratulations! You found all the mines!")
                return
            }
            firstMove = false
        }
    }

    private fun handleMove(cell: Cell) {//bfs
        val queue = LinkedList<Cell>() as Queue<Cell>
        queue.add(cell)
        cell.makeVisible()
        visible++
        while (!queue.isEmpty()) {
            val chosenCell = queue.poll()
            val r = chosenCell.row
            val c = chosenCell.column
            val neighbourMines = countNeighbourMines(r, c)
            if (hasMinesInNeighbour(neighbourMines)) {
                chosenCell.char = neighbourMines
                continue
            }
            chosenCell.char = '/'
            for (i in dr.indices) {
                val nr = dr[i] + r
                val nc = dc[i] + c
                if (isValid(nr, rows) && isValid(
                        nc,
                        columns
                    ) && !board[nr][nc].isVisible() && board[nr][nc] !in mineCells
                ) {
                    if (board[nr][nc].isMarked()) {
                        board[nr][nc].toggleFlag()
                        marked--
                    }
                    queue.add(board[nr][nc])
                    board[nr][nc].makeVisible()
                    visible++
                }
            }
        }
    }

    private fun hasMinesInNeighbour(neighbourMines: Char) = neighbourMines != '0'

    private fun countNeighbourMines(r: Int, c: Int): Char {
        var count = '0'
        for (i in dr.indices) {
            val nr = dr[i] + r
            val nc = dc[i] + c
            if (isValid(nr, rows) && isValid(nc, columns) && board[nr][nc] in mineCells) {
                count++
            }
        }
        return count
    }

    private fun reCreateBoard(chosenCell: Cell) {
        do {
            mineCells.clear()
            placeMines()
            hideMines()
        } while (chosenCell in mineCells)
    }

    private fun checkIfAllMarked(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (board[r][c].isMarked() && board[r][c] !in mineCells)
                    return false
            }
        }
        return true
    }

    private fun hideMines() {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (board[r][c].char == 'X')
                    board[r][c].char = '.'
            }
        }
    }

    private fun showMines() {
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (board[r][c] in mineCells)
                    board[r][c].char = 'X'
            }
        }
    }

    private fun isValid(upInd: Int, up: Int): Boolean = upInd in 0 until up

    private fun placeMines() {
        repeat(mines) {
            var r = Random.nextInt(rows)
            var c = Random.nextInt(columns)
            while (board[r][c].char == 'X') {
                r = Random.nextInt(rows)
                c = Random.nextInt(columns)
            }
            board[r][c].char = 'X'
            mineCells.add(board[r][c])
        }
    }

    private fun printMineField() {
        println(toString())
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
            .append(System.lineSeparator())
            .append(" │123456789│")
            .append(System.lineSeparator())
            .append("—│—————————│")
            .append(System.lineSeparator())

        for (r in 0 until rows) {
            stringBuilder.append(r + 1)
                .append("|")
            for (c in board[r])
                stringBuilder.append(c)
            stringBuilder.append("|")
                .append(System.lineSeparator())
        }
        stringBuilder.append("—│—————————│")
        return stringBuilder.toString()
    }
}

fun main() {
    MineSweeper(9, 9)
}
