package simulator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    private FileHandler() {
    }

    public static List<Double> parseNumberList(String relPath) {
        try {

            InputStream fstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(relPath);// FileHandler.class.getResourceAsStream(relPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            List<Double> numbers = new ArrayList<>();
            String strLine;

            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                // System.out.println(strLine);
                numbers.add(Double.parseDouble(strLine));
            }

            // Close the input stream
            br.close();
            return numbers;
        } catch (Exception e) {
            System.out.println("Couldn't parse file " + relPath + ":");
            e.printStackTrace(System.out);
        }
        return null;
    }
}
