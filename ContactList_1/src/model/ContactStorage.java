package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.sql.*;

public class ContactStorage {

    private static ContactStorage instance = new ContactStorage();

    private static final String CONTACT = "contact";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String NOTES = "notes";
    private static final String PHOTO = "photo";



    private ObservableList<Contact> contactObservableList = FXCollections.observableArrayList();
    private String filePath;
    private String loadtype;

    public static ContactStorage getInstance() {
        return instance;
    }

    private ContactStorage() {
        filePath = "defaultpath.xml";
        loadtype = "default";
    }

    public ObservableList<Contact> getContactObservableList() {
        return contactObservableList;
    }

    public void setContactObservableList(ObservableList<Contact> contactObservableList) {
        this.contactObservableList = contactObservableList;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getLoadtype() {
        return loadtype;
    }

    public void setLoadtype(String loadtype) {
        this.loadtype = loadtype;
    }

    public void loadContactsDB() {
        try{
            Connection connection = DriverManager.getConnection(filePath);
            Statement statement = connection.createStatement();

            statement.execute("SELECT * FROM contacts");
            ResultSet resultSet = statement.getResultSet();
            boolean favourite;
            while(resultSet.next()){
                if(resultSet.getString("favourite").equals("true")) {
                    favourite = true;
                } else {
                    favourite = false;
                }
                Contact contact = new Contact(resultSet.getString("name"), resultSet.getString("surname"),
                        resultSet.getString("number"), resultSet.getString("photo"), favourite);
                contactObservableList.add(contact);
            }
            resultSet.close();
            statement.close();
            connection.close();

        }catch (SQLException e){
            System.out.println(getClass().toString()+e.getMessage());
        }
    }
    public void saveContactsDB() {
        try {
            Connection connection = DriverManager.getConnection(filePath);
            String query = "INSERT INTO contacts (name,surname,number,favourite,photo) VALUES(?, ?, ?, ?, ?)";
            Statement deleteStatement = connection.createStatement();
            deleteStatement.execute("DELETE FROM contacts");
            deleteStatement.close();
            PreparedStatement insertStatement = connection.prepareStatement(query);
            for(Contact contact : contactObservableList){
                Boolean fav = contact.isFavourite();
                insertStatement.setString(1, contact.getName());
                insertStatement.setString(2, contact.getSurname());
                insertStatement.setString(3, contact.getNumber());
                insertStatement.setString(4, fav.toString());
                insertStatement.setString(5, contact.getPhotoPath());
                insertStatement.execute();
            }
            insertStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println( getClass().toString()+e.getMessage());
        }
    }

    public void loadContactsXML() {
        try {

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            InputStream in = new FileInputStream(filePath);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            Contact contact = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals(CONTACT)) {
                        contact = new Contact();
                        continue;
                    }

                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart()
                                .equals(FIRST_NAME)) {
                            event = eventReader.nextEvent();
                            contact.setName(event.asCharacters().getData());
                            continue;
                        }
                    }
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(LAST_NAME)) {
                        event = eventReader.nextEvent();
                        contact.setSurname(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(PHONE_NUMBER)) {
                        event = eventReader.nextEvent();
                        contact.setNumber(event.asCharacters().getData());
                        continue;
                    }

                    if (event.asStartElement().getName().getLocalPart()
                            .equals(NOTES)) {
                        event = eventReader.nextEvent();
                        if(event.asCharacters().getData().equals("true")){
                            contact.setFavourite(true);
                        } else {
                            contact.setFavourite(false);
                        }
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart()
                            .equals(PHOTO)) {
                        event = eventReader.nextEvent();
                        if(event.asCharacters().getData() != null){
                            contact.setPhotoPath(event.asCharacters().getData());
                        } else {
                            contact.setPhotoPath("NullPhotoPath");
                        }
                        continue;
                    }

                }

                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(CONTACT)) {
                        try{
                            contactObservableList.add(contact);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void saveContactsXML() {

        try {

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

            XMLEventWriter eventWriter = outputFactory
                    .createXMLEventWriter(new FileOutputStream(filePath));

            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            XMLEvent end = eventFactory.createDTD("\n");

            StartDocument startDocument = eventFactory.createStartDocument();
            eventWriter.add(startDocument);
            eventWriter.add(end);

            StartElement contactsStartElement = eventFactory.createStartElement("",
                    "", "contacts");
            eventWriter.add(contactsStartElement);
            eventWriter.add(end);

            for (Contact contact: contactObservableList) {
                saveContactXML(eventWriter, eventFactory, contact);
            }

            eventWriter.add(eventFactory.createEndElement("", "", "contacts"));
            eventWriter.add(end);
            eventWriter.add(eventFactory.createEndDocument());
            eventWriter.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Problem with Contacts file: " + e.getMessage());
            e.printStackTrace();
        }
        catch (XMLStreamException e) {
            System.out.println("Problem writing contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveContactXML(XMLEventWriter eventWriter, XMLEventFactory eventFactory, Contact contact)
            throws FileNotFoundException, XMLStreamException {

        XMLEvent end = eventFactory.createDTD("\n");


        StartElement configStartElement = eventFactory.createStartElement("",
                "", CONTACT);
        eventWriter.add(configStartElement);
        eventWriter.add(end);

        createNodeXML(eventWriter, FIRST_NAME, contact.getName());
        createNodeXML(eventWriter, LAST_NAME, contact.getSurname());
        createNodeXML(eventWriter, PHONE_NUMBER, contact.getNumber());
        if(contact.isFavourite()){
            createNodeXML(eventWriter, NOTES, "true");
        } else {
            createNodeXML(eventWriter, NOTES, "false");
        }
        createNodeXML(eventWriter, PHOTO, contact.getPhotoPath());


        eventWriter.add(eventFactory.createEndElement("", "", CONTACT));
        eventWriter.add(end);
    }

    private void createNodeXML(XMLEventWriter eventWriter, String name,
                            String value) throws XMLStreamException {

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");

        StartElement sElement = eventFactory.createStartElement("", "", name);
        eventWriter.add(tab);
        eventWriter.add(sElement);

        Characters characters = eventFactory.createCharacters(value);
        eventWriter.add(characters);

        EndElement eElement = eventFactory.createEndElement("", "", name);
        eventWriter.add(eElement);
        eventWriter.add(end);
    }

}
