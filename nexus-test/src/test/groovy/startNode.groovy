
def jarfile = properties['jarfile']

def configFile = properties['configFile']

log.info "$jarfile"

def processDesc = "java -jar $jarfile -configfile $configFile & echo \$! > /path/to/pid.file"

log.info "$processDesc"

def countdownLatch = new java.util.concurrent.CountDownLatch(1)

def process = "$processDesc".execute();

def t = new Thread({
    process.waitFor()
    log.info "HERE"+ process.pid
    countdownLatch.countDown()
})

t.start()

countdownLatch.await(10,java.util.concurrent.TimeUnit.SECONDS)



