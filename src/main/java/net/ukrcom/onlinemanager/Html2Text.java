/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.ukrcom.onlinemanager;

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

/**
 *
 * @author olden
 */
public class Html2Text extends ParserCallback {

    StringBuffer s;

    public Html2Text() {
    }

    public void parse(Reader in) throws IOException {
        s = new StringBuffer();
        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(in, this, Boolean.TRUE);
    }

    @Override
    public void handleText(char[] text, int pos) {
        s.append(text);
        s.append("\n");
    }

    public String getText() {
        return s.toString();
    }
}
