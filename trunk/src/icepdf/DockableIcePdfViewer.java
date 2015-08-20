package icepdf;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.View;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import org.gjt.sp.jedit.gui.DefaultFocusComponent;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class DockableIcePdfViewer extends JPanel implements EBComponent, DefaultFocusComponent {

    private JPanel icePdfViewer;
    private View view;
    private SwingController swingController;


    public DockableIcePdfViewer(View view) {
        this.view = view;
        try {
            swingController = new SwingController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingViewBuilder factory = new SwingViewBuilder(swingController);
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

    @Override
    public void focusOnDefaultComponent() {
        icePdfViewer.requestFocus();
    }

    @Override
    public void handleMessage(EBMessage ebMessage) {

    }
}
