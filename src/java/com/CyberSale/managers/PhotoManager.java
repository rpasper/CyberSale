/*
 * Created by Patrick Abod on 2016.02.14  * 
 * Copyright © 2016 Patrick Abod. All rights reserved. * 
 */
package com.CyberSale.managers;

import com.CyberSale.entitypackage.Item;
import com.CyberSale.entitypackage.ItemPhoto;
import com.CyberSale.entitypackage.Photo;
import com.CyberSale.jsfclassespackage.util.Constants;
import com.CyberSale.sessionbeanpackage.ItemFacade;
import com.CyberSale.sessionbeanpackage.ItemPhotoFacade;
import com.CyberSale.sessionbeanpackage.PhotoFacade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named(value = "photoManager")
@ManagedBean
@SessionScoped
/**
 *
 * @author patrickabod
 * @author Shawn Amjad
 * This manager class is used to handle the uploading of
 * photos (images) for items.
 */
public class PhotoManager {

    // Instance Variables (Properties)
    private UploadedFile file;
    private String message = "";
    
    /**
     * The instance variable 'customerFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject in
     * this instance variable a reference to the @Stateless session bean UserFacade.
     */
    @EJB
    private ItemFacade itemFacade;
    
    /**
     * The instance variable 'ItemPhotoFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject in
     * this instance variable a reference to the @Stateless session bean PhotoFacade.
     */
    @EJB
    private ItemPhotoFacade itemPhotoFacade;

    /**
     * The instance variable 'photoFacade' is annotated with the @EJB annotation.
     * This means that the GlassFish application server, at runtime, will inject in
     * this instance variable a reference to the @Stateless session bean PhotoFacade.
     */
    @EJB
    private PhotoFacade photoFacade;

    // Returns the uploaded file
    public UploadedFile getFile() {
        return file;
    }

    // Obtains the uploaded file
    public void setFile(UploadedFile file) {
        this.file = file;
    }

    // Returns the message
    public String getMessage() {
        return message;
    }

    // Obtains the message
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * "ItemDetail?faces-redirect=true" asks the web browser to display the
     * ItemDetail.xhtml page and update the URL corresponding to that page.
     * @return ItemDeatil.xhtml or nothing
     */

    public String upload() {
        if (file.getSize() != 0) {
            copyFile(file);
            message = "";
            return "ItemDetail?faces-redirect=true";
        } else {
            message = "You need to upload a file first!";
            return "";
        }
    }
    
    /**
     * Cancel the adding of photos
     * @return 
     */
    public String cancel() {
        message = "";
        return "ItemDetail?faces-redirect=true";
    }

    /**
     * Copy file takes the uploaded photo and stores 
     * the image file name in our DB and places
     * a copy of the image in the server file system.
     * @param file the file to copy
     * @return A success or failure message
     */
    public FacesMessage copyFile(UploadedFile file) {
        try {            
            InputStream in = file.getInputstream();
            
            File tempFile = inputStreamToFile(in, file.getFileName());
            in.close();

            FacesMessage resultMsg;

            Integer itemId = (Integer) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get("item_id");

            Item item = itemFacade.findItemById(itemId);

            // Insert photo record into database
            String extension = ".png";

            // create a new photo and insert it into our DB
            Photo photo = new Photo(file.getFileName());
            
            photoFacade.create(photo);
            
            // link the item to the photo and add the ItemPhoto
            // to our relational DB
            ItemPhoto itemPhoto = new ItemPhoto();
            itemPhoto.setItemId(item);
            itemPhoto.setPhotoId(photo);
            
            itemPhotoFacade.create(itemPhoto);
            
            this.file = null;
            
            resultMsg = new FacesMessage("Success!", "File Successfully Uploaded!");
            return resultMsg;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FacesMessage("Upload failure!",
            "There was a problem reading the image file. Please try again with a new photo file.");
    }

    /**
     * Writes the file to the appropriate location on the server file system
     * @param inputStream the bytes of the file from the input stream
     * @param childName the child name of the bytes to write
     * @return the file written to the server
     * @throws IOException 
     */
    private File inputStreamToFile(InputStream inputStream, String childName)
            throws IOException {
        // Read in the series of bytes from the input stream
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        String filePath = Constants.ROOT_DIRECTORY + "/" + (Integer) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get("item_id");
        
        new File(filePath).mkdir();
        
        // Write the series of bytes on file.
        File targetFile = new File(filePath, childName);

        OutputStream outStream;
        outStream = new FileOutputStream(targetFile);
        outStream.write(buffer);
        outStream.close();

        // Save reference to the current image.
        return targetFile;
    }
    
    /**
     * This method is used to handle the click of the file upload
     * from the prime faces upload button on the add item page
     * @param event The event (containing the files)
     */
    public void handleFileUpload(FileUploadEvent event) {
        FacesMessage message = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        this.file = event.getFile();
        upload();
    }
    
    /*
         Get Photo Filename given a Photo ID
    */    
    public String getItemPhotoFilename(int itemID) {
        
        List<Photo> photos = itemPhotoFacade.findPhotosForItem(itemID);
        
        if (photos != null) {
            String path = "/ItemPhotos/" + itemID + "/" + photos.get(0).getFileName();
            String realPath = FacesContext.getCurrentInstance()
                    .getExternalContext().getRealPath(path);
            
            // Check if item photo file actually exists on drive
            if (new File(realPath).exists())
                return path;
            else
                return "/resources/images/default_item_photo.png";
        }
        else
            return "/resources/images/default_item_photo.png";
        
    }
}
