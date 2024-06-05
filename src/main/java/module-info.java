module org.bapson.quotationapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires javafx.swing;
    requires kernel;
    requires layout;

    opens org.bapson.quotationapp to javafx.fxml, javafx.base, java.base;
    exports org.bapson.quotationapp;
}
