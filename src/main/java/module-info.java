module com.snake {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;

    opens com to javafx.fxml;

    exports com;
}
