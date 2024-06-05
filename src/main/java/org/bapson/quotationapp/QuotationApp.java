package org.bapson.quotationapp;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.print.*;
import javafx.embed.swing.SwingFXUtils;

// Adding table in a pdf using java
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.transform.Scale;
import java.awt.image.BufferedImage;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuotationApp extends Application {

    private TableView<Item> itemsTable;
    private TableView<PaymentInfo> paymentTable;
    private static final int TABLE_START_Y = 550;
    private static final int TABLE_START_X = 50;
    private static final int ROW_HEIGHT = 20;




    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Module module = Item.class.getModule();
        Module myModule = QuotationApp.class.getModule();
        module.addOpens(module.getDescriptor().name(), myModule);

        primaryStage.setTitle("Quotation Generator");

        // Create UI components
        TextField quotationNoField = new TextField();
        DatePicker quotationDatePicker = new DatePicker();

        // Initialize TableView for items
        itemsTable = createItemsTable();

        // Initialize TableView for payment information
        paymentTable = createPaymentTable();

        TextArea termsAndConditions = new TextArea();
        termsAndConditions.setPromptText("Enter terms and conditions...");

        TextField descriptionField = new TextField();
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        TextField unitPriceField = new TextField();

        Button addItemButton = new Button("Add Item");
        addItemButton.setOnAction(e -> {
            addItemToTable(itemsTable, descriptionField.getText(), quantitySpinner.getValue(), Double.parseDouble(unitPriceField.getText()));
            descriptionField.clear();
            quantitySpinner.getValueFactory().setValue(1);
            unitPriceField.clear();
            calculateTotal(itemsTable);
        });

        TextField accountNameField = new TextField();
        TextField accountNoField = new TextField();
        TextField bsbField = new TextField();
        TextField bankNameField = new TextField();

        Button addPaymentButton = new Button("Add Payment");
        addPaymentButton.setOnAction(e -> {
            addPaymentToTable(paymentTable, accountNameField.getText(), accountNoField.getText(), bsbField.getText(), bankNameField.getText());
            accountNameField.clear();
            accountNoField.clear();
            bsbField.clear();
            bankNameField.clear();
        });

        // Create UI components for customer details
        TextField customerNameField = new TextField();
        TextField customerAddressField = new TextField();
        TextField customerPhoneField = new TextField();
        TextField customerEmailField = new TextField();
        DatePicker expirationDatePicker = new DatePicker();
        TextField abnNumberField = new TextField();

        // Create a form for customer details
        GridPane customerDetailsForm = new GridPane();
        customerDetailsForm.setHgap(10);
        customerDetailsForm.setVgap(5);
        customerDetailsForm.addRow(0, new Label("Customer Name:"), customerNameField);
        customerDetailsForm.addRow(1, new Label("Customer Address:"), customerAddressField);
        customerDetailsForm.addRow(2, new Label("Customer Phone:"), customerPhoneField);
        customerDetailsForm.addRow(3, new Label("Customer Email:"), customerEmailField);
        customerDetailsForm.addRow(4, new Label("Expiration Date:"), expirationDatePicker);
        customerDetailsForm.addRow(5, new Label("ABN Number:"), abnNumberField);

        Button generatePdfButton = new Button("Generate PDF & Print");
//        generatePdfButton.setOnAction(e -> {
//            printPdf(quotationNoField.getText(), String.valueOf(quotationDatePicker.getValue()), itemsTable.getItems(),
//                    paymentTable.getItems(), termsAndConditions.getText());
//        });

        generatePdfButton.setOnAction(e -> {
            generatePdf(quotationNoField.getText(), String.valueOf(quotationDatePicker.getValue()), itemsTable.getItems(),
                    paymentTable.getItems(), termsAndConditions.getText(),customerNameField.getText(),customerAddressField.getText(),customerPhoneField.getText(),customerEmailField.getText(),expirationDatePicker.getValue(),abnNumberField.getText());
        });



        VBox leftVBox = new VBox(10);
        leftVBox.getChildren().addAll(
                new Label("Quotation Information"),
                new Label("Quotation No: "), quotationNoField,
                new Label("Quotation Date: "), quotationDatePicker,

                new Label("Customer Details"),  // Label for the customer details section
                customerDetailsForm , // Add the form to the UI


                new Label("Items Information"),
                itemsTable,
                new Label("Description:"), descriptionField,
                new Label("Quantity:"), quantitySpinner,
                new Label("Unit Price:"), unitPriceField,
                addItemButton
        );

        VBox rightVBox = new VBox(10);
        rightVBox.getChildren().addAll(
                new Label("Payment Information"),
                paymentTable,
                new Label("Account Name:"), accountNameField,
                new Label("Account No:"), accountNoField,
                new Label("BSB:"), bsbField,
                new Label("Bank Name:"), bankNameField,
                addPaymentButton
        );

        HBox topHBox = new HBox(10);
        topHBox.getChildren().addAll(leftVBox, rightVBox);

        VBox bottomVBox = new VBox(10);
        bottomVBox.getChildren().addAll(
                new Label("Terms and Conditions"),
                termsAndConditions,
                generatePdfButton
        );

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));
        layout.setTop(topHBox);
        layout.setBottom(bottomVBox);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(layout);

        Scene scene = new Scene(scrollPane, 800, 600);
        scene.getStylesheets().add("bootstrap3.css"); // Add Bootstrap stylesheet (replace with the actual path)
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TableView<Item> createItemsTable() {
        TableView<Item> table = new TableView<>();
        TableColumn<Item, String> descriptionColumn = new TableColumn<>("Description");
        TableColumn<Item, Integer> quantityColumn = new TableColumn<>("Quantity");
        TableColumn<Item, Double> unitPriceColumn = new TableColumn<>("Unit Price");
        TableColumn<Item, Double> amountColumn = new TableColumn<>("Amount");

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(descriptionColumn, quantityColumn, unitPriceColumn, amountColumn);
        return table;
    }

    private TableView<PaymentInfo> createPaymentTable() {
        TableView<PaymentInfo> table = new TableView<>();
        TableColumn<PaymentInfo, String> accountNameColumn = new TableColumn<>("Account Name");
        TableColumn<PaymentInfo, String> accountNoColumn = new TableColumn<>("Account No");
        TableColumn<PaymentInfo, String> bsbColumn = new TableColumn<>("BSB");
        TableColumn<PaymentInfo, String> bankNameColumn = new TableColumn<>("Bank Name");

        accountNameColumn.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        accountNoColumn.setCellValueFactory(new PropertyValueFactory<>("accountNo"));
        bsbColumn.setCellValueFactory(new PropertyValueFactory<>("bsb"));
        bankNameColumn.setCellValueFactory(new PropertyValueFactory<>("bankName"));

        table.getColumns().addAll(accountNameColumn, accountNoColumn, bsbColumn, bankNameColumn);
        return table;
    }

    private void addItemToTable(TableView<Item> table, String description, int quantity, double unitPrice) {
        Item newItem = new Item(description, quantity, unitPrice);
        table.getItems().add(newItem);
    }

    private void addPaymentToTable(TableView<PaymentInfo> table, String accountName, String accountNo, String bsb, String bankName) {
        PaymentInfo newPayment = new PaymentInfo(accountName, accountNo, bsb, bankName);
        table.getItems().add(newPayment);
    }

    private void calculateTotal(TableView<Item> table) {
        double subTotal = 0;
        for (Item item : table.getItems()) {
            subTotal += item.getAmount();
        }
        double gst = subTotal * 0.1;
        double totalAmount = subTotal + gst;

        System.out.println("Subtotal: " + subTotal);
        System.out.println("GST: " + gst);
        System.out.println("Total Amount: " + totalAmount);
    }

    private void generatePdf(String quotationNo, String quotationDate, Iterable<Item> items,
                             Iterable<PaymentInfo> paymentInfo, String termsAndConditions,
                             String customerName, String customerAddress, String customerPhone,
                             String customerEmail, LocalDate expirationDate, String abnNumber) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                PdfFont font = PdfFontFactory.createFont();
                document.setFont(font);

                // Left side header
                document.add(new Paragraph().add(new Text("Organization Name")));
                document.add(new Paragraph().add(new Text("Address: Your Organization Address")));
                document.add(new Paragraph().add(new Text("Phone: Your Organization Phone No")));
                document.add(new Paragraph().add(new Text("Email: Your Organization Email")));

                // Right side header
                Div headerDiv = new Div()
                        .setFixedPosition(500, 660, 50) // Adjust the position as needed
                        .add(new Paragraph().add(new Text("QUOTE").setTextAlignment(TextAlignment.RIGHT)))
                        .add(new Paragraph().add(new Text("QuotationNo: " + quotationNo).setTextAlignment(TextAlignment.RIGHT)))
                        .add(new Paragraph().add(new Text("QuotationDate: " + quotationDate).setTextAlignment(TextAlignment.RIGHT)))
                        .add(new Paragraph().add(new Text("Customer Name: " + customerName)))
                        .add(new Paragraph().add(new Text("Customer Address: " + customerAddress)))
                        .add(new Paragraph().add(new Text("Customer Phone: " + customerPhone)))
                        .add(new Paragraph().add(new Text("Customer Email: " + customerEmail)))
                        .add(new Paragraph().add(new Text("Expiration Date: " + String.valueOf(expirationDate))))
                        .add(new Paragraph().add(new Text("ABN Number: " + abnNumber)));

                document.add(headerDiv);

                // Draw tables
                drawTable(document, items, "Item Information");
                drawTable(document, paymentInfo, "Payment Information");

                document.add(new Paragraph().add(new Text("Terms and Conditions")));
                String[] termsLines = termsAndConditions.split("\n");
                for (String line : termsLines) {
                    document.add(new Paragraph().add(new Text(line)));
                }

                // Close the document
                document.close();

                System.out.println("PDF saved at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private <T> void drawTable(Document document, Iterable<T> data, String title) {
        if (!title.isEmpty()) {
            document.add(new Paragraph(title).setBold());
        }

        List<T> dataList = StreamSupport.stream(data.spliterator(), false)
                .collect(Collectors.toList());

        if (dataList.isEmpty()) {
            document.add(new Paragraph("No data available").setBold());
            document.add(new Paragraph("\n")); // Add spacing
            return;
        }

        T firstItem = dataList.get(0);
        Class<?> itemType = firstItem.getClass();

        float[] widths = getColumnWidths(dataList);
        Table table = new Table(widths);

        for (Field field : itemType.getDeclaredFields()) {
            field.setAccessible(true);
            table.addCell(new Cell().add(new Paragraph(field.getName()).setBold()));
        }

        for (T item : dataList) {
            table.startNewRow();

            for (Field field : itemType.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(item);

                    // Check if the field is a JavaFX property and get its value using the get() method
                    if (value instanceof StringProperty) {
                        value = ((StringProperty) value).get();
                    } else if (value instanceof IntegerProperty) {
                        value = ((IntegerProperty) value).get();
                    } else if (value instanceof DoubleProperty) {
                        value = ((DoubleProperty) value).get();
                    }

                    table.addCell(new Cell().add(new Paragraph(String.valueOf(value))));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        document.add(table);

        // Add spacing between tables
        document.add(new Paragraph("\n"));
    }








    private <T> float[] getColumnWidths(Iterable<T> data) {
        T firstItem = data.iterator().next();
        Class<?> itemType = firstItem.getClass();
        float[] widths = new float[itemType.getDeclaredFields().length + 1];
        Arrays.fill(widths, 100); // Adjust the width as needed
        return widths;
    }


    public static class Item {
        private final SimpleStringProperty description;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty unitPrice;

        public Item(String description, int quantity, double unitPrice) {
            this.description = new SimpleStringProperty(description);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
        }

        public String getDescription() {
            return description.get();
        }

        public int getQuantity() {
            return quantity.get();
        }

        public double getUnitPrice() {
            return unitPrice.get();
        }

        public double getAmount() {
            return quantity.get() * unitPrice.get();
        }
    }

    public static class PaymentInfo {
        private final SimpleStringProperty accountName;
        private final SimpleStringProperty accountNo;
        private final SimpleStringProperty bsb;
        private final SimpleStringProperty bankName;

        public PaymentInfo(String accountName, String accountNo, String bsb, String bankName) {
            this.accountName = new SimpleStringProperty(accountName);
            this.accountNo = new SimpleStringProperty(accountNo);
            this.bsb = new SimpleStringProperty(bsb);
            this.bankName = new SimpleStringProperty(bankName);
        }

        public String getAccountName() {
            return accountName.get();
        }

        public String getAccountNo() {
            return accountNo.get();
        }

        public String getBsb() {
            return bsb.get();
        }

        public String getBankName() {
            return bankName.get();
        }
    }
}
