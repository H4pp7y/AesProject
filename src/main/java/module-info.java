module com.example.aeskursach {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bouncycastle.provider;


    opens com.example.aeskursach to javafx.fxml;
    exports com.example.aeskursach;
}