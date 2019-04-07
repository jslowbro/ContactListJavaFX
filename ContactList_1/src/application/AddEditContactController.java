package application;

import model.Contact;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class AddEditContactController {

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField surnameTextField;

    @FXML
    private TextField numberTextField;

    public Contact processAddingContact() {
        Contact contact = new Contact(nameTextField.getText(),surnameTextField.getText(), numberTextField.getText(), "sample", false);
        //TODO photo path
        return contact;
    }
    public Contact processEditingContact() {
        //TODO photo path
        return new Contact(nameTextField.getText(),surnameTextField.getText(), numberTextField.getText(), "sample", false);
    }



}
