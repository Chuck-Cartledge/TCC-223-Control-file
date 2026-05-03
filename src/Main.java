import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the name of the CRM control file: ");
            String controlFileName = scanner.nextLine();
            controlFileName = controlFileName.isEmpty() ? "sample.txt" : controlFileName; // default to sample.txt
            CRMController controller = new CRMController();
            controller.run(controlFileName);
        }

    }
}
