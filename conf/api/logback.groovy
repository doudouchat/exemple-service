import java.nio.charset.Charset
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.jul.LevelChangePropagator

scan("30 seconds")

def USER_HOME = "/usr/local/tomcat"
def LOGS_FOLDER = "${USER_HOME}/logs"
def LOG_ARCHIVE = "${LOGS_FOLDER}/archive"
def LOGS_FILENAME = "exemple_service"

def lcp = new LevelChangePropagator()
lcp.context = context
lcp.resetJUL = true
context.addListener(lcp)

appender("console", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d %-5p: %C - %m%n"
        charset =  Charset.forName("UTF-8")
    }
}

//appender("file", FileAppender) {
//    file = "${LOGS_FOLDER}/${LOGS_FILENAME}.log"
//    encoder(PatternLayoutEncoder) {
//        pattern = "%d %-5p: %C - %m%n"
//    }
//}

appender("archive", RollingFileAppender) {
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "${LOG_ARCHIVE}/${LOGS_FILENAME}.%d{yyyy-MM-dd}.log"
        maxHistory = 2
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%d %-5p: %C - %m%n"
        charset =  Charset.forName("UTF-8")
    }
}

logger("org.apache.cassandra", WARN)

logger("com.datastax.driver.core.QueryLogger.NORMAL", DEBUG)
logger("com.hazelcast", INFO)
logger("org.apache.zookeeper", INFO)
logger("org.springframework.boot", INFO)

logger("com.exemple.service", DEBUG)
logger("org.glassfish.jersey.logging", DEBUG)

root(WARN, ["console", "archive"])