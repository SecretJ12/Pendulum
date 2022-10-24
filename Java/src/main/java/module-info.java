module de.secretj12 {
    requires javafx.controls;
    requires javafx.fxml;


    opens de.secretj12.Hauptfenster to javafx.fxml;
    exports de.secretj12.Hauptfenster;
    opens de.secretj12.Fenster_Verbinden to javafx.fxml;
    exports de.secretj12.Fenster_Verbinden;
}