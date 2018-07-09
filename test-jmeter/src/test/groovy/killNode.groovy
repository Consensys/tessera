
def pidFile = properties['pidFile'];
def pid = new File(pidFile).text

log.info "Found Pid file :" + pidFile

def processDesc = "kill $pid"

log.info "Executing: {}", processDesc
def process = processDesc.execute()
def exitCode = process.waitFor()
log.info  "Executed: {} with exit code: {}", processDesc, exitCode
log.info "{}",process.text
