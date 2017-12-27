/*
by Anthony Stump
Created: 21 Dec 2017
Updated: 22 Dec 2017
based off 
    https://developers.google.com/drive/v3/web/quickstart/java
    https://developers.google.com/api-client-library/java/google-api-java-client/media-upload
    https://developers.google.com/drive/v3/web/folder
*/

package asUtils.Shares;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GDrive {
    
    private static final String APPLICATION_NAME = "asUtils";
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/gDrive");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static Credential authorize() throws IOException {
        
        InputStream in = GDrive.class.getResourceAsStream("client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) {
        
        System.out.println("Google Drive main API client!");
        
    }
    
    public static void deleteFile(List<File> theseItems) throws IOException {
        
        Drive service = getDriveService();
        
        /* JsonBatchCallback<File> callback = new JsonBatchCallback<File>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                System.err.println(e.getMessage());
            }
            
            @Override
            public void onSuccess(File thisFile, HttpHeaders responseHeaders) {
                System.out.println("Deleted: " + thisFile.getId());
            }
        }; */
        
        
        /* BatchRequest batchDelete = service.batch(); */
        for (File item : theseItems) {
            service.files().delete(item.getId()).execute(); //queue(batchDelete, null);
            System.out.println("File "+item.getName()+" deleted!");
        }
        
        /* batchDelete.execute(); */
        System.out.println("Deleted all files successfully!");
        
    }
    
    public static void deleteChildItemsFromFolder(String folderName) throws IOException {
        List<File> children = getFolderListing(folderName);
        deleteFile(children);
    }
    
    public static List<File> getFolderListing(String folderName) throws IOException {
        List<File> childFiles = new ArrayList<>();
        Drive service = getDriveService();
        FileList getParent = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder' and name='"+folderName+"'")
                .setFields("files(id, name)")
                .execute();
                        
        List<File> parent = getParent.getFiles();
        if (parent == null || parent.size() == 0) {
            System.out.println("Parent folder "+folderName+" not found!");
        } else {
            System.out.println("Files:");
            for (File thisParent : parent) {
                String childrenQuery = "mimeType != 'application/vnd.google-apps.folder' and parents in '"+thisParent.getId()+"'";
                Drive.Files.List getChildren = service.files().list().setQ(childrenQuery);
                do {
                        FileList theFileList = getChildren.execute();
                        List<File> theseFiles = theFileList.getFiles();
                        for (File thisFile : theseFiles) {
                            if(thisFile == null || thisFile.size() == 0) {
                                System.out.println("No children under "+folderName+"!");
                            } else {
                                childFiles.add(thisFile);
                            }
                        }
                        getChildren.setPageToken(theFileList.getNextPageToken());
                } while (getChildren.getPageToken() != null && getChildren.getPageToken().length() > 0);
            }
        }
        return childFiles;
    }

    public static void getListing() throws IOException {
        Drive service = getDriveService();
        FileList result = service
                .files()
                .list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files in :");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
    
    public static void uploadFile(java.io.File uploadFile, String mediaType, String parentFolderId) throws IOException {
        Drive drive = getDriveService();
        File fileMetadata = new File();
        fileMetadata.setName(uploadFile.getName()).setParents(Collections.singletonList(parentFolderId));
        FileContent fileContent = new FileContent(mediaType, uploadFile);
        File file = drive.files().create(fileMetadata, fileContent).setFields("id, parents").execute();
        System.out.println("Uploaded successfully! Put "+file.getName()+" ("+file.getId()+") on Drive!");
    }

}
