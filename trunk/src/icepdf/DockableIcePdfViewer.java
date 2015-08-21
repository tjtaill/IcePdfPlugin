package icepdf;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.util.PropertiesManager;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DockableIcePdfViewer extends JPanel implements EBComponent, DefaultFocusComponent {

    private JPanel icePdfViewer;
    private View view;
    private SwingController swingController;


    public DockableIcePdfViewer(View view) {
        this.view = view;
        PropertiesManager properties =
                new PropertiesManager(System.getProperties(),
                        ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_FIT, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_SAVE, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITY_OPEN, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_STATUSBAR_VIEWMODE, Boolean.FALSE);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL, Boolean.FALSE);


        try {
            swingController = new SwingController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SwingViewBuilder factory = new SwingViewBuilder(swingController, properties);
        icePdfViewer = factory.buildViewerPanel();
        add(icePdfViewer, BorderLayout.CENTER);
    }

    public void openPdf(Buffer buffer) {
        try {
            String[] parts = buffer.getPath().split("\\.", 2);
            InputStream inputStream = new FileInputStream( new File(parts[0] + "." + "pdf") );
            swingController.openDocument(inputStream, "", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void syncPdfLocation(org.gjt.sp.jedit.textarea.TextArea textArea) {
        int currentLine = textArea.getCaretLine();
        int lastLine = textArea.getLineCount();
        float percent = (float)currentLine / lastLine ;
        Document document = swingController.getDocument();
        int pages = document.getNumberOfPages();
        int currentPage = swingController.getCurrentPageNumber();
        float targetPage = percent * pages;
        int goToPage = (int)Math.floor( targetPage );
        swingController.goToDeltaPage( goToPage - currentPage );
    }

    public void syncTextLocation(org.gjt.sp.jedit.textarea.TextArea textArea) {
        int lines = textArea.getLineCount();
        Document document = swingController.getDocument();
        int pages = document.getNumberOfPages();
        int currentPage = swingController.getCurrentPageNumber();

        float percent = (float) (currentPage + 1)/pages;
        float targetLine = percent * lines;
        int line = (int)Math.floor( targetLine );
        textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
    }

    public void searchSelected(org.gjt.sp.jedit.textarea.TextArea textArea) {
        DocumentSearchController searchController =
                swingController.getDocumentSearchController();
        searchController.clearAllSearchHighlight();
        if ( textArea.getSelectionCount() < 1 )
            return;
        String selectedText = textArea.getSelectedText();
        String[] words = selectedText.split("\\s|\\b");
        ArrayList<String> searchWords = new ArrayList<>();
        for(String word : words ) {
            if (!word.isEmpty())
                searchWords.add(word);

        }

        Document document = swingController.getDocument();
        int currentPage = swingController.getCurrentPageNumber();
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages();
             pageIndex++) {
            try {
                PageText pageText = document.getPageText(pageIndex);
                ArrayList<LineText> pageLines = pageText.getPageLines();
                for( LineText lineText : pageLines ) {
                    java.util.List<WordText> lineWords = lineText.getWords();
                    int matched = 0;

                    for(int i = 0; i < lineWords.size(); i++) {
                        String word = lineWords.get(i).getText();
                        if ( word.isEmpty() || word.matches("\\s+") )
                            continue;
                        for(int j = matched; j < searchWords.size(); j++ ) {
                            if ( lineWords.get(i).getText().equals( searchWords.get(j) ) ) {
                                matched++;
                                break;
                            } else {
                                i += matched; // skip the number of words matched
                                matched = 0;
                                break;
                            }
                        }
                        if ( matched == searchWords.size() ) {
                            swingController.goToDeltaPage(pageIndex - currentPage);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }





    @Override
    public void focusOnDefaultComponent() {
        icePdfViewer.requestFocus();
    }

    @Override
    public void handleMessage(EBMessage ebMessage) {

    }
}
