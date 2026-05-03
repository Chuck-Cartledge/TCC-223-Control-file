public class UpdateHandler implements SectionHandler {
    @Override
    public void handle(Node node, CRMParser parser) {
        String id = null;
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
            // decode base64 if there is something to decode it is not in a <CRMID> or
            // <Comment> tag.
            if (value != null && !child.name.equals("<CRMID>") && !child.name.equals("<Comment>")) {
                value = Base64Helper.decode(value);
            }

            switch (child.name) {
                case "<CRMID>":
                    id = value;
                    break;
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
        // make sure CRMID is provided.
        if (id == null) {
            System.out.println("Error: Missing CRMID; All other fields are optional.");
            return;
        }
        CRMRecord updatedRecord = new CRMRecord(givenName, surname, telephone, postCode);
        CRMState state = parser.getController().getState();
        if (!state.update(id, updatedRecord)) {
            System.out.println("Error: CRMID " + id + " does not exist. No records were updated.");
        } else {
            System.out.println("Updated record with CRMID: " + id);
        }
    }
}
