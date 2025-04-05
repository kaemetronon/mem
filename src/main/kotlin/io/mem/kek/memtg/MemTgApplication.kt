package io.mem.kek.memtg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MemTgApplication

fun main(args: Array<String>) {
    runApplication<MemTgApplication>(*args)
}
