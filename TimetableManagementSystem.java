import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

// Import modularized classes
import Classroom;
import Course;
import Instructor;
import ScheduleSlot;
import Timetable;

public class TimetableManagementSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Timetable masterTimetable;
    private Map<String, Student> students; // rollNumber -> Student

    public TimetableManagementSystem() {
        setTitle("Timetable Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        students = new HashMap<>();
        loadMasterTimetable();

        mainPanel.add(createWelcomePanel(), "welcome");
        mainPanel.add(createAdminPanel(), "admin");
        mainPanel.add(createStudentLoginPanel(), "studentLogin");
        // Student workflow panels are added dynamically after login

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton adminBtn = new JButton("Admin");
        JButton studentBtn = new JButton("Student");

        adminBtn.addActionListener(e -> cardLayout.show(mainPanel, "admin"));
        studentBtn.addActionListener(e -> cardLayout.show(mainPanel, "studentLogin"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        panel.add(adminBtn, gbc);
        panel.add(studentBtn, gbc);

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Upload Timetable CSV", SwingConstants.CENTER);
        JButton uploadBtn = new JButton("Upload CSV");
        JButton backBtn = new JButton("Back");

        JPanel btnPanel = new JPanel();
        btnPanel.add(uploadBtn);
        btnPanel.add(backBtn);

        panel.add(label, BorderLayout.NORTH);
        panel.add(btnPanel, BorderLayout.CENTER);

        uploadBtn.addActionListener(e -> handleCSVUpload());
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        return panel;
    }

    private JPanel createStudentLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("Enter Roll Number:");
        JTextField rollField = new JTextField(15);
        JButton loginBtn = new JButton("Login");
        JButton backBtn = new JButton("Back");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(rollField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(loginBtn, gbc);
        gbc.gridx = 1;
        panel.add(backBtn, gbc);

        loginBtn.addActionListener(e -> {
            String roll = rollField.getText().trim();
            if (roll.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a roll number.");
                return;
            }
            Student student = students.computeIfAbsent(roll, Student::new);
            showStudentCourseSelection(student);
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        return panel;
    }

    private void showStudentCourseSelection(Student student) {
        if (masterTimetable == null) {
            JOptionPane.showMessageDialog(this, "No master timetable loaded. Please contact admin.");
            cardLayout.show(mainPanel, "welcome");
            return;
        }
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Select Courses", SwingConstants.CENTER);

        DefaultListModel<Course> courseListModel = new DefaultListModel<>();
        for (Course c : masterTimetable.getCourses()) {
            courseListModel.addElement(c);
        }
        JList<Course> courseJList = new JList<>(courseListModel);
        courseJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton autoBtn = new JButton("Auto-generate Timetable");
        JButton manualBtn = new JButton("Manual Selection");
        JButton backBtn = new JButton("Back");

        JPanel btnPanel = new JPanel();
        btnPanel.add(autoBtn);
        btnPanel.add(manualBtn);
        btnPanel.add(backBtn);

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(courseJList), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        autoBtn.addActionListener(e -> {
            List<Course> selected = courseJList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one course.");
                return;
            }
            student.setSelectedCourses(selected);
            boolean success = student.autoGenerateTimetable(masterTimetable);
            if (success) {
                showStudentTimetable(student);
            } else {
                JOptionPane.showMessageDialog(this, "Could not generate a conflict-free timetable.");
            }
        });

        manualBtn.addActionListener(e -> {
            List<Course> selected = courseJList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one course.");
                return;
            }
            student.setSelectedCourses(selected);
            showManualSelectionPanel(student);
        });

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "studentLogin"));

        mainPanel.add(panel, "studentCourseSelect");
        cardLayout.show(mainPanel, "studentCourseSelect");
    }

    private void showManualSelectionPanel(Student student) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Manual Slot Selection", SwingConstants.CENTER);

        JPanel slotsPanel = new JPanel();
        slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
        Map<Course, JComboBox<ScheduleSlot>> slotSelectors = new HashMap<>();

        for (Course c : student.getSelectedCourses()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(new JLabel(c.courseCode + ": "));
            JComboBox<ScheduleSlot> slotBox = new JComboBox<>(c.scheduleSlots.toArray(new ScheduleSlot[0]));
            row.add(slotBox);
            slotsPanel.add(row);
            slotSelectors.put(c, slotBox);
        }

        JButton confirmBtn = new JButton("Confirm");
        JButton backBtn = new JButton("Back");
        JPanel btnPanel = new JPanel();
        btnPanel.add(confirmBtn);
        btnPanel.add(backBtn);

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(slotsPanel), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        confirmBtn.addActionListener(e -> {
            List<ScheduleSlot> chosenSlots = new ArrayList<>();
            for (Map.Entry<Course, JComboBox<ScheduleSlot>> entry : slotSelectors.entrySet()) {
                ScheduleSlot slot = (ScheduleSlot) entry.getValue().getSelectedItem();
                if (slot != null) {
                    chosenSlots.add(slot);
                }
            }
            if (Student.hasConflicts(chosenSlots)) {
                JOptionPane.showMessageDialog(this, "Selected slots have conflicts.");
            } else {
                student.setManualTimetable(chosenSlots);
                showStudentTimetable(student);
            }
        });

        backBtn.addActionListener(e -> showStudentCourseSelection(student));

        mainPanel.add(panel, "manualSelection");
        cardLayout.show(mainPanel, "manualSelection");
    }

    private void showStudentTimetable(Student student) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Your Timetable", SwingConstants.CENTER);

        JTextArea timetableArea = new JTextArea(20, 50);
        timetableArea.setEditable(false);
        timetableArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        timetableArea.setText(student.getTimetableString());

        JButton saveBtn = new JButton("Save Timetable");
        JButton backBtn = new JButton("Back");

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn);
        btnPanel.add(backBtn);

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(timetableArea), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Timetable");
            int ret = fc.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try (PrintWriter pw = new PrintWriter(file)) {
                    pw.print(student.getTimetableString());
                    JOptionPane.showMessageDialog(this, "Timetable saved.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving file.");
                }
            }
        });

        backBtn.addActionListener(e -> showStudentCourseSelection(student));

        mainPanel.add(panel, "studentTimetable");
        cardLayout.show(mainPanel, "studentTimetable");
    }

    private void handleCSVUpload() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                Timetable timetable = Timetable.fromCSV(file);
                if (timetable.hasConflicts()) {
                    JOptionPane.showMessageDialog(this, "Timetable has conflicts. Please fix and re-upload.");
                } else {
                    masterTimetable = timetable;
                    saveMasterTimetable();
                    JOptionPane.showMessageDialog(this, "Timetable uploaded and saved successfully.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV: " + ex.getMessage());
            }
        }
    }

    private void saveMasterTimetable() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("timetable.ser"))) {
            oos.writeObject(masterTimetable);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving timetable.");
        }
    }

    private void loadMasterTimetable() {
        File f = new File("timetable.ser");
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                masterTimetable = (Timetable) ois.readObject();
            } catch (Exception e) {
                masterTimetable = null;
            }
        }
    }
}