public class CreateHandler implements SectionHandler {
    // Done
    @Override
    public void handle(Node node, CRMParser parser) {
        String givenName = null;
        String surname = null;
        String postCode = null;
        String telephone = null;

        for (Node child : node.children) {
            // join all lines of content
            String value = null;
            if (!child.content.isEmpty()) {
                value = String.join("", child.content);
            }
            // decode base64 if there is something to decode it is not in a <Comment> tag.
            if (value != null && !child.name.equals("<Comment>")) {
                value = Base64Helper.decode(value);
            }

            switch (child.name) {
                case "<Comment>":
                    if (value != null) {
                        System.out.println(value);
                    }
                    break;
                case "<GivenName>":
                    givenName = value;
                    break;
                case "<PostCode>":
                    postCode = value;
                    break;
                case "<Surname>":
                    surname = value;
                    break;
                case "<Telephone>":
                    telephone = value;
                    break;
            }
        }
        CRMRecord record = new CRMRecord(givenName, surname, telephone, postCode);
        CRMState state = parser.getController().getState();
        String id = state.create(record);
        parser.getController().trace("Created record with CRMID: " + id + "\n" + record);
    }
}
