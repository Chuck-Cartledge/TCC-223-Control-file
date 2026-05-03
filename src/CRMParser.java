import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRMParser {
    private CRMController controller;

    private Deque<Node> sectionStack = new ArrayDeque<>(); // stack to manage nested sections
    private Map<String, SectionHandler> handlers = new HashMap<>(); // tag to handler class mapping

    public CRMParser(CRMController controller) {
        this.controller = controller;
        handlers.put("<System>", new SystemHandler());
        handlers.put("<Create>", new CreateHandler());
        handlers.put("<Retrieve>", new RetrieveHandler());
        handlers.put("<Update>", new UpdateHandler());
        handlers.put("<Delete>", new DeleteHandler());

    }

    public CRMController getController() {
        return controller;
    }

    public void parse(String fileName) {
        List<String> lines = read(fileName);
        for (String line : lines) {
            String trimmedLine = line.trim();
            // skip over empty lines and comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue;
            }
            if (trimmedLine.startsWith("<") && !trimmedLine.startsWith("</")) {
                // start Marker
                Node node = new Node();
                node.name = trimmedLine.replaceAll("\\s+", ""); // remove whitespace from tag name.
                if (!sectionStack.isEmpty()) {
                    sectionStack.peek().children.add(node);
                }
                sectionStack.push(node);
            } else if (trimmedLine.startsWith("</")) {
                // end Marker
                Node finished = sectionStack.pop();
                if (sectionStack.isEmpty()) {
                    handleNode(finished);
                }
            } else {
                // content
                if (!sectionStack.isEmpty()) {
                    sectionStack.peek().content.add(trimmedLine);
                }
            }
        }
    }

    private List<String> read(String fileName) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private void handleNode(Node node) {
        SectionHandler handler = handlers.get(node.name);

        if (handler != null) {
            handler.handle(node, this);
        } else {
            controller.trace("No handler for: " + node.name);
        }
    }
}