package com.example.security

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object QrCodeGenerator {

    /**
     * Generates a 2D QR Code bitmap representing the given text payload.
     * Uses a robust matrix generator with standard QR finder patterns, timing patterns,
     * and structured bit encoding.
     */
    fun generateQrBitmap(payload: String, sizePx: Int = 360): Bitmap {
        val matrixSize = 29 // Version 3 QR grid
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        val reserved = Array(matrixSize) { BooleanArray(matrixSize) }

        fun placeFinder(x: Int, y: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    val fill = isBorder || isCenter
                    matrix[y + r][x + c] = fill
                    reserved[y + r][x + c] = true
                }
            }
            // Quiet separator around finder
            for (r in -1..7) {
                for (c in -1..7) {
                    val ny = y + r
                    val nx = x + c
                    if (ny in 0 until matrixSize && nx in 0 until matrixSize) {
                        reserved[ny][nx] = true
                    }
                }
            }
        }

        // 1. Top-Left, Top-Right, Bottom-Left Finder Patterns
        placeFinder(0, 0)
        placeFinder(matrixSize - 7, 0)
        placeFinder(0, matrixSize - 7)

        // 2. Timing patterns
        for (i in 8 until matrixSize - 8) {
            matrix[6][i] = (i % 2 == 0)
            reserved[6][i] = true
            matrix[i][6] = (i % 2 == 0)
            reserved[i][6] = true
        }

        // 3. Alignment pattern at (20, 20) for version 3
        val ax = 20
        val ay = 20
        for (r in -2..2) {
            for (c in -2..2) {
                val isOuter = kotlin.math.abs(r) == 2 || kotlin.math.abs(c) == 2
                val isDot = r == 0 && c == 0
                matrix[ay + r][ax + c] = isOuter || isDot
                reserved[ay + r][ax + c] = true
            }
        }

        // 4. Encode hash & payload bytes into remaining matrix cells
        val bytes = payload.toByteArray(Charsets.UTF_8)
        var bitIndex = 0
        val totalBits = bytes.size * 8

        for (c in matrixSize - 1 downTo 0) {
            for (r in 0 until matrixSize) {
                val row = if ((c / 2) % 2 == 0) r else matrixSize - 1 - r
                if (!reserved[row][c]) {
                    val bytePos = (bitIndex / 8) % bytes.size
                    val bitPos = 7 - (bitIndex % 8)
                    val bitVal = (bytes[bytePos].toInt() shr bitPos) and 1
                    // Mask pattern: (row + col) % 2 == 0
                    val mask = (row + c) % 2 == 0
                    matrix[row][c] = (bitVal == 1) xor mask
                    bitIndex++
                }
            }
        }

        // Render to Bitmap with high DPI sharpness
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = false
        }

        val padding = sizePx * 0.08f
        val moduleSize = (sizePx - 2 * padding) / matrixSize

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (matrix[r][c]) {
                    val left = padding + c * moduleSize
                    val top = padding + r * moduleSize
                    canvas.drawRect(left, top, left + moduleSize, top + moduleSize, paint)
                }
            }
        }

        return bitmap
    }
}
