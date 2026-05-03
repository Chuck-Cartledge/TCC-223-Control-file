import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteHandler implements SectionHandler {

    @Override
    public void handle(Node node, CRMParser parser) {

        String id = null;
        String givenName = null;
        String surname = null;
        String postCode = null;
        String telephone = null;

        // get values
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
                case "<Comment>":
                    if (value != null) {
                        System.out.println(value);
                    }
                    break;

                case "<CRMID>":
                    id = value;
                    break;

                case "<GivenName>":
                    givenName = value;
                    break;

                case "<Surname>":
                    surname = value;
                    break;

                case "<PostCode>":
                    postCode = value;
                    break;

                case "<Telephone>":
                    telephone = value;
                    break;
            }
        }
        // get records from CRMState
        CRMState state = parser.getController().getState();
        Map<String, CRMRecord> records = state.getRecords();

        List<String> toDelete = new ArrayList<>();

        // search for match
        for (String crmID : records.keySet()) {

            CRMRecord r = records.get(crmID);

            boolean match = true;

            if (id != null && !id.equals(crmID)) {
                match = false;
            }

            if (givenName != null && !givenName.equals(r.getGivenName())) {
                match = false;
            }

            if (surname != null && !surname.equals(r.getSurname())) {
                match = false;
            }

            if (postCode != null && !postCode.equals(r.getPostcode())) {
                match = false;
            }

            if (telephone != null && !telephone.equals(r.getTelephone())) {
                match = false;
            }
            // add to list if match
            if (match) {
                toDelete.add(crmID);
            }
        }

        // delete list
        for (String crmID : toDelete) {
            state.delete(crmID);
        }

        if (toDelete.isEmpty()) {
            System.out.println("No records found.");
        } else {
            System.out.println(toDelete.size() + " record(s) deleted.");
        }
    }
}