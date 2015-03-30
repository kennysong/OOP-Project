import java.util.*;
import java.io.*;

public class GTFSParser {
    /* Returns a list of hash maps of each row (column name: value) in the CSV */
    public static ArrayList<Map<String, String>> readCSV(File file) throws IOException {
        // Prepare to read the input CSV file
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        // Some variables for the CSV file
        String[] headerRow = br.readLine().split(",");
        ArrayList<Map<String, String>> csvRows = new ArrayList<Map<String, String>>();

        // Iterate through each line in the CSV, adding it to the list
        while ((line = br.readLine()) != null) {
            String[] rowValues = line.split(",", -1); // -1 to accept empty strings
            Map<String, String> row = new HashMap<String, String>();
            for (int i = 0; i < headerRow.length; i++) {
                row.put(headerRow[i], rowValues[i]);
            }
            csvRows.add(row);
        }

        return csvRows;
    }
}
