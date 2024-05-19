module com.example.imagesdemo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.imagesdemo to javafx.fxml;
    exports com.example.imagesdemo;
}