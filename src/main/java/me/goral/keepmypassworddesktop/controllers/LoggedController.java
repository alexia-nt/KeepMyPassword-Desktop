package me.goral.keepmypassworddesktop.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import me.goral.keepmypassworddesktop.database.DatabaseHandler;
import me.goral.keepmypassworddesktop.util.AESUtil;
import me.goral.keepmypassworddesktop.util.AlertsUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class LoggedController {

    @FXML private TableView<PasswordRow> contentTable;
    @FXML private TableColumn<PasswordRow, String> descColumn;
    @FXML private TableColumn<PasswordRow, String> loginColumn;
    @FXML private TableColumn<PasswordRow, String> pwdColumn;
    @FXML private TableColumn<PasswordRow, String> ivColumn;
    @FXML private Label unameLabel;
    private SecretKey key;

    @FXML
    private void initialize() {
        descColumn.setCellValueFactory(new PropertyValueFactory<PasswordRow, String>("Description"));
        loginColumn.setCellValueFactory(new PropertyValueFactory<PasswordRow, String>("Login"));
        pwdColumn.setCellValueFactory(new PropertyValueFactory<PasswordRow, String>("Password"));
        ivColumn.setCellValueFactory(new PropertyValueFactory<PasswordRow, String>("IV"));
        ivColumn.setVisible(false);

        System.out.println("AFTER RECEIVING KEY");
        System.out.println(key);

        refreshContentTable();
    }

    @FXML
    private void onAddClick(){
        System.out.println("KEY ON ADD");
        System.out.println(Arrays.toString(key.getEncoded()));
        //AlertsUtil.showAddPasswordDialog(key);
        //refreshContentTable();
    }

    @FXML
    private void onDeleteAccountClick() {
        key = null;
        unameLabel.setText("");
        AlertsUtil.showDeleteAccountDialog();
    }

    @FXML
    private void onDeleteDataClick() {
        AlertsUtil.showDeleteDataDialog();
        refreshContentTable();
    }

    @FXML
    private void onLogoutButtonClick() {
        key = null;
        unameLabel.setText("");
        AlertsUtil.showLogoutDialog();
    }

    private void refreshContentTable() {
        contentTable.getItems().setAll(parsePasswordsList());
    }

    public void setSecretKey(SecretKey k){
        System.out.println("EXCHANGE");
        key = k;
    }

    public void setUnameLabel(String uname) {
        unameLabel.setText(uname);
    }

    private List<PasswordRow> parsePasswordsList() {
        List<PasswordRow> list = new ArrayList<>();

        List<List<String>> passwordsFromDb = DatabaseHandler.selectPasswords();
        try {
            if (!passwordsFromDb.isEmpty()) {
                for (List<String> r : passwordsFromDb){
                    String descEnc = r.get(0);
                    String loginEnc = r.get(1);
                    String pwdEnc = r.get(2);
                    String ivEnc = r.get(3);
                    IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(ivEnc));

                    System.out.println(key);

                    //decrypt data from database
                    String descDec = AESUtil.decrypt("AES/CBC/PKCS5Padding", new String(Base64.getDecoder().decode(descEnc)), key, iv);
                    String loginDec = AESUtil.decrypt("AES/CBC/PKCS5Padding", new String(Base64.getDecoder().decode(loginEnc)), key, iv);
                    String pwdDec = AESUtil.decrypt("AES/CBC/PKCS5Padding", new String(Base64.getDecoder().decode(pwdEnc)), key, iv);

                    //add decrypted things to new PasswordRow
                    PasswordRow pr = new PasswordRow(descDec, loginDec, pwdDec, ivEnc);

                    //new PasswordRow to list
                    list.add(pr);
                }
            }
        } catch (Exception e){
            AlertsUtil.showExceptionStackTraceDialog(e);
        }

        return list;
    }

    public static class PasswordRow {
        private final SimpleStringProperty desc;
        private final SimpleStringProperty login;
        private final SimpleStringProperty pwd;
        private final SimpleStringProperty iv;

        private PasswordRow(String desc, String login, String pwd, String iv){
            this.desc = new SimpleStringProperty(desc);
            this.login = new SimpleStringProperty(login);
            this.pwd = new SimpleStringProperty(pwd);
            this.iv = new SimpleStringProperty(iv);
        }

        public String getDesc() {
            return desc.get();
        }

        public void setDesc(String desc) {
            this.desc.set(desc);
        }

        public String getLogin() {
            return login.get();
        }

        public void setLogin(String login) {
            this.login.set(login);
        }

        public String getPwd() {
            return pwd.get();
        }

        public void setPwd(String pwd) {
            this.pwd.set(pwd);
        }

        public String getIv() {
            return iv.get();
        }

        public SimpleStringProperty ivProperty() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv.set(iv);
        }
    }
}
