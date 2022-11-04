package calculator

import java.util.*
import java.math.BigInteger

object Calculator {

    val variables = mutableMapOf<String, String>()

    fun isAssignment(operation: String) = operation.contains("=")
    fun isCorrectAssignment(operation: String) = Regex("^[a-zA-Z]+ *= *(-?\\d+|[a-zA-Z]+) *$").matches(operation)

    fun priority(op: String): Int {
        return when {
            op == "+" || op == "-" -> 1
            op == "*" || op == "/" -> 2
            op == "^" -> 3
            else -> 0
        }
    }

    fun infixToPostfix(operation: List<String>) {
        val stack: Stack<String> = Stack()
        val result: MutableList<String> = mutableListOf()
        for (i in operation) {
            if (i.toBigIntegerOrNull() != null) result.add(i)
            else if (i == "(") stack.push(i)
            else if (i == ")") {
                while (stack.isNotEmpty() && stack.peek() != "(") result.add(stack.pop())
                // remove (
                stack.pop()
            } else {
                while (stack.isNotEmpty() && priority(i) <= priority(stack.peek())) result.add(stack.pop())
                stack.push(i)
            }
        }
        while (stack.isNotEmpty()) result.add(stack.pop())
        calculatePostfix(result)
    }

    fun calculatePostfix(postfixList: MutableList<String>) {
        val stack: Stack<BigInteger> = Stack()
        for (i in postfixList) {
            if (i.toBigIntegerOrNull() != null) stack.push(i.toBigInteger())
            else if (Regex("^[+\\-*/^]$").matches(i)) {
                val numTwo = stack.pop()
                val numOne = stack.pop()
                when (i) {
                    "+" -> stack.push(numOne + numTwo)
                    "-" -> stack.push(numOne - numTwo)
                    "*" -> stack.push(numOne * numTwo)
                    "/" -> stack.push(numOne / numTwo)
                    "^" -> {
                        var res = BigInteger.ONE
                        repeat(numTwo.toInt()) { res *= numOne }
                        stack.push(res)
                    }
                }
            }
        }
        val final = stack.pop()
        println(final)
    }

    fun assignment(operation: String) {
        val variableName: String = Regex("^[a-zA-Z]+").find(operation)!!.value
        val variableValue: String = Regex("(-?\\d+|[a-zA-Z]+) *$").find(operation)!!.value.trim()
        if (variableValue.toBigIntegerOrNull() != null)
            variables[variableName] = variableValue
        else if (variables.keys.contains(variableValue)) {
            variables[variableName] = variables[variableValue]!!
        }
        else
            println("Unknown variable")
    }

    fun command(command: String) {
        when (command) {
            "/help" -> println("The program calculates the sum of numbers")
            else -> println("Unknown command")
        }
    }

    fun processInfix(operation: String) {
        if (operation.count { it == '(' } != operation.count { it == ')' }
                || Regex("^.+(\\*{2,}|/{2,}).+$").matches(operation)
                ) {
            println("Invalid expression")
            return
        }
        val operationList = operation.split(" ")
        val finalOperation: MutableList<String> = mutableListOf()
        // convert adds subs
        for (tempVal in operationList) {
            if (Regex("^[a-zA-Z]+$").matches(tempVal)) {
                if (variables.contains(tempVal)) finalOperation.add(variables[tempVal]!!)
                else {
                    print("Unknown variable")
                    break
                }
            }
            else if (Regex("^([+\\-])+$").matches(tempVal)) {
                finalOperation.add( if (tempVal.contains("-")
                    && tempVal.count { it == '-' } % 2 != 0) "-"
                else "+" )
            } else if (tempVal.contains(Regex("\\(+(\\d+|[a-zA-Z])+"))) {
                val numValue = Regex("[a-zA-Z0-9]+").find(tempVal)!!.value
                repeat(tempVal.count { it == '(' }) { finalOperation.add("(") }
                if (numValue.toBigIntegerOrNull() == null) {
                    if (variables.contains(numValue)) finalOperation.add(variables[numValue]!!)
                    else {
                        print("Unknown variable")
                        break
                    }
                } else finalOperation.add(numValue)
            } else if (tempVal.contains(Regex("(\\d+|[a-zA-Z])+\\)"))) {
                val numValue = Regex("[a-zA-Z0-9]+").find(tempVal)!!.value
                if (numValue.toBigIntegerOrNull() == null) {
                    if (variables.contains(numValue)) finalOperation.add(variables[numValue]!!)
                    else {
                        print("Unknown variable")
                        break
                    }
                } else finalOperation.add(numValue)
                repeat(tempVal.count { it == ')' }) { finalOperation.add(")") }
            } else finalOperation.add(tempVal)
        }
        infixToPostfix(finalOperation.toList())
    }
}

fun main() {
    while (true) {
        val numbers = readln().trim()
        if (numbers.isEmpty()) continue
        if (numbers == "/exit") break
        if (numbers.first() == '/') Calculator.command(numbers)
        else if (Calculator.isAssignment(numbers)){
            if (Calculator.isCorrectAssignment(numbers)) Calculator.assignment(numbers)
            else println("Invalid assignment")
        }
        else if (numbers.split(" ").count() == 1) {
            if (Calculator.variables.keys.contains(numbers)) println(Calculator.variables[numbers])
            else println("Unknown variable")
        }
        else Calculator.processInfix(numbers)
    }
    println("Bye!")
}
