// Shazil Taqi Project 3 

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class IRoadTrip {
    // Maps to store data
    private Map<String, Set<String>> bordersMap;
    private Map<String, Map<String, Integer>> distancesMap;
    private Map<String, String> countryNameMap;

    // Constructor initializes maps and reads input files
    public IRoadTrip() {
        bordersMap = new HashMap<>();
        distancesMap = new HashMap<>();
        countryNameMap = new HashMap<>();

        // Hardcoded file paths
        String bordersFilePath = "/Users/shaziltaqi/Documents/GitHub/project-assignment-3-iroadtrip-ShazilT2005/borders.txt";
        String distancesFilePath = "/Users/shaziltaqi/Documents/GitHub/project-assignment-3-iroadtrip-ShazilT2005/capdist.csv";
        String stateNameFilePath = "/Users/shaziltaqi/Documents/GitHub/project-assignment-3-iroadtrip-ShazilT2005/state_name.tsv";

        try {
            // Read and parse borders.txt
            readBordersFile(bordersFilePath);

            // Read and parse capdist.csv
            readDistancesFile(distancesFilePath);

            // Read and parse state_name.tsv
            readStateNameFile(stateNameFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Reads and parses the borders file
    private void readBordersFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                String country = parts[0].trim();
                String[] neighbors = parts[1].split(";");
                Set<String> borderingCountries = new HashSet<>(Arrays.asList(neighbors));
                bordersMap.put(country, borderingCountries);
            }
        }
    }

    // Reads and parses the distances file
    private void readDistancesFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String countryA = parts[1].trim();
                String countryB = parts[3].trim();
                try {
                    int distance = Integer.parseInt(parts[4].trim());
                    distancesMap.computeIfAbsent(countryA, k -> new HashMap<>()).put(countryB, distance);
                    distancesMap.computeIfAbsent(countryB, k -> new HashMap<>()).put(countryA, distance);
                } catch (NumberFormatException e) {
                    // Handle invalid distance value (e.g., log an error, skip the entry, etc.)
                }
            }
        }
    }

    // Reads and parses the state name file
    private void readStateNameFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");

                // Ensure that the array has enough elements
                if (parts.length >= 5) {
                    String countryId = parts[1].trim();
                    String countryName = parts[2].trim().toLowerCase();  // Convert to lowercase
                    String endDate = parts[4].trim();

                    // Take information corresponding to 2020-12-31
                    if (endDate.equals("2020-12-31")) {
                        countryNameMap.put(countryId, countryName);
                    }
                } else {
                    // Handle invalid line format (e.g., log an error, skip the entry, etc.)
                    System.err.println("Invalid line format in state_name.tsv: " + line);
                }
            }
        }
    }

    // Returns the distance between two countries
    public int getDistance(String country1, String country2) {
        // Check if countries exist
        if (!countryNameMap.containsKey(country1) || !countryNameMap.containsKey(country2)) {
            return -1; // One or both countries do not exist
        }

        Set<String> bordersCountry1 = bordersMap.get(countryNameMap.get(country1));
        if (bordersCountry1 == null || !bordersCountry1.contains(countryNameMap.get(country2))) {
            return -1; // Countries do not share a land border
        }

        Map<String, Integer> distancesFromCountry1 = distancesMap.get(country1);
        if (distancesFromCountry1 == null || !distancesFromCountry1.containsKey(country2)) {
            return -1; // No distance information between countries
        }

        return distancesFromCountry1.get(country2);
    }

    // Finds the path between two countries using BFS
    // Finds the path between two countries using BFS
public List<String> findPath(String country1, String country2) {
    // Check if countries exist
    if (!countryNameMap.containsKey(country1) || !countryNameMap.containsKey(country2)) {
        return Collections.emptyList(); // One or both countries do not exist
    }

    Set<String> visited = new HashSet<>();
    Queue<String> queue = new LinkedList<>();
    Map<String, String> parentMap = new HashMap<>();

    String normalizedCountry1 = countryNameMap.get(country1);
    String normalizedCountry2 = countryNameMap.get(country2);

    queue.offer(normalizedCountry1);
    visited.add(normalizedCountry1);

    while (!queue.isEmpty()) {
        String currentCountry = queue.poll();

        // Check if the current country has neighbors
        Set<String> neighbors = bordersMap.get(currentCountry);
        if (neighbors == null) {
            continue;
        }

        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                queue.offer(neighbor);
                visited.add(neighbor);
                parentMap.put(neighbor, currentCountry);
            }
        }
    }

    return buildPath(normalizedCountry1, normalizedCountry2, parentMap);
}


    // Builds the path between two countries
    private List<String> buildPath(String start, String end, Map<String, String> parentMap) {
        List<String> path = new ArrayList<>();
        String currentCountry = end;

        while (currentCountry != null && !currentCountry.equals(start)) {
            String parent = parentMap.get(currentCountry);

            if (parent != null) {
                // Check if there is distance information between the parent and current country
                if (distancesMap.containsKey(parent) && distancesMap.get(parent).containsKey(currentCountry)) {
                    int distance = distancesMap.get(parent).get(currentCountry);
                    path.add(parent + " --> " + currentCountry + " (" + distance + " km.)");
                } else {
                    // Handle cases where there is no distance information
                    path.add(parent + " --> " + currentCountry);
                }
            } else {
                // Handle cases where the parent is null
                path.add(currentCountry);
            }

            currentCountry = parent;
        }

        Collections.reverse(path);
        return path;
    }

    // Accepts user input for finding paths between countries
    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the name of the first country (type EXIT to quit): ");
            String country1 = scanner.nextLine().trim();

            if (country1.equalsIgnoreCase("EXIT")) {
                break; // Exit the loop if the user types EXIT
            }

            String normalizedCountry1 = country1.toUpperCase(); // Convert to uppercase for case-insensitive comparison

            if (!countryNameMap.containsKey(normalizedCountry1)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue; // Prompt the user again if the country name is invalid
            }

            System.out.println("Enter the name of the second country (type EXIT to quit): ");
            String country2 = scanner.nextLine().trim();

            if (country2.equalsIgnoreCase("EXIT")) {
                break; // Exit the loop if the user types EXIT
            }

            String normalizedCountry2 = country2.toUpperCase(); // Convert to uppercase for case-insensitive comparison

            if (!countryNameMap.containsKey(normalizedCountry2)) {
                System.out.println("Invalid country name. Please enter a valid country name.");
                continue; // Prompt the user again if the country name is invalid
            }

            List<String> path = findPath(normalizedCountry1, normalizedCountry2);

            if (path.isEmpty()) {
                System.out.println("No path exists between " + normalizedCountry1 + " and " + normalizedCountry2);
            } else {
                System.out.println("Route from " + normalizedCountry1 + " to " + normalizedCountry2 + ":");
                for (String step : path) {
                    System.out.println("* " + step);
                }
            }
        }
    }

    // Main method to execute the program
    public static void main(String[] args) {
        IRoadTrip roadTrip = new IRoadTrip();
        roadTrip.acceptUserInput();
    }
}
