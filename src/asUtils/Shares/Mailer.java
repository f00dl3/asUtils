/* 
by Anthony Stump
Created: 17 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Shares;

import asUtils.Secure.JunkyPrivate;
import java.sql.*;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.json.JSONObject;

public class Mailer {
    
        public static Session getMailSession() {
            
                JunkyPrivate junkyPrivate = new JunkyPrivate();
                JunkyBeans junkyBeans = new JunkyBeans();
                final String username = junkyPrivate.getGmailUser();
		final String password = mailAuth();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", junkyBeans.getGmailSmtpServer());
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
                
                return session;
                
        }
        
        public static Store getMailStore() throws NoSuchProviderException, MessagingException {
            
            JunkyPrivate junkyPrivate = new JunkyPrivate();
            JunkyBeans junkyBeans = new JunkyBeans();
            Session session = getMailSession();
            Store store = session.getStore("imaps");
            store.connect(junkyBeans.getGmailSmtpServer(), junkyPrivate.getGmailUser(), mailAuth());
            return store;
            
        }

	public static String mailAuth() {
		final String getAuthSQL = "SELECT Value FROM JavaSex WHERE Item='gmpa' LIMIT 1;";
		String password = null;
		try (
			Connection conn = MyDBConnector.getMyConnection();
			Statement stmt = conn.createStatement();
			ResultSet resultSetGetAuth = stmt.executeQuery(getAuthSQL)
		) {
			while (resultSetGetAuth.next()) { password = resultSetGetAuth.getString("Value"); }
		} catch (Exception e) { e.printStackTrace(); }
		return password;
	}
        
        public static String mailForSQL() {

            String sqlStatementAppending = "";
            String messageId = "";
            String received = "";
            String fromAddress = "";
            String subject ="";
            String body = "";
            
            JSONObject allMailJSON = readMailToJSON();
            Iterator<?> keys = allMailJSON.keys();
            while(keys.hasNext()) {
                String theKey = (String)keys.next();
                messageId = StumpJunk.jsonSanitize(theKey);
                JSONObject messageObj = (JSONObject) allMailJSON.get(theKey);
                received = StumpJunk.jsonSanitize(messageObj.getString("Received"));
                fromAddress = StumpJunk.jsonSanitize(messageObj.getString("From"));
                subject = StumpJunk.jsonSanitize(messageObj.getString("Subject"));
                body = StumpJunk.jsonSanitize(messageObj.getString("Body"));
                sqlStatementAppending += "('"+messageId+"','"+received+"','"+fromAddress+"','"+subject+"','"+body+"'),";
            }
            
            sqlStatementAppending = (sqlStatementAppending+";").replace(",;",";");
            
            String mailUpdateSQL = "INSERT IGNORE INTO Feeds.Messages ("
                    + "MessageId, Received, FromAddress, Subject, Body"
                    + ") VALUES "+sqlStatementAppending;
            
            return mailUpdateSQL;
            
        }

        public static JSONObject readMailToJSON() {
            JSONObject allMail = new JSONObject();
            try {
                Store store = getMailStore();
                Folder inbox = store.getFolder("inbox");
                inbox.open(Folder.READ_ONLY);
                String messageContent = "";
                int messageCount = inbox.getMessageCount();
                System.out.println("Total messages in Inbox: "+messageCount+"\n");
                Message[] messages = inbox.getMessages();
                for (int i=0; i<messages.length; i++) {
                    Message message = messages[i];
                    String senderAddress = InternetAddress.toString(message.getFrom());
                    JSONObject mailMessage = new JSONObject();
                    String messageKey = message.getReceivedDate().toString()+message.getSubject();
                    mailMessage.put("Received", message.getReceivedDate().toString());
                    mailMessage.put("From", senderAddress);
                    mailMessage.put("Subject", message.getSubject());
                    if(message.getContent() instanceof Multipart) {
                        Multipart multipart = (Multipart) message.getContent();
                        for(int j=0; j < multipart.getCount(); j++) {
                            BodyPart bodyPart = multipart.getBodyPart(j);
                            String disposition = bodyPart.getDisposition();
                            if(disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                                System.out.println("Mail has an attachment");
                                DataHandler handler = bodyPart.getDataHandler();
                                System.out.println("File name: "+handler.getName());
                            } else {
                                messageContent += bodyPart.getContent().toString();
                            }
                        }
                    } else {
                        messageContent = message.getContent().toString();
                    }
                    mailMessage.put("Body", messageContent);
                    allMail.put(messageKey, mailMessage);
                }
                inbox.close(true);
                store.close();
            } catch (NoSuchProviderException nsp) {
                nsp.printStackTrace();              
            } catch (MessagingException mex) {
                mex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return allMail;
        }
        
	public static void sendMail(String sendTo, String messageSubject, String messageContent, File attachment) {

                JunkyPrivate junkyPrivate = new JunkyPrivate();
                final String username = junkyPrivate.getGmailUser();
                
                Session session = getMailSession();

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sendTo));
			message.setSubject(messageSubject);
                        if(attachment != null) {
                            DataSource source = new FileDataSource(attachment.toString());
                            Multipart multipart = new MimeMultipart();
                            BodyPart messageBodyPart1 = new MimeBodyPart();
                            BodyPart messageBodyPart2 = new MimeBodyPart();
                            messageBodyPart1.setText(messageContent);
                            messageBodyPart2.setDataHandler(new DataHandler(source));
                            messageBodyPart2.setFileName(attachment.toString());
                            multipart.addBodyPart(messageBodyPart1);
                            multipart.addBodyPart(messageBodyPart2);
                            message.setContent(multipart);
                        } else {
                            message.setText(messageContent);
                        }
			Transport.send(message);
			System.out.println(" -> Mail sent!");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                
		System.out.println(junkyBeans.getApplicationName()+".Shares.Mailer -- Java mail class!");
                String mailSQLStatement = mailForSQL();
                System.out.println(mailSQLStatement);

	}

}