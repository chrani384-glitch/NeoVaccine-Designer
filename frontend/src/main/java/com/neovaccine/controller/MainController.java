package com.neovaccine.controller;

import com.neovaccine.model.PeptidePrediction;
import com.neovaccine.service.ApiService;
import com.neovaccine.service.ApiService.PredictionResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    // ══════════════════════════════════════════════════════════════════
    // @FXML FIELDS — These are connected to elements in MainView.fxml
    // The name here MUST match the fx:id in the FXML file!
    // ══════════════════════════════════════════════════════════════════

    // ── LEFT PANEL: Input Controls ─────────────────────────────────────
    @FXML private TextArea         peptideInputArea;    // Where user types peptides
    @FXML private ListView<String> alleleListView;      // List of HLA alleles to select
    @FXML private Button           predictButton;       // "Run Prediction" button
    @FXML private Button           clearButton;         // "Clear All" button
    @FXML private Button           loadFileButton;      // "Load from File" button
    @FXML private ProgressBar      progressBar;         // Shows prediction progress
    @FXML private Label            statusLabel;         // Shows current status message

    // ── RIGHT PANEL: Results ───────────────────────────────────────────
    @FXML private TableView<PeptidePrediction> resultsTable;        // The big results table
    @FXML private TableColumn<PeptidePrediction, String>  colPeptide;
    @FXML private TableColumn<PeptidePrediction, Integer> colLength;
    @FXML private TableColumn<PeptidePrediction, String>  colAllele;
    @FXML private TableColumn<PeptidePrediction, Double>  colPercentRank;
    @FXML private TableColumn<PeptidePrediction, Double>  colAffinity;
    @FXML private TableColumn<PeptidePrediction, String>  colBinderLevel;
    @FXML private TableColumn<PeptidePrediction, Double>  colImmunogenicity;

    // ── SUMMARY LABELS ──────────────────────────────────────────────────
    @FXML private Label labelTotalPredictions;
    @FXML private Label labelStrongBinders;
    @FXML private Label labelWeakBinders;
    @FXML private Label labelNonBinders;

    // ── CHART AREA ──────────────────────────────────────────────────────
    @FXML private BarChart<String, Number>  bindingBarChart;    // Bar chart of %Rank by peptide
    @FXML private PieChart                  binderPieChart;      // Pie chart: SB vs WB vs NB
    @FXML private CategoryAxis              barChartXAxis;
    @FXML private NumberAxis                barChartYAxis;

    // ── EXPORT BUTTON ───────────────────────────────────────────────────
    @FXML private Button exportCsvButton;

    // ── FILTER CONTROLS ─────────────────────────────────────────────────
    @FXML private ComboBox<String> filterComboBox;    // Filter by: All, SB only, WB only
    @FXML private TextField        searchField;        // Search/filter by peptide sequence

    // ── BACKEND STATUS ──────────────────────────────────────────────────
    @FXML private Label backendStatusLabel;

    // ══════════════════════════════════════════════════════════════════
    // INSTANCE VARIABLES — Internal data used by the controller
    // ══════════════════════════════════════════════════════════════════
    private final ApiService apiService = new ApiService();
    private ObservableList<PeptidePrediction> allPredictions = FXCollections.observableArrayList();
    private ObservableList<PeptidePrediction> filteredPredictions = FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════════════════
    // initialize() — Called AUTOMATICALLY by JavaFX right after the
    // FXML is loaded. Like the "setup" phase before the app is shown.
    // ══════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();    // Configure the TableView columns
        setupAlleleList();      // Populate the HLA allele list
        setupCharts();          // Configure the bar & pie charts
        setupFilterControls();  // Set up search and filter dropdowns
        checkBackendStatus();   // Ping the Python server on startup
        setInitialStatus();     // Set the initial welcome message
    }

    // ══════════════════════════════════════════════════════════════════
    // SETUP: Configure TableView columns
    // ══════════════════════════════════════════════════════════════════
    private void setupTableColumns() {
        // Each column is told WHICH PROPERTY of PeptidePrediction to display.
        // "peptide" → calls getPeptide() on each PeptidePrediction object.
        colPeptide.setCellValueFactory(new PropertyValueFactory<>("peptide"));
        colLength.setCellValueFactory(new PropertyValueFactory<>("length"));
        colAllele.setCellValueFactory(new PropertyValueFactory<>("allele"));
        colPercentRank.setCellValueFactory(new PropertyValueFactory<>("percentRank"));
        colAffinity.setCellValueFactory(new PropertyValueFactory<>("affinityNm"));
        colBinderLevel.setCellValueFactory(new PropertyValueFactory<>("binderLevelDisplay"));
        colImmunogenicity.setCellValueFactory(new PropertyValueFactory<>("immunogenicityScore"));

        // Format the %Rank column to show 4 decimal places
        colPercentRank.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.4f", value));
                    // Color coding: green for SB, yellow for WB, red for NB
                    if (value < 0.5) {
                        setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;"); // Green
                    } else if (value < 2.0) {
                        setStyle("-fx-text-fill: #ffca28;"); // Yellow
                    } else {
                        setStyle("-fx-text-fill: #ef5350;"); // Red
                    }
                }
            }
        });

        // Format affinity column with 2 decimal places + "nM" unit
        colAffinity.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f nM", value));
                }
            }
        });

        // Format immunogenicity as a percentage
        colImmunogenicity.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", value * 100));
                }
            }
        });

        // Color-code the Binder Level column
        colBinderLevel.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value);
                    if (value.contains("Strong")) {
                        setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold;");
                    } else if (value.contains("Weak")) {
                        setStyle("-fx-text-fill: #ffca28;");
                    } else {
                        setStyle("-fx-text-fill: #ef5350;");
                    }
                }
            }
        });

        // Connect the table to our observable list
        resultsTable.setItems(filteredPredictions);
        
        // Allow column sorting (click column header to sort)
        resultsTable.setSortPolicy(tv -> true);
    }

    // ══════════════════════════════════════════════════════════════════
    // SETUP: Populate the allele list with checkboxes
    // ══════════════════════════════════════════════════════════════════
    private void setupAlleleList() {
        // Hard-coded alleles for now (will be fetched from backend in production)
        ObservableList<String> alleles = FXCollections.observableArrayList(
            "HLA-A*02:01", "HLA-A*01:01", "HLA-A*03:01", "HLA-A*24:02",
            "HLA-A*11:01", "HLA-A*26:01", "HLA-A*30:01", "HLA-A*31:01",
            "HLA-B*07:02", "HLA-B*08:01", "HLA-B*15:01", "HLA-B*35:01",
            "HLA-B*40:01", "HLA-B*44:02", "HLA-B*51:01", "HLA-B*57:01",
            "HLA-C*03:04", "HLA-C*04:01", "HLA-C*07:01", "HLA-C*07:02"
        );
        alleleListView.setItems(alleles);
        alleleListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Pre-select the most common alleles (A*02:01, B*07:02)
        alleleListView.getSelectionModel().select(0);  // HLA-A*02:01
        alleleListView.getSelectionModel().select(8);  // HLA-B*07:02
    }

    // ══════════════════════════════════════════════════════════════════
    // SETUP: Configure bar and pie charts
    // ══════════════════════════════════════════════════════════════════
    private void setupCharts() {
        bindingBarChart.setTitle("Peptide Binding Affinity (%Rank)");
        bindingBarChart.setAnimated(true);
        barChartXAxis.setLabel("Peptide");
        barChartYAxis.setLabel("%Rank (lower = better)");
        
        binderPieChart.setTitle("Binder Distribution");
        binderPieChart.setAnimated(true);
        binderPieChart.setLabelsVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    // SETUP: Filter dropdown and search field
    // ══════════════════════════════════════════════════════════════════
    private void setupFilterControls() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Results", "Strong Binders Only", "Weak Binders Only", "Non-Binders Only"
        ));
        filterComboBox.setValue("All Results");
        
        // When filter dropdown changes, re-filter the table
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        
        // When text in search field changes, re-filter the table
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    // ══════════════════════════════════════════════════════════════════
    // SETUP: Check if Python backend is running
    // ══════════════════════════════════════════════════════════════════
    private void checkBackendStatus() {
        // Run in background thread so UI doesn't freeze
        new Thread(() -> {
            boolean alive = apiService.isBackendAlive();
            Platform.runLater(() -> {
                if (alive) {
                    backendStatusLabel.setText("🟢 Backend Online");
                    backendStatusLabel.setStyle("-fx-text-fill: #00e676;");
                } else {
                    backendStatusLabel.setText("🔴 Backend Offline — Start Python server!");
                    backendStatusLabel.setStyle("-fx-text-fill: #ef5350;");
                }
            });
        }).start();
    }

    private void setInitialStatus() {
        statusLabel.setText("Enter peptide sequences and select HLA alleles, then click Predict.");
        progressBar.setProgress(0);
        exportCsvButton.setDisable(true);
    }

    // ══════════════════════════════════════════════════════════════════
    // ACTION: "Run Prediction" button click handler
    // This is the MAIN ACTION of the app!
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void onPredictClicked() {
        // ── Step 1: Get the peptides the user typed ──────────────────────
        String rawInput = peptideInputArea.getText().trim();
        if (rawInput.isEmpty()) {
            showAlert("No Input", "Please enter at least one peptide sequence.");
            return;
        }

        // Split by newlines, spaces, or commas — be flexible with input format
        List<String> peptides = Arrays.stream(rawInput.split("[\\n,\\s]+"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.toList());

        if (peptides.isEmpty()) {
            showAlert("No Peptides", "Could not parse any peptide sequences from the input.");
            return;
        }

        // ── Step 2: Get the selected alleles ─────────────────────────────
        List<String> selectedAlleles = new ArrayList<>(
            alleleListView.getSelectionModel().getSelectedItems()
        );

        if (selectedAlleles.isEmpty()) {
            showAlert("No Alleles", "Please select at least one HLA allele from the list.");
            return;
        }

        // ── Step 3: Run the prediction in a BACKGROUND THREAD ────────────
        // WHY? Because HTTP calls take time. If we run in the main thread,
        // the UI would FREEZE until the call completes. Bad user experience!
        // 
        // JavaFX Task = a job that runs in the background.
        // We update the UI (labels, progress bar) from the main thread using
        // Platform.runLater() — this is the proper way to update JavaFX UI
        // from a background thread.

        predictButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);  // Spinning animation
        statusLabel.setText("Running predictions for " + peptides.size() + 
                           " peptide(s) × " + selectedAlleles.size() + " allele(s)...");
        allPredictions.clear();
        filteredPredictions.clear();

        // Create the background task
        Task<PredictionResult> predictionTask = new Task<>() {
            @Override
            protected PredictionResult call() throws Exception {
                // This runs in the background thread
                return apiService.predict(peptides, selectedAlleles);
            }
        };

        // What to do when the task SUCCEEDS
        predictionTask.setOnSucceeded(event -> {
            PredictionResult result = predictionTask.getValue();
            
            // Update UI on the main thread
            allPredictions.addAll(result.predictions);
            applyFilter();
            updateSummaryLabels(result);
            updateCharts(result.predictions);
            
            progressBar.setProgress(1.0);
            statusLabel.setText(String.format(
                "✅ Done! %d predictions completed. Strong: %d | Weak: %d | Non: %d",
                result.total(), result.strongBinders, result.weakBinders, result.nonBinders
            ));
            predictButton.setDisable(false);
            exportCsvButton.setDisable(false);
        });

        // What to do when the task FAILS (e.g., backend offline)
        predictionTask.setOnFailed(event -> {
            Throwable error = predictionTask.getException();
            progressBar.setProgress(0);
            statusLabel.setText("❌ Error: " + error.getMessage());
            predictButton.setDisable(false);
            showAlert("Prediction Failed", 
                "Could not complete predictions.\n\nError: " + error.getMessage() +
                "\n\nMake sure the Python backend is running:\n  python backend/app.py");
        });

        // Start the background thread
        Thread thread = new Thread(predictionTask);
        thread.setDaemon(true);  // Daemon = stops when main app closes
        thread.start();
    }

    // ══════════════════════════════════════════════════════════════════
    // ACTION: Apply filter to the results table
    // ══════════════════════════════════════════════════════════════════
    private void applyFilter() {
        String filterValue = filterComboBox.getValue();
        String searchText  = searchField.getText().trim().toUpperCase();

        List<PeptidePrediction> filtered = allPredictions.stream()
            .filter(p -> {
                // Apply binder level filter
                boolean levelMatch = switch (filterValue) {
                    case "Strong Binders Only" -> p.getBinderLevel().equals("SB");
                    case "Weak Binders Only"   -> p.getBinderLevel().equals("WB");
                    case "Non-Binders Only"    -> p.getBinderLevel().equals("NB");
                    default -> true;  // "All Results"
                };

                // Apply peptide search filter
                boolean searchMatch = searchText.isEmpty() || 
                    p.getPeptide().toUpperCase().contains(searchText) ||
                    p.getAllele().toUpperCase().contains(searchText);

                return levelMatch && searchMatch;
            })
            .collect(Collectors.toList());

        filteredPredictions.setAll(filtered);
    }

    // ══════════════════════════════════════════════════════════════════
    // UPDATE: Summary statistics labels at the top
    // ══════════════════════════════════════════════════════════════════
    private void updateSummaryLabels(PredictionResult result) {
        labelTotalPredictions.setText(String.valueOf(result.total()));
        labelStrongBinders.setText(String.valueOf(result.strongBinders));
        labelWeakBinders.setText(String.valueOf(result.weakBinders));
        labelNonBinders.setText(String.valueOf(result.nonBinders));
    }

    // ══════════════════════════════════════════════════════════════════
    // UPDATE: Bar chart and pie chart with new data
    // ══════════════════════════════════════════════════════════════════
    private void updateCharts(List<PeptidePrediction> predictions) {
        // ── Bar Chart: Best %Rank per peptide ─────────────────────────────
        bindingBarChart.getData().clear();

        // Group predictions by peptide, get the best (min) %Rank per peptide
        Map<String, Double> bestByPeptide = predictions.stream()
            .collect(Collectors.toMap(
                PeptidePrediction::getPeptide,
                PeptidePrediction::getPercentRank,
                Math::min  // Keep the minimum %Rank (best binding)
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("%Rank Score");

        // Sort by %Rank ascending (best binders first)
        bestByPeptide.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .limit(15)  // Show top 15 only to avoid clutter
            .forEach(entry -> {
                XYChart.Data<String, Number> dataPoint = 
                    new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(dataPoint);
            });

        bindingBarChart.getData().add(series);

        // Color bars by binder level after rendering
        Platform.runLater(() -> {
            series.getData().forEach(dataPoint -> {
                double rank = dataPoint.getYValue().doubleValue();
                String color;
                if (rank < 0.5) color = "#00e676";      // Green = Strong Binder
                else if (rank < 2.0) color = "#ffca28"; // Yellow = Weak Binder
                else color = "#ef5350";                  // Red = Non-Binder

                if (dataPoint.getNode() != null) {
                    dataPoint.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        });

        // ── Pie Chart: SB vs WB vs NB Distribution ──────────────────────
        long sbCount = predictions.stream().filter(p -> p.getBinderLevel().equals("SB")).count();
        long wbCount = predictions.stream().filter(p -> p.getBinderLevel().equals("WB")).count();
        long nbCount = predictions.stream().filter(p -> p.getBinderLevel().equals("NB")).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Strong Binders (" + sbCount + ")", sbCount),
            new PieChart.Data("Weak Binders (" + wbCount + ")", wbCount),
            new PieChart.Data("Non-Binders (" + nbCount + ")", nbCount)
        );

        binderPieChart.setData(pieData);
    }

    // ══════════════════════════════════════════════════════════════════
    // ACTION: Clear all inputs and results
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void onClearClicked() {
        peptideInputArea.clear();
        allPredictions.clear();
        filteredPredictions.clear();
        bindingBarChart.getData().clear();
        binderPieChart.getData().clear();
        labelTotalPredictions.setText("0");
        labelStrongBinders.setText("0");
        labelWeakBinders.setText("0");
        labelNonBinders.setText("0");
        progressBar.setProgress(0);
        statusLabel.setText("Cleared. Enter new peptides to begin.");
        exportCsvButton.setDisable(true);
        searchField.clear();
    }

    // ══════════════════════════════════════════════════════════════════
    // ACTION: Load peptides from a text file
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void onLoadFileClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Peptide File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.fasta", "*.fa"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (file == null) return;  // User cancelled

        try {
            // Read all lines from the file
            List<String> lines = java.nio.file.Files.readAllLines(
    file.toPath(), java.nio.charset.StandardCharsets.UTF_8)
    .stream()
    .map(String::trim)
    .filter(line -> !line.isEmpty() && !line.startsWith(">"))
    .collect(Collectors.toList());

            // Put them in the text area, one per line
            peptideInputArea.setText(String.join("\n", lines));
            statusLabel.setText("Loaded " + lines.size() + " peptides from " + file.getName());
            
        } catch (IOException e) {
            showAlert("File Error", "Could not read the file:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ACTION: Export results to CSV
    // ══════════════════════════════════════════════════════════════════
    @FXML
    private void onExportCsvClicked() {
        if (allPredictions.isEmpty()) {
            showAlert("No Data", "Run a prediction first before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results as CSV");
        fileChooser.setInitialFileName("neovaccine_results.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(exportCsvButton.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            // Write CSV header
            writer.println("Peptide,Length,Allele,PercentRank,AffinityNM,BinderLevel,ImmunogenicityScore");
            
            // Write each prediction as a CSV row
            for (PeptidePrediction p : allPredictions) {
                writer.printf("%s,%d,%s,%.4f,%.2f,%s,%.3f%n",
                    p.getPeptide(), p.getLength(), p.getAllele(),
                    p.getPercentRank(), p.getAffinityNm(),
                    p.getBinderLevel(), p.getImmunogenicityScore()
                );
            }
            
            statusLabel.setText("✅ Exported " + allPredictions.size() + 
                               " results to: " + file.getName());
        } catch (IOException e) {
            showAlert("Export Error", "Could not save the file:\n" + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPER: Show a simple alert dialog
    // ══════════════════════════════════════════════════════════════════
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
