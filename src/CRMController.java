public class CRMController {
    private String DBFileName;
    private CRMState state;
    private CRMParser parser;
    private boolean traceEnabled = false;

    public CRMController() {
        this.parser = new CRMParser(this);
    }

    // state related methods
    public CRMState getState() {
        return state;
    }

    public void setState(CRMState newState) {
        state = newState;
    }

    public void initState(String fileName) {
        if (state == null) {
            DBFileName = fileName;

            try {
                state = CRMStorage.load(fileName);
                trace("Loaded CRM state from file: " + fileName);
            } catch (Exception ex) {
                state = new CRMState();
                trace("Initialized CRM state. Failed to load from file: " + fileName + "; Error: " + ex.getMessage());
            }
        } else {
            trace("CRM state already initialized. Skipping initialization.");
        }
    }

    public String getDBFileName() {
        return DBFileName;
    }

    public void setDBFileName(String fileName) {
        DBFileName = fileName;
    }

    public boolean toggleTrace() {
        traceEnabled = !traceEnabled;
        return traceEnabled;
    }

    public void trace(String message) {
        if (traceEnabled) {
            System.out.println(message);
        }
    }

    // execution related methods
    public void run(String controlFileName) {
        System.out.println("Parsing control file: " + controlFileName);
        parser.parse(controlFileName);
    }

}
