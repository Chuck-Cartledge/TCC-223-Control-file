package Scripts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Entry point and tag parser — delegates all data operations to ClientManager
public class CRM {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Scripts.CRM <filename>");
            return;
        }
        processControlFile(args[0]);
    }

    // Decode Base64 to UTF-8 plaintext (Based on Listing A.5)
    static String decode(String base64Str, ClientManager manager) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Str.trim());
            String result = new String(decodedBytes, StandardCharsets.UTF_8);
            manager.trace("Base64 decode: [" + base64Str.trim() + "] -> [" + result + "]");
            return result;
        } catch (Exception e) {
            manager.trace("Base64 passthrough: [" + base64Str.trim() + "]");
            return base64Str;
        }
    }

    public static void processControlFile(String fileName) {
        ClientManager manager  = new ClientManager();
        String currentTag      = "";
        String currentBlock    = "";
        StringBuilder dataCollector = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Rule 3: lines whose first non-whitespace char is # are comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Opening tag: reset buffer and set block/tag context
                if (line.startsWith("<") && !line.startsWith("</")) {
                    currentTag = line;
                    dataCollector.setLength(0);
                    manager.trace("Tag detected: " + line + " (block=" + currentBlock + ")");

                    // <Delete> inside System is an inline command — do NOT override currentBlock
                    if (currentBlock.equals("System") && line.equals("<Delete>")) {
                        // stay in System context; System Delete fires on </Delete>
                    } else if (line.equals("<Create>") || line.equals("<Retrieve>") || line.equals("<System>")
                            || line.equals("<Update>") || line.equals("<Delete>") || line.equals("<Sort>")) {
                        currentBlock = line.substring(1, line.length() - 1);
                        // Clear stale search buffers at the start of each new Retrieve block
                        if (line.equals("<Retrieve>")) {
                            manager.clearSearchBuffers();
                        }
                        // Reset sort order when a new Sort block begins
                        if (line.equals("<Sort>")) {
                            manager.clearSortOrder();
                        }
                    }
                }
                // Closing tag: decode buffer, assign fields, dispatch commands
                else if (line.startsWith("</")) {
                    String rawData     = dataCollector.toString();
                    String decodedData = decode(rawData, manager);
                    manager.trace("Tag closed: " + line + " (block=" + currentBlock + ")");

                    // <Comment> carries plain text — print raw, no Base64 decode
                    if (line.equals("</Comment>")) {
                        System.out.println(rawData);
                    }
                    // Field assignment gated by active block (case-sensitive tags per §3)
                    else if (currentBlock.equals("System")) {
                        if      (currentTag.equals("<DBFileName>")) manager.setDbFileName(decodedData);
                        // <Trace> ON/OFF toggles internal execution logging
                        else if (currentTag.equals("<Trace>"))      manager.setTraceOn(decodedData.trim().equals("ON"));
                    } else if (currentBlock.equals("Create")) {
                        if      (currentTag.equals("<Surname>"))   manager.setTempSurname(decodedData);
                        else if (currentTag.equals("<GivenName>")) manager.setTempGivenName(decodedData);
                        else if (currentTag.equals("<Telephone>")) manager.setTempPhone(decodedData);
                        else if (currentTag.equals("<PostCode>"))  manager.setTempZip(decodedData);
                    } else if (currentBlock.equals("Retrieve")) {
                        // CRMID is always plain text (§3.3); other fields are Base64
                        if      (currentTag.equals("<CRMID>"))     manager.setSearchID(rawData);
                        else if (currentTag.equals("<Surname>"))   manager.setSearchSurname(decodedData);
                        else if (currentTag.equals("<GivenName>")) manager.setSearchGivenName(decodedData);
                        else if (currentTag.equals("<Telephone>")) manager.setSearchPhone(decodedData);
                        else if (currentTag.equals("<PostCode>"))  manager.setSearchZip(decodedData);
                    } else if (currentBlock.equals("Update")) {
                        // CRMID is always plain text (§3.4)
                        if      (currentTag.equals("<CRMID>"))     manager.setSearchID(rawData);
                        else if (currentTag.equals("<Surname>"))   manager.setTempSurname(decodedData);
                        else if (currentTag.equals("<GivenName>")) manager.setTempGivenName(decodedData);
                        else if (currentTag.equals("<Telephone>")) manager.setTempPhone(decodedData);
                        else if (currentTag.equals("<PostCode>"))  manager.setTempZip(decodedData);
                    } else if (currentBlock.equals("Delete")) {
                        // CRMID is always plain text (§3.5)
                        if      (currentTag.equals("<CRMID>"))     manager.setSearchID(rawData);
                        else if (currentTag.equals("<Surname>"))   manager.setSearchSurname(decodedData);
                        else if (currentTag.equals("<GivenName>")) manager.setSearchGivenName(decodedData);
                        else if (currentTag.equals("<Telephone>")) manager.setSearchPhone(decodedData);
                        else if (currentTag.equals("<PostCode>"))  manager.setSearchZip(decodedData);
                    } else if (currentBlock.equals("Sort")) {
                        // Any closing tag inside Sort (except </Sort>) defines the next sort priority field
                        if (!line.equals("</Sort>")) {
                            String fieldName = line.substring(2, line.length() - 1);
                            manager.addSortField(fieldName);
                        }
                    }

                    // Command and block-close dispatch

                    // <Exit>: decode and print termination message, then stop (§3.1 Table 1)
                    if (line.equals("</Exit>")) {
                        if (!decodedData.isEmpty()) System.out.println(decodedData);
                        break;
                    }

                    if (line.equals("</Save>"))       { manager.saveDatabase(); }
                    if (line.equals("</Output>"))     { manager.outputClients(); }
                    if (line.equals("</Create>"))     { manager.createClient();  currentBlock = ""; }
                    // Load DB when DBFileName is resolved; </System> only closes the block
                    if (line.equals("</DBFileName>")) { manager.loadDatabase(); }
                    if (line.equals("</System>"))     { currentBlock = ""; }
                    // </Retrieve> only closes the block; search buffers persist for <Output>
                    if (line.equals("</Retrieve>"))   { currentBlock = ""; }
                    if (line.equals("</Update>"))     { manager.updateClient();  currentBlock = ""; }
                    if (line.equals("</Delete>")) {
                        // System-level Delete: remove the physical database file from disk
                        if (currentBlock.equals("System")) {
                            manager.deleteDatabase();
                        } else {
                            // CRUD Delete: remove matching client records from the HashMap
                            manager.deleteClients();
                            currentBlock = "";
                        }
                    }
                    if (line.equals("</Sort>")) { currentBlock = ""; }

                    // Reset tag and buffer after every closing tag
                    currentTag = "";
                    dataCollector.setLength(0);
                }
                // Accumulate content lines (multi-line Base64 or plain text) until closing tag
                else if (!currentTag.isEmpty()) {
                    dataCollector.append(line);
                }
            }
        } catch (IOException e) {
            System.err.println("[CRM] File error: " + e.getMessage());
        }
    }
}
