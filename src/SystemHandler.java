public class SystemHandler implements SectionHandler {
    @Override
    public void handle(Node node, CRMParser parser) {
        CRMController controller = parser.getController();

        for (Node child : node.children) {
            String value = null;
            if (!child.content.isEmpty()) {
                value = String.join("", child.content);
            }
            switch (child.name) {
                case "<Comment>":
                    if (value != null) {
                        System.out.println(value);
                    }
                    break;

                case "<DBFileName>":
                    // Initializes CRM state from a specified file and sets the DBFIleName in
                    // the controller.
                    // If content is empty, it will initialize the state (which will not be saved to
                    // a file until a Save command.)
                    if (!child.content.isEmpty()) {
                        controller.initState(value);
                    }
                    break;
                case "<Delete>":
                    if (value != null) {
                        if (CRMStorage.delete(value)) {
                            controller.trace("Deleted file: " + value);
                        } else {
                            controller.trace("Failed to delete file: " + value);
                        }
                    }
                    break;
                case "<Exit>":
                    if (!child.content.isEmpty()) {
                        System.out.println(value);
                        System.exit(0);
                    } else {
                        System.exit(0);
                    }
                    break;
                case "<Output>":
                    // TODO: Allow output to return more than just all records.
                    CRMState state = controller.getState();
                    if (state == null) {
                        controller.trace("CRM state is not initialized. No records to output.");
                    } else {

                        controller.trace("Outputting all CRM records...");
                        for (String id : state.getAllIDs()) {
                            CRMRecord record = state.retrieve(id);
                            controller.trace("CRMID: " + id + "\n" + record);
                        }
                    }
                    break;
                case "<Save>":
                    controller.trace("Saving CRM State...");
                    // if content is provided use it as the filename, otherwise use current DB
                    // filename.
                    String fileName = child.content.isEmpty() ? controller.getDBFileName() : value;
                    String error = CRMStorage.save(fileName, controller.getState());
                    if (error == null) {
                        controller.trace("CRM state saved to file: " + fileName + "\n");
                    } else {
                        controller.trace("Failed to save CRM state to file: " + fileName + "; Error: " + error + "\n");
                    }
                    break;
                case "<Sort>":
                    // TODO: There may be more than one Sort directive. All sorts are cumulative.
                    System.out.println("<Sort> command is not yet implemented.");
                    break;
                case "<Trace>":
                    // defaults to off.
                    if (controller.toggleTrace()) {
                        controller.trace("Trace enabled.\n");
                    }
                    break;
            }
        }
    }
}
