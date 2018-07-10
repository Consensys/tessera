
def jarfile = properties['jarfile']

def configFile = properties['configFile']

def pidFile = properties['pidFile']

def logbackConfigFile = properties['logbackConfigFile']

def junixsocketLibPath = properties['junixsocketLibPath']

log.info "$jarfile"
// -Dspring.profiles.active=disable-unixsocket
//org.newsclub.net.unix.library.path
//-Dorg.newsclub.net.unix.library.path=$junixsocketLibPath
def processDesc = "java -Dspring.profiles.active=disable-unixsocket -Dlogback.configurationFile=$logbackConfigFile -jar $jarfile -configfile $configFile -pidfile $pidFile"

log.info "$processDesc"

def countdownLatch = new java.util.concurrent.CountDownLatch(1)

def process = "$processDesc".execute();

def t = new Thread({
    def exitCode = process.waitFor()
    log.info "Exit code: {}",exitCode
    if(exitCode != 0) {
        log.error process.err.text
            
    }
    countdownLatch.countDown()
})

t.start()

countdownLatch.await(10,java.util.concurrent.TimeUnit.SECONDS)



