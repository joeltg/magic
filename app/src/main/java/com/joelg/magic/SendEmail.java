package com.joelg.magic;

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
 * Created by Joel on 4/6/2015.
 */
public class SendEmail extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        main(null);
        return "SUCCESS";
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {

    }
    public static void main(String[] args) {

        final String username = "jtg003";
        final String password = "comotellamas";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");

        System.out.println("Trying to send...");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        System.out.println("Continuing...");

        try {
            System.out.println("Well");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("jtg003@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("jtg003@gmail.com"));
            message.setSubject("Testing Subject");
            message.setText("Dear Mail Crawler,"
                    + "\n\n No spam to my email, please!");
            System.out.println("Hi");
            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            System.out.println("MessagingException: " + e.toString());
        }
    }
}