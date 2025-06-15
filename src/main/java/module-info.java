module com.baiching.filesearch {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.sql;
    requires com.gluonhq.charm.glisten;
    requires com.gluonhq.attach.util;
    requires org.apache.lucene.core;

    opens com.baiching.filesearch to javafx.fxml;
    exports com.baiching.filesearch;
}