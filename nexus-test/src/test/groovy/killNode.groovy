
def pidFile = properties['pidFile'];
def pid = new File(pidFile).text

log.info "Found Pid file :" + pidFile

def processDesc = "kill $pid"

log.info "Executing: {}", processDesc
def process = processDesc.execute()
log.info  "Executed: "+ processDesc +" with exit code: "+ process.waitFor()
