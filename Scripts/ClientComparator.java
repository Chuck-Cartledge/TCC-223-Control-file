package Scripts;

import java.util.ArrayList;
import java.util.Comparator;

// Handles cumulative multi field sort priority defined by <Sort> blocks
public class ClientComparator implements Comparator<Client> {

    private ArrayList<String> sortOrder;

    public ClientComparator(ArrayList<String> sortOrder) {
        this.sortOrder = sortOrder;
    }

    // First non zero comparison result wins, later fields are tiebreakers
    public int compare(Client a, Client b) {
        for (int i = 0; i < sortOrder.size(); i++) {
            String field = sortOrder.get(i);
            int cmp = getField(a, field).compareTo(getField(b, field));
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    // Returns "" for null fields so compare() never throws NullPointerException
    private String getField(Client c, String field) {
        if (field.equals("Surname"))   return c.getSurname()   != null ? c.getSurname()   : "";
        if (field.equals("GivenName")) return c.getGivenName() != null ? c.getGivenName() : "";
        if (field.equals("Telephone")) return c.getTelephone() != null ? c.getTelephone() : "";
        if (field.equals("PostCode"))  return c.getPostCode()  != null ? c.getPostCode()  : "";
        if (field.equals("CRMID"))     return c.getCrmID()     != null ? c.getCrmID()     : "";
        return "";
    }
}
