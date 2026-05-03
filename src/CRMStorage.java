import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CRMStorage {

    public static String save(String fileName, CRMState state) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(state);
            return null; // successful
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public static CRMState load(String fileName) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (CRMState) in.readObject();
        }
    }

    public static boolean delete(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            return false;
        }
        // if and only if the file or directory is successfully deleted; false
        // otherwise.
        return file.delete();
    }
}
