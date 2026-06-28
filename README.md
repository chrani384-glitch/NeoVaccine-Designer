# NeoVaccine Designer

## Overview

NeoVaccine Designer is a desktop application developed using **JavaFX** and **Python Flask**. It helps predict peptide-HLA binding strength to identify potential neoantigens for personalized cancer vaccine research.

## Features

* Enter peptide sequences
* Select HLA alleles
* Predict peptide binding affinity
* Display results in tables and charts
* Export prediction results as CSV

## Technologies Used

* Java 17
* JavaFX
* Maven
* Python 3
* Flask
* REST API

## Project Structure

```
NeoVaccineDesigner/
│
├── backend/
│   ├── app.py
│   └── requirements.txt
│
├── frontend/
│   ├── pom.xml
│   └── src/
│
└── README.md
```

## How to Run

### Backend

1. Open the `backend` folder.
2. Install dependencies:

```
pip install -r requirements.txt
```

3. Start the Flask server:

```
python app.py
```

### Frontend

1. Open the `frontend` folder.
2. Run the JavaFX application:

```
mvn javafx:run
```

## Author

**Rania Lal**
BS Bioinformatics
International Islamic University Islamabad (IIUI)
