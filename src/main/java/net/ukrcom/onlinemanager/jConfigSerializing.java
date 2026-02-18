/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
 */
/**
 *
 * @author olden
 */
public class jConfigSerializing {

    public String fileName = "onlinemanager.config.bin";
    public String xmlName = "onlinemanager.config.xml";

    public void save(jConfig config) throws FileNotFoundException,
                                            IOException {
        /*
        try (FileOutputStream fileOut = new FileOutputStream(this.fileName);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(config);
        }
        
        //////////////////////
        
        XMLEncoder encoder = null;
        encoder = new XMLEncoder(
                new BufferedOutputStream(
                        new FileOutputStream(this.xmlName)
                )
        );
        encoder.writeObject(config);
        encoder.close();
         */
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.writeValue(new File(this.xmlName), config);
    }

    public jConfig load() throws FileNotFoundException, IOException,
                                 ClassNotFoundException {
        jConfig config;
        /*
        try (FileInputStream fileIn = new FileInputStream(this.fileName);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            config = (jConfig) objectIn.readObject();
        }
        
        //////////////////////
        
        XMLDecoder decoder = null;
        decoder = new XMLDecoder(
                new BufferedInputStream(
                        new FileInputStream(this.xmlName)
                )
        );
        config = (jConfig) decoder.readObject();
         */
        config = new XmlMapper()
                .readValue(
                        new File(this.xmlName),
                        jConfig.class
                );
        return config;
    }
}
