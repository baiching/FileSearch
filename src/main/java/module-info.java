module com.baiching.filesearch {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.baiching.filesearch to javafx.fxml;
    exports com.baiching.filesearch;
}