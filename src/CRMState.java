import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CRMState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, CRMRecord> records;

    public CRMState() {
        this.records = new HashMap<>();
    }

    public String create(CRMRecord record) {
        long id = newID();
        String key = String.valueOf(id);

        records.put(key, record);
        return key;
    }

    public Set<String> getAllIDs() {
        return records.keySet();
    }

    public CRMRecord retrieve(String id) {
        return records.get(id);
    }

    public boolean update(String id, CRMRecord newRecord) {
        CRMRecord currentRecord = records.get(id);
        if (currentRecord == null) {
            return false; // Dont update anything if the record doesnt exist.
        }
        if (newRecord.getGivenName() != null) {
            currentRecord.setGivenName(newRecord.getGivenName());
        }
        if (newRecord.getSurname() != null) {
            currentRecord.setSurname(newRecord.getSurname());
        }
        if (newRecord.getTelephone() != null) {
            currentRecord.setTelephone(newRecord.getTelephone());
        }
        if (newRecord.getPostcode() != null) {
            currentRecord.setPostcode(newRecord.getPostcode());
        }

        return true;
    }

    public CRMRecord delete(String id) {
        return records.remove(id);
    }

    public Map<String, CRMRecord> getRecords() { // method added so handlers can
        return records; // retrieve records from CRMState
    }

    private long newID() {
        long max = 0;
        for (String key : records.keySet()) {
            try {
                long id = Long.parseLong(key);
                if (id > max) {
                    max = id;
                }
            } catch (NumberFormatException e) {
                // ignore non-numeric IDs
            }
        }
        return max + 1;
    }
}
