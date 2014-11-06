
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jupar.Downloader;
import jupar.Updater;
import jupar.objects.Modes;
import jupar.objects.Release;
import jupar.parsers.ReleaseXMLParser;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Programmieren
 */
public class AutoUpdater {

    private String pfad = System.getenv("AppData");
    static String updatePath = "http://localhost/inv/";
    private RandomAccessFile raf;
    private String version;
    private String release;

    public AutoUpdater() {
        try {
            File f = new File(pfad + "\\.GyHoInventar\\" + "version");
            if (!f.exists()) {
                File p = new File(pfad + "\\.GyHoInventar");
                p.mkdirs();
                raf = new RandomAccessFile(f, "rw");
                raf.seek(0);
                raf.writeDouble(0.0);
                raf.writeInt(0);
            } else {
                raf = new RandomAccessFile(f, "rw");
                raf.seek(0);
                this.version = "" + raf.readDouble();
                this.release = "" + raf.readInt();
                this.updatecheck();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Runtime.getRuntime().exec("java -jar " + pfad + "\\.GyHoInventar\\GyHoInventar.jar" );
        } catch (IOException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatecheck() {
        Release cRelease = new Release();
        cRelease.setpkgver(this.version);
        cRelease.setPkgrel(this.release);

        ReleaseXMLParser parser = new ReleaseXMLParser();
        try {
            Release current = parser.parse(updatePath + "latest.xml", Modes.URL);
            if (current.compareTo(cRelease) > 0) {

                /**
                 * Download needed files
                 */
                Downloader dl = new Downloader();
                dl.download(updatePath + "files.xml", pfad + "\\.GyHoInventar" + "\\tmp\\", Modes.URL);
                System.out.println("Neue version");
                Updater update = new Updater();
                update.update("update.xml", pfad + "\\.GyHoInventar" + "\\tmp\\", Modes.FILE);
                raf.setLength(0);
                raf.writeDouble(Double.parseDouble(current.getpkgver()));
                raf.writeInt(Integer.parseInt(current.getPkgrel()));
                
                JOptionPane.showMessageDialog(null, "GyHoInventar wurde erfolgreich auf Version " + current.getpkgver() + " " + current.getPkgrel() + " geupdated.");
                /**
                 * Delete tmp directory
                 */
                File tmp = new File(pfad + "\\.GyHoInventar" + "\\tmp\\");
                if (tmp.exists()) {
                    for (File file : tmp.listFiles()) {
                        file.delete();
                    }
                    tmp.delete();
                }
                

            }
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, "The xml wasn't loaded succesfully!\n",
                    "Something went wrong!", JOptionPane.WARNING_MESSAGE);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Files were unable to be read or created successfully!\n"
                    + "Please be sure that you have the right permissions and internet connectivity!",
                    "Something went wrong!", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "IOEXception!",
                    "Something went wrong!", JOptionPane.WARNING_MESSAGE);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(null, "The connection has been lost!\n"
                    + "Please check your internet connectivity!",
                    "Something went wrong!", JOptionPane.WARNING_MESSAGE);
        }
    }

}
