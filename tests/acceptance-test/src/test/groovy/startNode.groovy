import com.quorum.tessera.test.util.ElUtil as EL

def jarfile = properties['jarfile']

def configFileTemplate = properties['configFile']

def configFile = EL.createAndPopulatePaths(new File(configFileTemplate).toPath())

def pidFile = properties['pidFile']

def logbackConfigFile = properties['logbackConfigFile']

def nodeNumber = properties['nodeNumber']

log.info "$jarfile"
// -Dspring.profiles.active=disable-unixsocket
def processDesc = "java -Dspring.profiles.active=disable-unixsocket -Dnode.number=$nodeNumber -Dlogback.configurationFile=$logbackConfigFile -jar $jarfile -configfile $configFile -pidfile $pidFile "

log.info "$processDesc"

def countdownLatch = new java.util.concurrent.CountDownLatch(1)

def process = "$processDesc".execute()

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



