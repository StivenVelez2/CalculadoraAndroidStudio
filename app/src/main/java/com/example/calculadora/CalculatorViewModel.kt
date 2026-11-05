package com.example.calculadora

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class CalculatorViewModel : ViewModel() {

    private val model = CalculatorModel()

    val result = mutableStateOf("0")
    val contentResult = mutableStateListOf<HistoryEntry>()
    val expression = mutableStateOf("")
    val activeOperation = mutableStateOf("")
    val error = mutableStateOf<CalculatorError?>(null)

    private var leftOperand: Double? = null
    private var pendingOperation = ""
    private var isAwaitingOperand = false
    private var replacesInputOnNextEntry = false
    private var repeatOperation = ""
    private var repeatOperand: Double? = null

    fun manageClickButtons(valueButton: String) {
        if (valueButton.isEmpty()) return

        if (error.value != null) {
            when {
                valueButton in DIGITS || valueButton == "." -> clear()
                valueButton == "AC" -> {
                    clear()
                    return
                }
                else -> return
            }
        }

        when (valueButton) {
            in DIGITS -> enterDigit(valueButton)
            "." -> enterDecimal()
            "≪" -> deleteLastCharacter()
            "AC" -> clear()
            "+/−" -> toggleSign()
            "%" -> applyPercent()
            "+", "−", "×", "÷" -> selectOperation(valueButton)
            "=" -> calculateResult()
        }
    }

    fun calculateResult(): Double {
        val left = leftOperand
        val repeatedRight = repeatOperand
        val computed = when {
            pendingOperation.isNotEmpty() && left != null ->
                complete(left, pendingOperation, parseInput())
            repeatOperation.isNotEmpty() && repeatedRight != null ->
                complete(parseInput(), repeatOperation, repeatedRight)
            else -> null
        }
        return computed ?: 0.0
    }

    fun add(number1: String, number2: String): String =
        operate(number1, number2) { num1, num2 -> model.add(num1, num2) }

    fun subtraction(number1: String, number2: String): String =
        operate(number1, number2) { num1, num2 -> model.subtraction(num1, num2) }

    fun multiply(number1: String, number2: String): String =
        operate(number1, number2) { num1, num2 -> model.multiply(num1, num2) }

    fun divide(number1: String, number2: String): String =
        operate(number1, number2, rejectZeroDivisor = true) { num1, num2 ->
            model.divide(num1, num2)
        }

    private fun operate(
        number1: String,
        number2: String,
        rejectZeroDivisor: Boolean = false,
        operation: (Double, Double) -> Double
    ): String {
        val num1 = number1.toDoubleOrNull()
        val num2 = number2.toDoubleOrNull()
        if (num1 == null || num2 == null || (rejectZeroDivisor && num2 == 0.0)) {
            return ERROR_RESULT
        }
        return operation(num1, num2).toString()
    }

    private fun enterDigit(digit: String) {
        val current = result.value
        when {
            replacesInputOnNextEntry || isAwaitingOperand -> startInput(digit)
            current == "0" -> updateInput(digit)
            current == "-0" -> updateInput("-$digit")
            countDigits(current) < MAX_INPUT_DIGITS -> updateInput(current + digit)
        }
    }

    private fun enterDecimal() {
        val current = result.value
        when {
            replacesInputOnNextEntry || isAwaitingOperand -> startInput("0.")
            !current.contains('.') && countDigits(current) < MAX_INPUT_DIGITS ->
                updateInput("$current.")
        }
    }

    private fun deleteLastCharacter() {
        if (replacesInputOnNextEntry || isAwaitingOperand) return
        val remaining = result.value.dropLast(1)
        updateInput(if (remaining.isEmpty() || remaining == "-") "0" else remaining)
    }

    private fun toggleSign() {
        if (isAwaitingOperand) return startInput("-0")
        val current = result.value
        updateInput(if (current.startsWith("-")) current.removePrefix("-") else "-$current")
    }

    private fun applyPercent() {
        val value = result.value.toDoubleOrNull() ?: return showError(CalculatorError.InvalidInput)
        val base = if (pendingOperation == "+" || pendingOperation == "−") leftOperand else null
        val percentage = if (base != null) {
            model.divide(model.multiply(base, value), 100.0)
        } else {
            model.divide(value, 100.0)
        }
        val hasPendingOperation = pendingOperation.isNotEmpty()

        isAwaitingOperand = false
        replacesInputOnNextEntry = true
        result.value = formatDecimal(percentage)
        if (!hasPendingOperation) expression.value = ""
        activeOperation.value = ""
    }

    private fun selectOperation(operation: String) {
        val left = leftOperand
        val operand = when {
            pendingOperation.isNotEmpty() && left != null && isAwaitingOperand -> left
            pendingOperation.isNotEmpty() && left != null -> resolvePending(left, pendingOperation) ?: return
            else -> parseInput() ?: return showError(CalculatorError.InvalidInput)
        }

        leftOperand = operand
        pendingOperation = operation
        isAwaitingOperand = true
        replacesInputOnNextEntry = false
        repeatOperation = ""
        repeatOperand = null
        result.value = formatDecimal(operand)
        expression.value = formatNumber(formatDecimal(operand)) + " " + operation
        activeOperation.value = operation
    }

    private fun resolvePending(left: Double, operation: String): Double? {
        val right = parseInput() ?: run {
            showError(CalculatorError.InvalidInput)
            return null
        }
        if (operation == "÷" && right == 0.0) {
            showError(CalculatorError.DivisionByZero)
            return null
        }
        val computed = calculate(left, operation, right) ?: run {
            showError(CalculatorError.InvalidInput)
            return null
        }
        recordHistory(left, operation, right, computed)
        return computed
    }

    private fun complete(left: Double?, operation: String, right: Double?): Double? {
        if (left == null || right == null) {
            showError(CalculatorError.InvalidInput)
            return null
        }
        if (operation == "÷" && right == 0.0) {
            showError(CalculatorError.DivisionByZero)
            return null
        }
        val computed = calculate(left, operation, right) ?: run {
            showError(CalculatorError.InvalidInput)
            return null
        }

        recordHistory(left, operation, right, computed)
        leftOperand = null
        pendingOperation = ""
        isAwaitingOperand = false
        replacesInputOnNextEntry = true
        repeatOperation = operation
        repeatOperand = right
        result.value = formatDecimal(computed)
        expression.value = formatNumber(formatDecimal(left)) +
            " " + operation + " " +
            formatNumber(formatDecimal(right))
        activeOperation.value = ""
        return computed
    }

    private fun calculate(left: Double, operation: String, right: Double): Double? {
        val text = when (operation) {
            "+" -> add(left.toString(), right.toString())
            "−" -> subtraction(left.toString(), right.toString())
            "×" -> multiply(left.toString(), right.toString())
            "÷" -> divide(left.toString(), right.toString())
            else -> return null
        }
        return text.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun recordHistory(left: Double, operation: String, right: Double, result: Double) {
        contentResult.add(
            HistoryEntry(
                leftOperand = formatNumber(formatDecimal(left)),
                operation = operation,
                rightOperand = formatNumber(formatDecimal(right)),
                result = formatNumber(formatDecimal(result))
            )
        )
    }

    private fun startInput(value: String) {
        val continuesOperation = pendingOperation.isNotEmpty()
        if (!continuesOperation) {
            repeatOperation = ""
            repeatOperand = null
        }
        isAwaitingOperand = false
        replacesInputOnNextEntry = false
        result.value = value
        if (!continuesOperation) expression.value = ""
        activeOperation.value = ""
    }

    private fun updateInput(value: String) {
        result.value = value
    }

    private fun clear() {
        clearOperationState()
        result.value = "0"
        expression.value = ""
        activeOperation.value = ""
        error.value = null
    }

    private fun showError(error: CalculatorError) {
        clearOperationState()
        result.value = "0"
        expression.value = ""
        activeOperation.value = ""
        this.error.value = error
    }

    private fun clearOperationState() {
        leftOperand = null
        pendingOperation = ""
        isAwaitingOperand = false
        replacesInputOnNextEntry = false
        repeatOperation = ""
        repeatOperand = null
    }

    private fun parseInput(): Double? = result.value.toDoubleOrNull()

    private fun countDigits(input: String): Int = input.count { it.isDigit() }

    private fun formatDecimal(value: Double): String {
        if (!value.isFinite()) return "0"
        val rounded = BigDecimal.valueOf(value).round(MATH_CONTEXT)
        val plain = rounded.stripTrailingZeros().toPlainString()
        return if (plain == "-0") "0" else plain
    }

    private companion object {
        val MATH_CONTEXT = MathContext(12, RoundingMode.HALF_UP)
    }
}

data class HistoryEntry(
    val leftOperand: String,
    val operation: String,
    val rightOperand: String,
    val result: String
)

internal fun formatNumber(rawNumber: String): String {
    val sign = if (rawNumber.startsWith("-")) "-" else ""
    val unsigned = rawNumber.removePrefix("-")
    val integerPart = unsigned.substringBefore('.')
    val fractionPart = if (unsigned.contains('.')) "." + unsigned.substringAfter('.') else ""
    val groupedInteger = integerPart
        .reversed()
        .chunked(GROUP_SIZE)
        .joinToString(GROUP_SEPARATOR)
        .reversed()
    return sign + groupedInteger + fractionPart
}

enum class CalculatorError {
    DivisionByZero,
    InvalidInput
}

private const val GROUP_SIZE = 3
private const val GROUP_SEPARATOR = ","
private const val MAX_INPUT_DIGITS = 9
private const val ERROR_RESULT = "Error"

private val DIGITS = setOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
