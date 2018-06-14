

def pid = new File(properties['pidFile']).text

def processDesc = "kill $pid"

log.info processDesc
def process = processDesc.execute()

log.info  "Exit code "+process.waitFor()
