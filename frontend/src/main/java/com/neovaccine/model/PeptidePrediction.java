package com.neovaccine.model;

/**
 * PeptidePrediction.java — A DATA CONTAINER (Model class)
 * =========================================================
 * This class is like a FORM with fields.
 * It holds the result of ONE peptide binding prediction.
 *
 * Example: 
 *   Peptide: SIINFEKL
 *   Allele:  HLA-A*02:01
 *   %Rank:   0.23  (Strong Binder!)
 *   Affinity: 85.4 nM
 *   Binder Level: SB
 *
 * In JavaFX, we use these model objects to fill the TableView.
 * JavaFX's TableView reads the properties from this class automatically!
 */

import javafx.beans.property.*;

public class PeptidePrediction {

    // ── JAVAFX PROPERTIES ───────────────────────────────────────────────
    // JavaFX uses special "Property" objects instead of plain variables.
    // This allows the TableView to AUTOMATICALLY UPDATE when data changes.
    // Think of Property like a "smart variable" that notifies the UI.

    private final StringProperty  peptide;           // The amino acid sequence, e.g. "SIINFEKL"
    private final IntegerProperty length;            // Peptide length, e.g. 8
    private final StringProperty  allele;            // HLA allele, e.g. "HLA-A*02:01"
    private final DoubleProperty  percentRank;       // %Rank score (lower = better binder)
    private final DoubleProperty  affinityNm;        // Binding affinity in nM
    private final StringProperty  binderLevel;       // "SB", "WB", or "NB"
    private final DoubleProperty  immunogenicityScore; // Predicted immunogenicity 0.0-1.0
    private final StringProperty  binderLevelDisplay;  // Human-friendly label

    // ── CONSTRUCTOR ─────────────────────────────────────────────────────
    // Constructor = a special method to CREATE a new PeptidePrediction object.
    // When we say: new PeptidePrediction("SIINFEKL", 8, "HLA-A*02:01", ...)
    // Java calls this constructor.
    public PeptidePrediction(
            String  peptide,
            int     length,
            String  allele,
            double  percentRank,
            double  affinityNm,
            String  binderLevel,
            double  immunogenicityScore) {

        this.peptide              = new SimpleStringProperty(peptide);
        this.length               = new SimpleIntegerProperty(length);
        this.allele               = new SimpleStringProperty(allele);
        this.percentRank          = new SimpleDoubleProperty(percentRank);
        this.affinityNm           = new SimpleDoubleProperty(affinityNm);
        this.binderLevel          = new SimpleStringProperty(binderLevel);
        this.immunogenicityScore  = new SimpleDoubleProperty(immunogenicityScore);

        // Create a display-friendly label for the binder level
        String display = switch (binderLevel) {
            case "SB" -> "⭐ Strong Binder";
            case "WB" -> "⚡ Weak Binder";
            default   -> "✗ Non-Binder";
        };
        this.binderLevelDisplay = new SimpleStringProperty(display);
    }

    // ── GETTERS (JavaFX Property style) ─────────────────────────────────
    // JavaFX TableView uses these "xxxProperty()" methods automatically.
    // For each field, we need TWO methods:
    //   1. peptideProperty()  → returns the Property object (for TableView binding)
    //   2. getPeptide()       → returns the plain String value

    // PEPTIDE
    public StringProperty  peptideProperty()             { return peptide; }
    public String          getPeptide()                  { return peptide.get(); }

    // LENGTH
    public IntegerProperty lengthProperty()              { return length; }
    public int             getLength()                   { return length.get(); }

    // ALLELE
    public StringProperty  alleleProperty()              { return allele; }
    public String          getAllele()                   { return allele.get(); }

    // PERCENT RANK — the key metric! Lower = better binder
    public DoubleProperty  percentRankProperty()         { return percentRank; }
    public double          getPercentRank()              { return percentRank.get(); }

    // AFFINITY IN nM — nanomolar binding affinity
    public DoubleProperty  affinityNmProperty()          { return affinityNm; }
    public double          getAffinityNm()               { return affinityNm.get(); }

    // BINDER LEVEL — raw: "SB", "WB", "NB"
    public StringProperty  binderLevelProperty()         { return binderLevel; }
    public String          getBinderLevel()              { return binderLevel.get(); }

    // BINDER LEVEL DISPLAY — pretty: "⭐ Strong Binder"
    public StringProperty  binderLevelDisplayProperty()  { return binderLevelDisplay; }
    public String          getBinderLevelDisplay()       { return binderLevelDisplay.get(); }

    // IMMUNOGENICITY SCORE — predicted T-cell activation potential
    public DoubleProperty  immunogenicityScoreProperty() { return immunogenicityScore; }
    public double          getImmunogenicityScore()      { return immunogenicityScore.get(); }

    // ── toString() ───────────────────────────────────────────────────────
    // Used for debugging — shows a quick summary in the console.
    @Override
    public String toString() {
        return String.format(
            "PeptidePrediction{peptide='%s', allele='%s', %%Rank=%.3f, level='%s'}",
            getPeptide(), getAllele(), getPercentRank(), getBinderLevel()
        );
    }
}
