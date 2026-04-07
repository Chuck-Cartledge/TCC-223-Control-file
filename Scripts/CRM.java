package Scripts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class CRM {
    // Temporary Database
    static ArrayList<Client> listaClientes = new ArrayList<>();

    // Temporary variables to build a client
    static String tempSurname, tempGivenName, tempPhone, tempZip;

    // Function to convert Base64 to normal text (Based on Listing A.5)
    public static String decode(String base64Str) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Str.trim());
            return new String(decodedBytes);
        } catch (Exception e) {
            return base64Str; // If not base64, return the original
        }
    }

    public static void main(String[] args) {
        //
        if (args.length < 1) {
            System.out.println("Usage: java Scripts.CRM <filename>");
            return;
        }

        String controlFile = args[0];

        String currentTag = ""; // To know if we are inside <Surname>, <GivenName>, etc.
        StringBuilder dataCollector = new StringBuilder(); // To join Base64 lines

        //
        try (BufferedReader reader = new BufferedReader(new FileReader(controlFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // If the line is empty or a comment (#), ignore it
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // TAG PROCESSING LOGIC
                if (line.startsWith("<") && !line.startsWith("</")) {
                    // It's an opening tag
                    currentTag = line;
                    dataCollector.setLength(0); // Clean data
                } else if (line.startsWith("</")) {
                    // It's a closing tag
                    String finalData = dataCollector.toString();
                    String decodedData = decode(finalData);

                    // DATA ASSIGNMENT ACCORDING TO THE TAG
                    if (currentTag.equals("<Surname>"))
                        tempSurname = decodedData;
                    if (currentTag.equals("<GivenName>"))
                        tempGivenName = decodedData;
                    if (currentTag.equals("<Telephone>"))
                        tempPhone = decodedData;
                    if (currentTag.equals("<PostCode>"))
                        tempZip = decodedData;

                    // IF THE <Create> GROUP ENDS, WE SAVE THE CLIENT
                    if (line.equals("</Create>")) {
                        String id = UUID.randomUUID().toString().substring(0, 8); // Random ID
                        Client nuevo = new Client(id, tempSurname, tempGivenName, tempPhone, tempZip);
                        listaClientes.add(nuevo);
                        System.out.println("Client created: " + tempSurname + " (ID: " + id + ")");
                    }

                    currentTag = ""; // Reset current tag
                } else {
                    // If it's not a tag, it's Base64 content. We save it.
                    if (!currentTag.isEmpty()) {
                        dataCollector.append(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
