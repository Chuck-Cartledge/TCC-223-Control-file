package Scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

// Owns all CRM state; executes every CRUD, sort, search, and DB operation
public class ClientManager {

    HashMap<String, Client> listClients = new HashMap<>();
    String dbFileName = null;
    boolean traceOn   = false;

    // ArrayList tracks sort priority order; first field added = highest priority
    ArrayList<String> sortOrder = new ArrayList<>();

    // Temp buffers hold tag data between <Create>/<Update> open and close
    String tempSurname   = null;
    String tempGivenName = null;
    String tempPhone     = null;
    String tempZip       = null;

    // Search buffers persist after </Retrieve> until <Output> consumes them
    String searchSurname   = null;
    String searchGivenName = null;
    String searchPhone     = null;
    String searchZip       = null;
    String searchID        = null;

    void trace(String msg) {
        if (traceOn) System.out.println("[TRACE] " + msg);
    }

    void setTraceOn(boolean on)     { traceOn    = on; }
    void setDbFileName(String name) { dbFileName = name; }

    void setTempSurname(String v)   { tempSurname   = v; }
    void setTempGivenName(String v) { tempGivenName = v; }
    void setTempPhone(String v)     { tempPhone     = v; }
    void setTempZip(String v)       { tempZip       = v; }

    void setSearchID(String v)        { searchID        = v; }
    void setSearchSurname(String v)   { searchSurname   = v; }
    void setSearchGivenName(String v) { searchGivenName = v; }
    void setSearchPhone(String v)     { searchPhone     = v; }
    void setSearchZip(String v)       { searchZip       = v; }

    // Clear stale search criteria before each new Retrieve block
    void clearSearchBuffers() {
        searchSurname = searchGivenName = searchPhone = searchZip = searchID = null;
    }

    void clearSortOrder() { sortOrder.clear(); }

    void addSortField(String field) {
        sortOrder.add(field);
        trace("Sort priority added: " + field + " (priority=" + sortOrder.size() + ")");
    }

    @SuppressWarnings("unchecked")
    void loadDatabase() {
        if (dbFileName == null) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dbFileName))) {
            listClients = (HashMap<String, Client>) ois.readObject();
            System.out.println("[CRM] Database loaded: " + dbFileName + " (" + listClients.size() + " records)");
        } catch (FileNotFoundException e) {
            System.out.println("[CRM] No existing database found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[CRM] Load error: " + e.getMessage());
        }
    }

    void saveDatabase() {
        if (dbFileName == null) {
            System.err.println("[CRM] Save failed: no DBFileName set.");
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dbFileName))) {
            oos.writeObject(listClients);
            System.out.println("[CRM] Database saved: " + dbFileName + " (" + listClients.size() + " records)");
        } catch (IOException e) {
            System.err.println("[CRM] Save error: " + e.getMessage());
        }
    }

    // Removes the physical .ser file from disk — triggered by System <Delete>, not CRUD Delete
    void deleteDatabase() {
        if (dbFileName == null) {
            System.err.println("[CRM] Delete failed: no DBFileName set.");
            return;
        }
        File dbFile = new File(dbFileName);
        if (dbFile.exists()) {
            if (dbFile.delete()) {
                System.out.println("[CRM] Database file deleted: " + dbFileName);
            } else {
                System.err.println("[CRM] Delete failed: could not remove " + dbFileName);
            }
        } else {
            System.out.println("[CRM] Delete skipped: file not found -> " + dbFileName);
        }
        trace("File delete attempted: " + dbFileName);
    }

    void createClient() {
        if (tempSurname == null || tempGivenName == null) {
            System.err.println("[CRM] Warning: Missing required fields. Skipping record.");
            return;
        }

        // TEST_OVERRIDE gives test scripts a predictable CRMID instead of random UUID
        String newID;
        if ("TEST_OVERRIDE".equals(tempSurname)) {
            newID = "TEST-001";
        } else {
            // Loop regenerates UUID until no collision exists in the HashMap (O(1) check)
            do {
                newID = UUID.randomUUID().toString().substring(0, 8);
            } while (listClients.containsKey(newID));
        }

        Client newClient = new Client(newID, tempSurname, tempGivenName, tempPhone, tempZip);
        trace("HashMap.put: CRMID=" + newID);
        listClients.put(newID, newClient);
        System.out.println("[CRM] Client created: " + tempGivenName + " " + tempSurname + " | CRMID: " + newID);

        tempSurname = tempGivenName = tempPhone = tempZip = null;
    }

    // O(1) HashMap.get by CRMID; only overwrites fields that were set in the Update block
    void updateClient() {
        if (searchID == null) {
            System.err.println("[CRM] Update failed: no <CRMID> provided.");
            resetTempBuffers();
            return;
        }
        trace("HashMap.get: CRMID=" + searchID);
        Client c = listClients.get(searchID);
        if (c == null) {
            System.err.println("[CRM] Update failed: CRMID not found -> " + searchID);
            resetTempBuffers();
            return;
        }
        if (tempSurname != null)   c.setSurname(tempSurname);
        if (tempGivenName != null) c.setGivenName(tempGivenName);
        if (tempPhone != null)     c.setTelephone(tempPhone);
        if (tempZip != null)       c.setPostCode(tempZip);
        System.out.println("[CRM] 1 record updated | CRMID: " + searchID);
        resetTempBuffers();
    }

    private void resetTempBuffers() {
        searchID = null;
        tempSurname = tempGivenName = tempPhone = tempZip = null;
    }

    // O(1) remove by CRMID; falls back to O(n) scan when deleting by field criteria
    void deleteClients() {
        boolean deleteAll = "All".equals(searchID) || "NULL".equals(searchID) || "".equals(searchID);

        if (searchID != null && !deleteAll) {
            trace("HashMap.remove: CRMID=" + searchID);
            Client removed = listClients.remove(searchID);
            if (removed != null) {
                System.out.println("[CRM] 1 record deleted | CRMID: " + searchID);
            } else {
                System.err.println("[CRM] Delete failed: CRMID not found -> " + searchID);
            }
        } else {
            // Collect IDs into a separate ArrayList first to avoid ConcurrentModificationException
            ArrayList<String> toRemove = new ArrayList<>();
            for (Client c : listClients.values()) {
                boolean match = true;
                if (!deleteAll) {
                    if (searchSurname   != null && !fieldMatches(searchSurname,   c.getSurname()))    match = false;
                    if (searchGivenName != null && !fieldMatches(searchGivenName, c.getGivenName()))  match = false;
                    if (searchPhone     != null && !fieldMatches(searchPhone,     c.getTelephone()))  match = false;
                    if (searchZip       != null && !fieldMatches(searchZip,       c.getPostCode()))   match = false;
                }
                if (match) toRemove.add(c.getCrmID());
            }
            for (int i = 0; i < toRemove.size(); i++) {
                String id = toRemove.get(i);
                trace("HashMap.remove: CRMID=" + id);
                listClients.remove(id);
            }
            System.out.println("[CRM] " + toRemove.size() + " record(s) deleted.");
        }
        searchID = null;
        searchSurname = searchGivenName = searchPhone = searchZip = null;
    }

    void outputClients() {
        System.out.printf("%-10s %-15s %-15s %-15s %-10s%n", "CRMID", "Surname", "GivenName", "Phone", "Zip");
        System.out.println("------------------------------------------------------------------");

        // "All" and "NULL" are reserved IDs meaning return every record (§3.3)
        boolean allRecords = searchID == null
                || "All".equals(searchID)
                || "NULL".equals(searchID)
                || "".equals(searchID);

        // Dump matching records into an ArrayList so we can sort before printing (O(n) pass)
        ArrayList<Client> results = new ArrayList<>();
        if (allRecords) {
            for (Client c : listClients.values()) {
                results.add(c);
                trace("HashMap access: retrieved " + c.getCrmID());
            }
        } else {
            for (Client c : listClients.values()) {
                boolean match = true;
                if (searchID        != null && !fieldMatches(searchID,        c.getCrmID()))     match = false;
                if (searchSurname   != null && !fieldMatches(searchSurname,   c.getSurname()))   match = false;
                if (searchGivenName != null && !fieldMatches(searchGivenName, c.getGivenName())) match = false;
                if (searchPhone     != null && !fieldMatches(searchPhone,     c.getTelephone())) match = false;
                if (searchZip       != null && !fieldMatches(searchZip,       c.getPostCode()))  match = false;
                if (match) {
                    results.add(c);
                    trace("HashMap access: match found " + c.getCrmID());
                }
            }
        }

        // Delegate sort logic to ClientComparator; first non-zero comparison wins
        if (!sortOrder.isEmpty()) {
            trace("Sorting by fields: " + sortOrder);
            Collections.sort(results, new ClientComparator(sortOrder));
        }

        for (int i = 0; i < results.size(); i++) {
            Client c = results.get(i);
            System.out.printf("%-10s %-15s %-15s %-15s %-10s%n",
                c.getCrmID(), c.getSurname(), c.getGivenName(), c.getTelephone(), c.getPostCode());
        }

        // Search buffers are cleared here, not at </Retrieve>, so they survive until Output
        searchSurname = searchGivenName = searchPhone = searchZip = searchID = null;
    }

    // "NULL" is a reserved filter keyword meaning the field has no value
    private boolean fieldMatches(String filter, String clientField) {
        if (filter.equals("NULL")) {
            return clientField == null || clientField.isEmpty();
        }
        return filter.equals(clientField);
    }
}
