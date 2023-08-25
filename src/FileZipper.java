import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.*;

public class FileZipper extends JFrame {


    private final JFileChooser jFileChooser = new JFileChooser();
    private static final DefaultListModel defaultListModel = new DefaultListModel() {
        @Override
        public void addElement(Object o) {
            arrayList.add(o);
            super.addElement(((File) o).getName());
        }

        @Override
        public Object get(int index) {
            return arrayList.get(index);
        }

        @Override
        public Object remove(int index) {
            arrayList.remove(index);
            return super.remove(index);
        }

        private final ArrayList<Object> arrayList = new ArrayList<>();
    };
    private final JList<File> jList = new JList<File>(defaultListModel);

    public FileZipper() {

        this.setTitle("File Zipper");
        this.setBounds(275, 300, 250, 250);
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = menuBar.add(new JMenu("File"));
        Action addAction = new CustomAction(
                "Add",
                "Add new file to Archive",
                "ctrl D",
                new ImageIcon("addIcon.png"
                ));
        Action deleteAction = new CustomAction(
                "Delete",
                "Delete selected files from the Archive",
                "ctrl U",
                new ImageIcon("deleteIcon.png"
                ));
        Action zipAction = new CustomAction(
                "Zip",
                "Zip the file",
                "ctrl Z"
        );

        fileMenu.add(addAction);
        fileMenu.add(deleteAction);
        fileMenu.add(zipAction);

        JButton addButton = new JButton(addAction);
        JButton deleteButton = new JButton(deleteAction);
        JButton zipButton = new JButton(zipAction);
        JScrollPane jScrollPane = new JScrollPane(jList);

        jList.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(jScrollPane, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0, Short.MAX_VALUE)
                        .addGroup(
                                layout
                                        .createParallelGroup()
                                        .addComponent(addButton)
                                        .addComponent(deleteButton)
                                        .addComponent(zipButton)
                        )
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(jScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(
                                layout
                                        .createSequentialGroup()
                                        .addComponent(addButton)
                                        .addComponent(deleteButton)
                                        .addGap(5, 40, Short.MAX_VALUE).
                                        addComponent(zipButton))
        );

        this.getContentPane().setLayout(layout);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
    }


    enum CustomActions {
        ADD("Add", "Add new file to Archive", "ctrl D"),
        DELETE("Delete", "Delete selected files from the Archive", "ctrl U"),
        ZIP("Zip", "Zip the file", "ctrl Z");

        final String name;
        final String description;
        final String shortcutKey;

        CustomActions(String name, String description, String shortcutKey) {
            this.name = name;
            this.description = description;
            this.shortcutKey = shortcutKey;
        }
    }

    public class CustomAction extends AbstractAction {
        private final ArrayList<File> pathsList = new ArrayList<>();
        public static final int BUFFER = 1024;

        public CustomAction(String name, String description, String shortcutKey) {
            this.putValue(Action.NAME, name);
            this.putValue(Action.SHORT_DESCRIPTION, description);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcutKey));
        }

        public CustomAction(String name, String description, String shortcutKey, Icon icon) {
            this(name, description, shortcutKey);
            this.putValue(Action.SMALL_ICON, icon);
        }


        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(CustomActions.ADD.name)) {
                addFileToArchive();
            } else if (e.getActionCommand().equals(CustomActions.DELETE.name)) {
                deleteFileFromList();
            } else if (e.getActionCommand().equals(CustomActions.ZIP.name)) {
                createZipArchive();
            }
        }

        private void addFileToArchive() {
            jFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jFileChooser.setMultiSelectionEnabled(true);

            int tmp = jFileChooser.showDialog(rootPane, "Add File To Archive");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                File[] paths = jFileChooser.getSelectedFiles();

                for (File path : paths)
                    if (!isFileRepeating(path.getPath()))
                        defaultListModel.addElement(path);

            }
        }

        private boolean isFileRepeating(String testedFile) {
            for (int i = 0; i < defaultListModel.getSize(); i++)
                if (((File) defaultListModel.get(i)).getPath().equals(testedFile))
                    return true;

            return false;
        }

        private void deleteFileFromList() {
            int[] tmp = jList.getSelectedIndices();

            for (int i = 0; i < tmp.length; i++)
                defaultListModel.remove(tmp[i] - i);
        }

        private void createZipArchive() {
            jFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            jFileChooser.setSelectedFile(new File(
                    System.getProperty("user.dir")
                            + File.separator
                            + "myName.zip"
            ));

            int tmp = jFileChooser.showDialog(rootPane, "compress");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                byte[] tmpData = new byte[BUFFER];
                try {
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(
                            new FileOutputStream(jFileChooser.getSelectedFile()), BUFFER
                    ));

                    for (int i = 0; i < defaultListModel.getSize(); i++) {
                        if (!((File) defaultListModel.get(i)).isDirectory())
                            zipFile(zipOutputStream,
                                    (File) defaultListModel.get(i),
                                    tmpData,
                                    ((File) defaultListModel.get(i)).getPath()
                            );
                        else {
                            printPaths((File) defaultListModel.get(i));

                            for (File o : pathsList)
                                zipFile(zipOutputStream,
                                        o,
                                        tmpData,
                                        ((File) defaultListModel.get(i)).getPath()
                                );

                            pathsList.clear();
                        }

                    }

                    zipOutputStream.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        private void zipFile(ZipOutputStream zOutS, File filePath, byte[] tmpData, String basePath) throws IOException {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(filePath), BUFFER);

            zOutS.putNextEntry(new ZipEntry(
                    filePath.getPath().substring(basePath.lastIndexOf(File.separator) + 1))
            );

            int counter;
            while ((counter = inS.read(tmpData, 0, BUFFER)) != -1)
                zOutS.write(tmpData, 0, counter);

            zOutS.closeEntry();

            inS.close();
        }


        private void printPaths(File pathName) {
            String[] fileNames = pathName.list();

            assert fileNames != null;
            for (String fileName : fileNames) {
                File p = new File(pathName.getPath(), fileName);

                if (p.isFile())
                    pathsList.add(p);

                if (p.isDirectory())
                    printPaths(new File(p.getPath()));

            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FileZipper().setVisible(true));
    }
}
