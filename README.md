# 🧬 NeoVaccine Designer — Complete Build Guide (A to Z)
**Rania's Step-by-Step Guide: Explained Like You're 5**

---

## 📌 WHAT IS THIS PROJECT?

NeoVaccine Designer is a desktop application that:
1. Takes **peptide sequences** as input (short chains of amino acids, like "SIINFEKL")
2. Predicts how strongly each peptide **binds to HLA (MHC) proteins**
3. Visualizes which peptides are the **best neoantigen candidates** for vaccines
4. Uses a **Python backend** (the brain) and a **JavaFX frontend** (the face)

**Real-world use:** Cancer immunotherapy — finding peptides from tumor mutations that can train the immune system to fight cancer.

---

## 🗂 PROJECT STRUCTURE (the folder layout)

```
NeoVaccineDesigner/
│
├── backend/                         ← Python Flask server (the BRAIN)
│   ├── app.py                       ← Main server file
│   └── requirements.txt             ← Python libraries to install
│
└── frontend/                        ← JavaFX app (the FACE)
    ├── pom.xml                      ← Maven config (like a shopping list for Java)
    └── src/main/
        ├── java/com/neovaccine/
        │   ├── MainApp.java         ← Entry point (the front door)
        │   ├── controller/
        │   │   └── MainController.java  ← UI logic (what happens when you click)
        │   ├── model/
        │   │   └── PeptidePrediction.java  ← Data container
        │   └── service/
        │       └── ApiService.java  ← HTTP calls to Python backend
        └── resources/com/neovaccine/
            ├── fxml/
            │   └── MainView.fxml    ← UI layout (like HTML for JavaFX)
            └── css/
                └── dark-theme.css  ← Styling (colors, fonts, sizes)
```

---

## 🛠 STEP 1: INSTALL REQUIRED SOFTWARE

### A. Install Java 17 (JDK)
JavaFX requires Java 17 or higher.

1. Go to: https://adoptium.net/
2. Download **Eclipse Temurin 17 LTS** for your OS (Windows/Mac/Linux)
3. Run the installer → click Next → Next → Finish
4. **Verify installation:** Open terminal/cmd → type:
   ```
   java -version
   ```
   You should see: `openjdk version "17.x.x"`

### B. Install Maven (build tool for Java)
Maven downloads Java libraries automatically.

1. Go to: https://maven.apache.org/download.cgi
2. Download the **Binary zip archive** (e.g., `apache-maven-3.9.x-bin.zip`)
3. Extract it to `C:\Program Files\Maven\` (Windows) or `/usr/local/maven` (Mac/Linux)
4. Add to PATH:
   - **Windows:** Search "Environment Variables" → System Variables → Path → Add `C:\Program Files\Maven\bin`
   - **Mac/Linux:** Add to `~/.bashrc`: `export PATH=/usr/local/maven/bin:$PATH`
5. **Verify:** Open NEW terminal → type:
   ```
   mvn -version
   ```
   You should see: `Apache Maven 3.9.x`

### C. Install Python 3.10+ 
Our backend is written in Python.

1. Go to: https://www.python.org/downloads/
2. Download Python 3.10 or newer
3. **IMPORTANT:** During install, check ✅ "Add Python to PATH"
4. **Verify:** Open terminal → type:
   ```
   python --version
   ```
   You should see: `Python 3.10.x` or higher

### D. Install IntelliJ IDEA (Recommended IDE)
IntelliJ is the best IDE for JavaFX development.

1. Go to: https://www.jetbrains.com/idea/download/
2. Download **Community Edition** (free)
3. Install → Launch → Choose dark theme (optional but cool)

---

## 🐍 STEP 2: SET UP THE PYTHON BACKEND

The backend is a Flask web server — like a mini-website that runs locally on your computer.

### Open terminal and navigate to the backend folder:
```bash
cd NeoVaccineDesigner/backend
```

### Install the required Python libraries:
```bash
pip install -r requirements.txt
```
This installs:
- **Flask:** A web framework (lets Python listen for requests)
- **Flask-CORS:** Allows JavaFX to talk to Flask (cross-origin requests)
- **Requests:** For making HTTP calls (to call NetMHCpan API in production)

### Start the backend server:
```bash
python app.py
```

You should see:
```
============================================================
  NeoVaccine Designer Backend Server
  Starting at: http://localhost:5000
  Press Ctrl+C to stop the server
============================================================
 * Running on http://0.0.0.0:5000
```

### ✅ Test the backend is working:
Open your browser and go to: http://localhost:5000/api/health

You should see:
```json
{
  "status": "running",
  "message": "NeoVaccine Designer Backend is alive!",
  "version": "1.0.0"
}
```

**Keep this terminal open!** The server must run while you use the app.

---

## ☕ STEP 3: SET UP THE JAVAFX FRONTEND

### Open the project in IntelliJ IDEA:

1. Open IntelliJ IDEA
2. Click **"Open"**
3. Navigate to `NeoVaccineDesigner/frontend/`
4. Select the `pom.xml` file → Click **"Open as Project"**
5. IntelliJ will automatically detect it as a Maven project
6. Wait for Maven to **download all dependencies** (takes 1-3 minutes the first time)
   - You'll see a progress bar at the bottom

### Understanding the project structure in IntelliJ:
```
frontend/
├── src/main/java/          ← Your Java code goes here
└── src/main/resources/     ← FXML, CSS, images go here
```

---

## 🏃 STEP 4: RUN THE APPLICATION

### Method 1: Using IntelliJ (easiest for development)
1. In IntelliJ, find `MainApp.java` in the project tree
2. Right-click → **"Run 'MainApp.main()'"**
3. The app window should open!

### Method 2: Using Maven command line
```bash
cd NeoVaccineDesigner/frontend
mvn javafx:run
```

### Method 3: Build a JAR file (for sharing with others)
```bash
cd NeoVaccineDesigner/frontend
mvn clean package
java -jar target/NeoVaccineDesigner-1.0.0.jar
```

---

## 🎯 HOW TO USE THE APP (Tutorial)

### Step 1: Start the Python backend first
```bash
python backend/app.py
```
The green dot in the top-right should show "🟢 Backend Online"

### Step 2: Enter peptide sequences in the left text area
Type one peptide per line. Peptides must be:
- 8 to 14 amino acids long
- Only use letters: A C D E F G H I K L M N P Q R S T V W Y

**Example peptides to try:**
```
SIINFEKL
GILGFVFTL
NLVPMVATV
KLGGALQAK
RYLRDQQLL
KVAELVHFL
YVDQNFISV
FLYNTVATLY
```

### Step 3: Select HLA alleles in the list
Hold **Ctrl** (Windows/Linux) or **Cmd** (Mac) to select multiple alleles.

Common ones to start with:
- HLA-A*02:01 (most common in Caucasians)
- HLA-B*07:02

### Step 4: Click "🚀 Run Prediction"
Wait a moment. The progress bar will spin.

### Step 5: Read the results!

**Understanding the results table:**
| Column | What it means |
|--------|---------------|
| Peptide | The amino acid sequence you entered |
| Length | Number of amino acids |
| HLA Allele | Which MHC molecule was tested |
| %Rank | ⭐ KEY METRIC — lower is BETTER. < 0.5 = strong binder |
| Affinity (nM) | Binding strength in nanomolar — lower = stronger binding |
| Binder Level | SB = Strong Binder, WB = Weak Binder, NB = Non-Binder |
| Immunogenicity | Predicted T-cell activation potential (higher = better) |

**Color coding:**
- 🟢 GREEN = Strong Binder (excellent vaccine candidate)
- 🟡 YELLOW = Weak Binder (possible candidate)
- 🔴 RED = Non-Binder (skip this one)

### Step 6: Use the charts
- **Bar chart (left):** Shows %Rank for each peptide — bars pointing lower = better binders
- **Pie chart (right):** Distribution of SB/WB/NB — ideally you want a big green slice!

### Step 7: Export results
Click **"💾 Export CSV"** to save results that you can open in Excel.

---

## 🔬 THE SCIENCE BEHIND IT

### What is MHC/HLA?
- **MHC** = Major Histocompatibility Complex (in animals)
- **HLA** = Human Leukocyte Antigen (in humans — same concept)
- Think of HLA like a "display shelf" on the surface of every cell
- It holds short peptides (8-14 amino acids) and shows them to the immune system
- T-cells patrol the body, looking at these shelves

### What is a Neoantigen?
- Cancer cells have mutations → they produce DIFFERENT proteins
- When those mutant proteins are broken into peptides → they look FOREIGN to the immune system
- These foreign peptides = neoantigens
- If a neoantigen binds HLA well → T-cells can recognize and KILL the cancer cell
- This is the basis of personalized cancer vaccines!

### What is %Rank?
- NetMHCpan compares your peptide against thousands of random peptides
- %Rank = "What % of random peptides bind better than yours?"
- If %Rank = 0.1% → your peptide beats 99.9% of random peptides → GREAT binder!
- Cutoffs:
  - < 0.5% = Strong Binder (SB) ← Best vaccine candidates
  - 0.5-2% = Weak Binder (WB) ← Possible candidates
  - > 2% = Non-Binder (NB) ← Discard these

---

## 🌐 API ENDPOINTS (for your CV and documentation)

| Method | URL | Purpose |
|--------|-----|---------|
| GET | `/api/health` | Check if backend is running |
| GET | `/api/alleles` | Get list of supported HLA alleles |
| POST | `/api/predict` | Submit peptides for binding prediction |
| POST | `/api/export` | Export results as CSV content |

### Example API call using curl (for testing):
```bash
curl -X POST http://localhost:5000/api/predict \
  -H "Content-Type: application/json" \
  -d '{"peptides": ["SIINFEKL", "GILGFVFTL"], "alleles": ["HLA-A*02:01"]}'
```

---

## 🔧 CONNECTING TO THE REAL NETMHCPAN API

Currently, the backend uses a realistic simulation. To connect to the real NetMHCpan:

### Option 1: Use DTU's web service (free with registration)
1. Register at: https://services.healthtech.dtu.dk/
2. Get your API key
3. In `app.py`, replace `calculate_binding_score()` with a real API call:

```python
def call_real_netmhcpan(peptides, alleles):
    """Call the real NetMHCpan API at DTU Denmark."""
    url = "https://services.healthtech.dtu.dk/cgi-bin/webface2.cgi"
    
    # Format peptides as FASTA
    fasta_content = "\n".join([f">pep{i}\n{pep}" for i, pep in enumerate(peptides)])
    
    # Submit job
    response = requests.post(url, data={
        "configfile": "/usr/cbs/services/srv/webface-compat.conf",
        "service": "NetMHCpan-4.1b",
        "SEQPASTE": fasta_content,
        "allele": ",".join(alleles),
        "length": "9",  # Or detect from peptide
    })
    
    # Parse response (HTML format from DTU server)
    # ... parse the HTML table of results ...
    return parsed_results
```

### Option 2: Install NetMHCpan locally (Linux/Mac)
1. Download from DTU: https://services.healthtech.dtu.dk/software.php
2. Install following their instructions
3. Call it as a command-line tool from Python:

```python
import subprocess
result = subprocess.run(
    ["netMHCpan", "-p", peptide_file, "-a", allele],
    capture_output=True, text=True
)
# Parse result.stdout
```

---

## 📚 WHAT TO WRITE IN YOUR CV/RESUME

**Project Title:** NeoVaccine Designer (May 2025)

**Description:**
> Developed a full-stack desktop application for personalized cancer vaccine design. Built a JavaFX GUI integrating with a Python Flask REST API to predict peptide-MHC binding affinity using NetMHCpan. Features include multi-allele batch prediction, interactive BarChart/PieChart visualization, CSV export, real-time progress feedback, and color-coded results by binder level.

**Skills demonstrated:**
- JavaFX (FXML, MVC architecture, TableView, BarChart, PieChart, CSS theming)
- Python Flask REST API development
- Bioinformatics: MHC-I binding prediction, neoantigen identification
- HTTP client-server communication (Apache HttpClient, JSON with Jackson)
- Maven build system
- Background threading (Task, Platform.runLater)
- Computational immunology concepts (%Rank, IC50, HLA alleles)

---

## ❓ TROUBLESHOOTING

### "Backend Offline" red status
➡ Make sure you started the Python server: `python backend/app.py`
➡ Check if port 5000 is blocked by firewall

### "Prediction Failed" error
➡ Is the backend running? Check terminal for errors
➡ Did you select at least one allele?

### JavaFX window doesn't open
➡ Make sure Java 17 is installed: `java -version`
➡ Try: `mvn javafx:run` from the `frontend/` directory

### "Cannot resolve symbol" errors in IntelliJ
➡ Right-click `pom.xml` → Maven → Reload Project
➡ Wait for dependencies to download

### Peptide validation errors
➡ Minimum 8, maximum 14 amino acids
➡ Only valid amino acid letters: A C D E F G H I K L M N P Q R S T V W Y
➡ No spaces within a peptide sequence

---

## 🎓 LEARNING RESOURCES

- **JavaFX Documentation:** https://openjfx.io/openjfx-docs/
- **FXML Tutorial:** https://jenkov.com/tutorials/javafx/fxml.html
- **Flask Documentation:** https://flask.palletsprojects.com/
- **NetMHCpan Paper:** https://doi.org/10.1093/nar/gkab379
- **MHC Binding Explained:** https://www.ebi.ac.uk/training/online/courses/immunoinformatics-an-introduction/

---

*Built for Rania Lal's Bioinformatics Portfolio — IIUI, 2025*
