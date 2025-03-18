package org.example.csc311weatherdataanalyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


interface WeatherAnalyzer {

    record WeatherRecord(String date, double temperatureC, double humidity, double precipitation) {

        // temperature in Fahrenheit
        public double temperatureF() {
            return (temperatureC * 9 / 5) + 32;
        }

        // filter data by specific months
        public int getMonth() {
            if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {  // Format: YYYY-MM-DD
                return Integer.parseInt(date.substring(5, 7));
            }
            // The format of date listed : M/D/YY or MM/DD/YY
            else if (date.matches("\\d{1,2}/\\d{1,2}/\\d{2}")) {
                return Integer.parseInt(date.split("/")[0]);  // Extract month
            }
            System.err.println("Invalid date format: " + date);
            return -1;  // Return -1 if date format is invalid
        }
    }

    // Load the CSV file, convert from xlsm.
    static List<WeatherRecord> load(String filePath) throws IOException {
        try (var lines = Files.lines(Path.of(filePath))) {
            return lines.skip(1)  // Skip the header row
                    .map(line -> line.split(","))  // Split each line by commas
                    .map(parts -> new WeatherRecord(
                            parts[0],  // Date
                            Double.parseDouble(parts[1]),  // Temperature (Celsius)
                            Double.parseDouble(parts[2]),  // Humidity (%)
                            Double.parseDouble(parts[3])   // Precipitation (mm)
                    ))
                    .collect(Collectors.toList());
        }
    }

    // Average temperature for a specific month.
    static double averageTemperatureForMonth(List<WeatherRecord> data, int month) {
        return data.stream()
                .filter(record -> record.getMonth() == month)  // Filter by the specified month
                .mapToDouble(WeatherRecord::temperatureF)  //  Fahrenheit
                .average()
                .orElse(0.0);
    }

    // Count of rainy days.
    static long countRainyDays(List<WeatherRecord> data) {
        return data.stream()
                .filter(record -> record.precipitation() > 0)  // Count days when precipitation > 0
                .count();
    }

    // Days with temperatures above a given threshold.
    static List<WeatherRecord> daysAboveTemperature(List<WeatherRecord> data, double threshold) {
        return data.stream()
                .filter(record -> record.temperatureF() > threshold)  // Filter by threshold temperature
                .collect(Collectors.toList());
    }

    // Use an enhanced switch statement to determine weather categories (e.g., "Hot", "Warm", "Cold").
    static String categorizeWeather(double temperature) {
        return switch ((int) temperature / 10) {
            case 9, 10 -> "Very Hot";
            case 8 -> "Hot";
            case 7 -> "Warm";
            case 5, 6 -> "Cool";
            default -> "Cold";
        };
    }

    // Text block to make the report look more neat
    static void printWeatherReport(List<WeatherRecord> data) {
        String header = """
        ┌──────────┬──────────────┬──────────┬──────────────┬──────────────┐
        │   Date   │  Temperature │ Humidity │ Precipitation │  Category    │
        ├──────────┼──────────────┼──────────┼──────────────┼──────────────┤
        """;

        String rows = data.stream()
                .map(r -> String.format("│ %-8s │   %5.1f°F   │   %3.0f%%   │    %5.1f mm    │  %-12s │",
                        r.date(), r.temperatureF(), r.humidity(), r.precipitation(), categorizeWeather(r.temperatureF())))
                .collect(Collectors.joining("\n"));

        String footer = """
        └──────────┴──────────────┴──────────┴──────────────┴──────────────┘
        """;

        System.out.println(header + rows + "\n" + footer);
    }

    // Generate report and load data from CSV
    static void main(String[] args) throws IOException {
        var data = WeatherAnalyzer.load("/Users/aishashoaib/Downloads/weatherdata.csv");

        printWeatherReport(data);  // Print the formatted weather report

        // Compute and display statistics
        double avgTempAugust = averageTemperatureForMonth(data, 8);
        System.out.printf("\nAverage Temperature for August: %.2f°F\n", avgTempAugust);
        System.out.printf("Total Rainy Days: %d\n", countRainyDays(data));
        System.out.printf("Days Above 86°F: %d\n", daysAboveTemperature(data, 86).size());
    }
}
