public class Echo {
    public static void main(String[] args) {
        System.out.println("Arguments received:");
        // Loop through all elements in the args array
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument " + (i + 1) + ": " + args[i]);
        }
    }
}
