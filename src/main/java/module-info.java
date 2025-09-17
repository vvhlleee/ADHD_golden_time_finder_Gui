module org.example.adhd_test_gui {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.adhd_test_gui to javafx.fxml;
    exports org.example.adhd_test_gui;
}