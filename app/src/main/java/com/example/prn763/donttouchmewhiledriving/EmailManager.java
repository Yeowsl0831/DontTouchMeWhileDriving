package com.example.prn763.donttouchmewhiledriving;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by PRN763 on 1/31/2018.
 */

public class EmailManager extends AsyncTask<Void, Void, Void>{

    //Declaring Variables
    private Session session;

    //Information to send email
    private String mEmail;
    private String mSubject;
    private String mMessage;

    //Progressdialog to show while sending email
    //private ProgressDialog progressDialog;

    //Class Constructor
    public EmailManager(){
    }


    @Override
    protected Void doInBackground(Void... voids) {
        //Creating properties
        Properties props = new Properties();

        //Configuring properties for gmail
        //If you are not using gmail you may need to change the values
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                                                          //Authenticating the password
                                                          protected PasswordAuthentication getPasswordAuthentication() {
                                                          return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);}});
        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(session);
            //Setting sender address
            mm.setFrom(new InternetAddress(Config.EMAIL));
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(mEmail));
            //Adding subject
            mm.setSubject(mSubject);
            //Adding message
            mm.setText(mMessage);
            //Sending email
            Transport.send(mm);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Showing progress dialog while sending email
        //progressDialog = ProgressDialog.show(context,"Sending message","Please wait...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismissing the progress dialog
        //progressDialog.dismiss();
        //Showing a success message
        //Toast.makeText(context,"Message Sent",Toast.LENGTH_LONG).show();
    }

    public void sendEmail(service_msg_t msg, String email, String subject, String message){
        if(ConfigPredefineEnvironment.getInstance().cpe_enable_email_notification()){
            this.mEmail = email;
            this.mSubject = subject;
            this.mMessage = message;

            //Email manager Executing to send email
            this.execute();
        }
    }
}

class Config {
    public static final String EMAIL ="yslin91@gmail.com";
    public static final String PASSWORD ="047877453";
}


