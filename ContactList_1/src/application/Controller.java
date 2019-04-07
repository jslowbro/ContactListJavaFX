package application;

import model.Contact;
import model.ContactStorage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;



public class Controller {


    @FXML
    private ListView<Contact> contactListView;

    @FXML
    private Label nameLabel;

    @FXML
    private Label surnameLabel;

    @FXML
    private Label numberLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private CheckBox showFavouritesCheckbox;

    @FXML
    private CheckBox favouritesCheckBox;

    @FXML
    private ImageView contactImageView;


    private FilteredList<Contact> contactFilteredList;
    private Predicate<Contact> wantAllItems;
    private Predicate<Contact> onlyFavourites;

    private SortedList<Contact> contactSortedList;


    public void initialize(){


        wantAllItems = new Predicate<Contact>() {
            @Override
            public boolean test(Contact contact) {
                return true;
            }
        };
        onlyFavourites = new Predicate<Contact>() {
            @Override
            public boolean test(Contact contact) {
                return contact.isFavourite();
            }
        };

        contactFilteredList = new FilteredList<Contact>(ContactStorage.getInstance().getContactObservableList(), wantAllItems);
        contactSortedList = new SortedList<Contact>(contactFilteredList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        contactListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Contact>() {
            @Override
            public void changed(ObservableValue<? extends Contact> observable, Contact oldValue, Contact newValue) {
                if(newValue != null){
                    Contact contact = contactListView.getSelectionModel().getSelectedItem();
                    nameLabel.setText(contact.getName());
                    surnameLabel.setText(contact.getSurname());
                    numberLabel.setText(contact.getNumber());
                    favouritesCheckBox.setSelected(contact.isFavourite());
                    File file = new File(contact.getPhotoPath());
                    Image image = new Image(file.toURI().toString());
                    contactImageView.setImage(image);
                    //TODO picture changes
                }
            }
        });


        contactListView.setItems(contactSortedList);
        contactListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contactListView.getSelectionModel().selectFirst();




    }

    @FXML
    public void handleAddNewContact(){

        FXMLLoader fxmlLoader = setUpFXMLLoader("addEditContactDialog.fxml");
        Dialog<ButtonType> dialog = setUpDialog("Add a new Contact", fxmlLoader);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && dialog.getResult() == ButtonType.OK) {
            AddEditContactController contactController = fxmlLoader.getController();
            Contact contact = contactController.processAddingContact();
            ContactStorage.getInstance().getContactObservableList().add(contact);
            contactListView.getSelectionModel().select(contact);
        } else {
            System.out.println("Didn't add new contact");
        }
    }

    @FXML
    public void handleDeleteContact(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Contact contact = contactListView.getSelectionModel().getSelectedItem();
        alert.setTitle("Delete selected contact");
        alert.setHeaderText("Deleting contact : " + contact.getName() + " " + contact.getSurname());
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            ContactStorage.getInstance().getContactObservableList().remove(contact);
            contactListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Didn't delete item");
        }
    }

    @FXML
    public void handleEditContact(){
        FXMLLoader fxmlLoader = setUpFXMLLoader("../resources/gui/addEditContactDialog.fxml");
        Dialog<ButtonType> dialog = setUpDialog("Edit a Contact", fxmlLoader);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){
            AddEditContactController contactController = fxmlLoader.getController();
            Contact oldContact = contactListView.getSelectionModel().getSelectedItem();
            Contact newContact = contactController.processEditingContact();
            oldContact.setName(newContact.getName());
            oldContact.setSurname(newContact.getSurname());
            oldContact.setNumber(newContact.getNumber());
            contactListView.getSelectionModel().select(oldContact);
        } else {
            System.out.println("Failed to edit an item");
        }

    }

    @FXML
    private void handleFavouriteCheckBox(){
        Contact contact = contactListView.getSelectionModel().getSelectedItem();
        contact.setFavourite(favouritesCheckBox.isSelected());
        contactListView.getSelectionModel().select(contact);
    }

    @FXML
    private void handleShowCheckBox(){
        if(showFavouritesCheckbox.isSelected()){
            contactFilteredList.setPredicate(onlyFavourites);
        } else {
            contactFilteredList.setPredicate(wantAllItems);
        }
        Contact contact = contactListView.getSelectionModel().getSelectedItem();
        if(contact != null){
            contactListView.getSelectionModel().select(contact);
        } else {
            contactListView.getSelectionModel().selectFirst();
        }
    }
    @FXML
    private void handleUpdatePhoto(){
        FileChooser chooser = new FileChooser();

        chooser.setTitle("Choose a picture");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image","*.jpg", "*.png")
        );

        File file = chooser.showOpenDialog(mainBorderPane.getScene().getWindow());

        if(file != null) {
            System.out.println(file.getPath());
            Contact contact = contactListView.getSelectionModel().getSelectedItem();
            contact.setPhotoPath(file.getPath());
            contactListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Photo update chooser was cancelled");
        }
    }

    @FXML
    private void handleLoadItemsXML() {
        ContactStorage contactInstance = ContactStorage.getInstance();
        saveWhenLoadingNew();
        contactInstance.setLoadtype("XML");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Contacts from XML file");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML", "*xml")
        );

        File file = chooser.showOpenDialog(mainBorderPane.getScene().getWindow());

        if(file!=null){
            contactInstance.setFilePath(file.getPath());
            contactInstance.loadContactsXML();
            contactListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Load Contacts from XML chooser was cancelled");
        }
    }
    @FXML
    private void handleLoadItemsDB(){
        ContactStorage contactInstance = ContactStorage.getInstance();
        saveWhenLoadingNew();
        contactInstance.setLoadtype("Database");
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Contacts from  a database");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Database", "*db")
        );

        File file = chooser.showOpenDialog(mainBorderPane.getScene().getWindow());

        if(file!=null){
            contactInstance.setFilePath("jdbc:sqlite:"+file.getPath());
            contactInstance.loadContactsDB();
            contactListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Load Contacts from database chooser was cancelled");
        }

    }

    private Dialog<ButtonType> setUpDialog(String title, FXMLLoader loader){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle(title);

        try{
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        return dialog;
    }

    private FXMLLoader setUpFXMLLoader(String path){
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource(path));
        return fxmlLoader;
    }


    private void saveWhenLoadingNew() {
        ContactStorage contactInstance = ContactStorage.getInstance();
        if(contactInstance.getLoadtype().equals("Database")){
            contactInstance.saveContactsDB();
        } else if(contactInstance.getLoadtype().equals("XML")) {
            contactInstance.saveContactsXML();
        }
        ContactStorage.getInstance().getContactObservableList().clear();
    }




















}
