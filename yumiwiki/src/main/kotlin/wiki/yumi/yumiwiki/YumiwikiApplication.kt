package wiki.yumi.yumiwiki

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class YumiwikiApplication

fun main(args: Array<String>) {
	runApplication<YumiwikiApplication>(*args)
}
