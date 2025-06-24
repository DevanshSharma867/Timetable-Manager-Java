import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;



class TimetableManagementSystem extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Stores the master timetable after admin validation
    private Timetable masterTimetable = new Timetable();
    private List<Course> availableCourses = new ArrayList<>();
    private List<Classroom> classrooms = new ArrayList<>();
    private List<Instructor> instructors = new ArrayList<>();
    private Map<Course, ScheduleSlot> selectedSlots = new HashMap<>();
    
    // Student specific data
    private String studentRollNumber;
    private List<Course> selectedCourses = new ArrayList<>();
    private Timetable studentTimetable = new Timetable();
    
    public TimetableManagementSystem() {
        setTitle("Timetable Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create all panels
        createUserTypePanel();
        createAdminPanel();
        createStudentLoginPanel();
        createCourseSelectionPanel();
        createTimetableOptionsPanel();
        createManualTimetablePanel();
        createAutoTimetablePanel();
        
        add(mainPanel);
        setVisible(true);
    }
    
    private void createUserTypePanel() {
        JPanel userTypePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("Timetable Management System");
        titleLabel.setFont(new Font("Roman", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userTypePanel.add(titleLabel, gbc);
        
        JLabel promptLabel = new JLabel("Select User Type:");
        promptLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        userTypePanel.add(promptLabel, gbc);
        
        JButton adminButton = new JButton("Admin");
        adminButton.setFont(new Font("Arial", Font.BOLD, 16));
        adminButton.addActionListener(e -> cardLayout.show(mainPanel, "adminPanel"));
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        userTypePanel.add(adminButton, gbc);
        
        JButton studentButton = new JButton("Student");
        studentButton.setFont(new Font("Arial", Font.BOLD, 16));
        studentButton.addActionListener(e -> cardLayout.show(mainPanel, "studentLoginPanel"));
        gbc.gridx = 1;
        userTypePanel.add(studentButton, gbc);
        
        mainPanel.add(userTypePanel, "userTypePanel");
    }
    
    private void createAdminPanel() {
        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Admin Panel - Upload Timetable CSV");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        adminPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel instructionLabel = new JLabel("<html>Upload a CSV file with the timetable data.<br>"
                + "Format: Day,Time,CourseCode,CourseName,TotalHours,InstructorName,InstructorID,RoomNumber,Capacity,HasAV,NumComputers</html>");
        centerPanel.add(instructionLabel, gbc);
        
        JButton uploadButton = new JButton("Upload CSV File");
        uploadButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 1;
        centerPanel.add(uploadButton, gbc);
        
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        gbc.gridy = 2;
        centerPanel.add(scrollPane, gbc);
        
        adminPanel.add(centerPanel, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "userTypePanel"));
        adminPanel.add(backButton, BorderLayout.SOUTH);
        
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
                int result = fileChooser.showOpenDialog(adminPanel);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        resultArea.setText("Processing file: " + selectedFile.getName() + "\n");
                        processCSVFile(selectedFile, resultArea);
                    } catch (Exception ex) {
                        resultArea.append("Error: " + ex.getMessage() + "\n");
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        mainPanel.add(adminPanel, "adminPanel");
    }
    
    private void processCSVFile(File file, JTextArea resultArea) {
        try {
            // Clear previous data
            masterTimetable = new Timetable();
            availableCourses.clear();
            classrooms.clear();
            instructors.clear();
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean headerSkipped = false;
            
            // Maps to keep track of already created objects
            Map<String, Course> courseMap = new HashMap<>();
            Map<Integer, Classroom> classroomMap = new HashMap<>();
            Map<Integer, Instructor> instructorMap = new HashMap<>();
            
            resultArea.append("Reading data...\n");
            
            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                
                String[] data = line.split(",");
                if (data.length < 11) {
                    resultArea.append("Invalid line format: " + line + "\n");
                    continue;
                }
                
                // Parse data
                String day = data[0].trim();
                String time = data[1].trim();
                String courseCode = data[2].trim();
                String courseName = data[3].trim();
                int totalHours = Integer.parseInt(data[4].trim());
                String instructorName = data[5].trim();
                int instructorID = Integer.parseInt(data[6].trim());
                int roomNumber = Integer.parseInt(data[7].trim());
                int capacity = Integer.parseInt(data[8].trim());
                boolean hasAV = Boolean.parseBoolean(data[9].trim());
                int numComputers = Integer.parseInt(data[10].trim());
                
                // Get or create Course
                Course course = courseMap.get(courseCode);
                if (course == null) {
                    course = new Course(courseCode, courseName, totalHours);
                    courseMap.put(courseCode, course);
                    availableCourses.add(course);
                }
                
                // Get or create Instructor
                Instructor instructor = instructorMap.get(instructorID);
                if (instructor == null) {
                    instructor = new Instructor(instructorName, instructorID);
                    instructorMap.put(instructorID, instructor);
                    instructors.add(instructor);
                }
                
                // Get or create Classroom
                Classroom classroom = classroomMap.get(roomNumber);
                if (classroom == null) {
                    classroom = new Classroom(roomNumber, capacity, hasAV, numComputers);
                    classroomMap.put(roomNumber, classroom);
                    classrooms.add(classroom);
                }
                
                // Create schedule slot
                ScheduleSlot slot = new ScheduleSlot(day, time, course, instructor, classroom);
                
                // Add slot to various objects
                course.addSchedule(slot);
                instructor.addSlot(slot);
                classroom.addSlot(slot);
                masterTimetable.addSlot(slot);
            }
            
            reader.close();
            
            // Validate the timetable
            boolean isValid = masterTimetable.validateSchedule();
            
            if (isValid) {
                resultArea.append("Timetable is valid! Saved " + masterTimetable.schedule.size() + " schedule slots.\n");
                resultArea.append("Loaded " + availableCourses.size() + " courses.\n");
                resultArea.append("Loaded " + instructors.size() + " instructors.\n");
                resultArea.append("Loaded " + classrooms.size() + " classrooms.\n");
                
                // Save the timetable to a local file
                saveTimetableToFile();
                resultArea.append("Timetable saved locally.\n");
            } else {
                resultArea.append("Timetable is invalid! Please fix conflicts and try again.\n");
            }
            
        } catch (Exception e) {
            resultArea.append("Error processing file: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    private void saveTimetableToFile() {
        try {
            FileOutputStream fileOut = new FileOutputStream("timetable.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(masterTimetable);
            out.writeObject(availableCourses);
            out.writeObject(classrooms);
            out.writeObject(instructors);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTimetableFromFile() {
        try {
            FileInputStream fileIn = new FileInputStream("timetable.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            masterTimetable = (Timetable) in.readObject();
            availableCourses = (List<Course>) in.readObject();
            classrooms = (List<Classroom>) in.readObject();
            instructors = (List<Instructor>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Initialize with default values if file doesn't exist
            masterTimetable = new Timetable();
            availableCourses = new ArrayList<>();
            classrooms = new ArrayList<>();
            instructors = new ArrayList<>();
        }
    }
    
    private void createStudentLoginPanel() {
        JPanel studentLoginPanel = new JPanel(new BorderLayout(10, 10));
        studentLoginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Student Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        studentLoginPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        
        JLabel rollNumberLabel = new JLabel("Enter Roll Number:");
        centerPanel.add(rollNumberLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField rollNumberField = new JTextField(15);
        centerPanel.add(rollNumberField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Login");
        centerPanel.add(loginButton, gbc);
        
        studentLoginPanel.add(centerPanel, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "userTypePanel"));
        studentLoginPanel.add(backButton, BorderLayout.SOUTH);
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentRollNumber = rollNumberField.getText().trim();
                if (studentRollNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(studentLoginPanel, "Please enter a valid roll number", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Load timetable data
                loadTimetableFromFile();
                
                // Check if there are any courses available
                if (availableCourses.isEmpty()) {
                    JOptionPane.showMessageDialog(studentLoginPanel, "No courses available. Please ask admin to upload timetable data first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Reset student selections
                selectedCourses.clear();
                studentTimetable = new Timetable();
                
                // Move to course selection
                updateCourseSelectionPanel();
                cardLayout.show(mainPanel, "courseSelectionPanel");
            }
        });
        
        mainPanel.add(studentLoginPanel, "studentLoginPanel");
    }
    
    private void createCourseSelectionPanel() {
        JPanel courseSelectionPanel = new JPanel(new BorderLayout(10, 10));
        courseSelectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Select Courses");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        courseSelectionPanel.add(titleLabel, BorderLayout.NORTH);
        
        // This panel will be dynamically updated
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        courseSelectionPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "studentLoginPanel"));
        
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {
            if (selectedCourses.isEmpty()) {
                JOptionPane.showMessageDialog(courseSelectionPanel, "Please select at least one course", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cardLayout.show(mainPanel, "timetableOptionsPanel");
        });
        
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        courseSelectionPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(courseSelectionPanel, "courseSelectionPanel");
    }
    
    private void updateCourseSelectionPanel() {
        JPanel courseSelectionPanel = (JPanel) ((JScrollPane) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 4)).getComponent(1)).getViewport().getView();
        courseSelectionPanel.removeAll();
        
        JPanel headerPanel = new JPanel(new GridLayout(1, 4));
        headerPanel.add(new JLabel("Select"));
        headerPanel.add(new JLabel("Course Code"));
        headerPanel.add(new JLabel("Course Name"));
        headerPanel.add(new JLabel("Total Hours"));
        courseSelectionPanel.add(headerPanel);
        
        for (Course course : availableCourses) {
            JPanel coursePanel = new JPanel(new GridLayout(1, 4));
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(selectedCourses.contains(course));
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    selectedCourses.add(course);
                } else {
                    selectedCourses.remove(course);
                }
            });
            
            coursePanel.add(checkBox);
            coursePanel.add(new JLabel(course.courseCode));
            coursePanel.add(new JLabel(course.courseName));
            coursePanel.add(new JLabel(String.valueOf(course.totalHours)));
            
            courseSelectionPanel.add(coursePanel);
        }
        
        courseSelectionPanel.revalidate();
        courseSelectionPanel.repaint();
    }
    
    private void createTimetableOptionsPanel() {
        JPanel timetableOptionsPanel = new JPanel(new BorderLayout(10, 10));
        timetableOptionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Timetable Options");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        timetableOptionsPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 10, 20, 10);
        
        JLabel promptLabel = new JLabel("How would you like to create your timetable?");
        promptLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(promptLabel, gbc);
        
        JButton autoButton = new JButton("Auto-Generate Timetable");
        autoButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 1;
        centerPanel.add(autoButton, gbc);
        
        JButton manualButton = new JButton("Create Timetable Manually");
        manualButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 2;
        centerPanel.add(manualButton, gbc);
        
        timetableOptionsPanel.add(centerPanel, BorderLayout.CENTER);
        
        JButton backButton = new JButton("Back to Course Selection");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "courseSelectionPanel"));
        timetableOptionsPanel.add(backButton, BorderLayout.SOUTH);
        
        autoButton.addActionListener(e -> {
            generateAutoTimetable();
            updateAutoTimetablePanel();
            cardLayout.show(mainPanel, "autoTimetablePanel");
        });
        
        manualButton.addActionListener(e -> {
            updateManualTimetablePanel();
            cardLayout.show(mainPanel, "manualTimetablePanel");
        });
        
        mainPanel.add(timetableOptionsPanel, "timetableOptionsPanel");
    }
    
    private void createManualTimetablePanel() {
        JPanel manualTimetablePanel = new JPanel(new BorderLayout(10, 10));
        manualTimetablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Create Timetable Manually");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        manualTimetablePanel.add(titleLabel, BorderLayout.NORTH);
        
        // This panel will be dynamically updated
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        manualTimetablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "timetableOptionsPanel"));
        
        JButton saveButton = new JButton("Save Timetable");
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(manualTimetablePanel, 
                "Timetable saved for student " + studentRollNumber, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "userTypePanel");
        });
        
        buttonPanel.add(backButton);
        buttonPanel.add(saveButton);
        manualTimetablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(manualTimetablePanel, "manualTimetablePanel");
    }
    
    private void updateManualTimetablePanel() {
        JPanel manualPanel = (JPanel) ((JScrollPane) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 2)).getComponent(1)).getViewport().getView();
        manualPanel.removeAll();
        
        // Create days and times
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] times = {"9:00", "10:00", "11:00", "12:00","13:00", "14:00", "15:00", "16:00","17:00"};
        
        // Create the timetable grid
        JPanel timetableGrid = new JPanel(new GridLayout(times.length + 1, days.length + 1));
        
        // Add empty corner cell
        timetableGrid.add(new JLabel(""));
        
        // Add day headers
        for (String day : days) {
            timetableGrid.add(new JLabel(day, JLabel.CENTER));
        }
        
        studentTimetable = new Timetable();
        
        // Add time rows with course selection combos
        for (String time : times) {
            timetableGrid.add(new JLabel(time, JLabel.CENTER));
            
            for (String day : days) {
                JComboBox<String> courseCombo = new JComboBox<>();
                courseCombo.addItem("None");
                
                for (Course course : selectedCourses) {
                    for (ScheduleSlot slot : course.scheduleSlots) {
                        if (slot.day != null && slot.time != null && slot.day.equals(day) && slot.time.equals(time)) {
                            courseCombo.addItem(course.courseCode + " - " + course.courseName);
                            break;
                        }
                    }
                }
                
                final String currentDay = day;
                final String currentTime = time;
                
                courseCombo.addActionListener(e -> {
                    String selected = (String) courseCombo.getSelectedItem();
                    if (selected != null && !selected.equals("None")) {
                        String courseCode = selected.split(" - ")[0];
                        
                        // Find the course
                        Course selectedCourse = null;
                        for (Course c : selectedCourses) {
                            if (c.courseCode.equals(courseCode)) {
                                selectedCourse = c;
                                break;
                            }
                        }
                        
                        if (selectedCourse != null) {
                            // Check for conflicts
                            boolean hasConflict = false;
                            for (ScheduleSlot slot : studentTimetable.schedule) {
                                if (slot.day.equals(currentDay) && slot.time.equals(currentTime)) {
                                    hasConflict = true;
                                    break;
                                }
                            }
                            
                            if (hasConflict) {
                                JOptionPane.showMessageDialog(manualPanel, 
                                    "You already have a course scheduled at this time!", 
                                    "Schedule Conflict", JOptionPane.ERROR_MESSAGE);
                                courseCombo.setSelectedItem("None");
                            } else {
                                // Find a matching slot from master timetable
                                ScheduleSlot matchingSlot = null;
                                for (ScheduleSlot slot : masterTimetable.schedule) {
                                    if (slot.course == selectedCourse && slot.day.equals(currentDay) && slot.time.equals(currentTime)) {
                                        matchingSlot = slot;
                                        break;
                                    }
                                }
                                
                                if (matchingSlot != null) {
                                    studentTimetable.addSlot(matchingSlot);
                                } else {
                                    // Create a dummy slot if no matching slot exists
                                    // This could happen if the course is not actually scheduled at this time in the master timetable
                                    Instructor dummyInstructor = new Instructor("TBD", 0);
                                    Classroom dummyClassroom = new Classroom(0, 0, false, 0);
                                    ScheduleSlot newSlot = new ScheduleSlot(currentDay, currentTime, selectedCourse, dummyInstructor, dummyClassroom);
                                    studentTimetable.addSlot(newSlot);
                                }
                            }
                        }
                    }
                });
                
                timetableGrid.add(courseCombo);
            }
        }
        
        manualPanel.add(timetableGrid);
        manualPanel.revalidate();
        manualPanel.repaint();
    }
    
    private void createAutoTimetablePanel() {
        JPanel autoTimetablePanel = new JPanel(new BorderLayout(10, 10));
        autoTimetablePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Auto-Generated Timetable");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        autoTimetablePanel.add(titleLabel, BorderLayout.NORTH);
        
        // This panel will be dynamically updated
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        autoTimetablePanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "timetableOptionsPanel"));
        
        JButton regenerateButton = new JButton("Regenerate");
        regenerateButton.addActionListener(e -> {
            regenerateAutoTimetable();
            updateAutoTimetablePanel();
        });
        
        JButton saveButton = new JButton("Save Timetable");
        saveButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(autoTimetablePanel, 
                "Timetable saved for student " + studentRollNumber, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "userTypePanel");
        });
        
        buttonPanel.add(backButton);
        buttonPanel.add(regenerateButton);
        buttonPanel.add(saveButton);
        autoTimetablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(autoTimetablePanel, "autoTimetablePanel");
    }
    
    private void generateAutoTimetable() {
        // Clear previous timetable
        studentTimetable = new Timetable();

        // For each selected course
        for (Course course : selectedCourses) {
            // Find all available slots for this course in the master timetable
            List<ScheduleSlot> availableSlots = new ArrayList<>();
            for (ScheduleSlot slot : masterTimetable.schedule) {
                if (slot.course.courseCode.equals(course.courseCode)) {
                    // Check if this slot conflicts with already scheduled slots
                    boolean conflicts = false;
                    for (ScheduleSlot studentSlot : studentTimetable.schedule) {
                        if (studentSlot.conflictsWith(slot)) {
                            conflicts = true;
                            break;
                        }
                    }
                    
                    if (!conflicts) {
                        availableSlots.add(slot);
                    }
                }
            }
            
            // Add a slot if available
            if (!availableSlots.isEmpty()) {
                // Just pick the first available slot for simplicity
                studentTimetable.addSlot(availableSlots.get(0));
                selectedSlots.put(course, availableSlots.get(0));
            }
        }
    }
    
    
    private void regenerateAutoTimetable() {
        List<Course> tempCourses = new ArrayList<>();
        //go through all the courses in the slots
        for(ScheduleSlot slot : studentTimetable.schedule) {

            //if some course has multiple slots within the week
            if(slot.course.scheduleSlots.size() != 1) {

                //remove that slot and keep note of which course has been removed
                tempCourses.add(slot.course);
            }
        }

        for (Course course : tempCourses) {
            studentTimetable.schedule.remove(selectedSlots.get(course));
        }
        
        boolean finished = false;
        int coursesChanged = 0;
        while(!finished) {
            //go through the tempCourses (courses that have multiple slots)
            for (Course course: tempCourses) {
                //check for conflicts 
                // Find all available slots for this course in the master timetable
                
                boolean slotChanged = false; 
                for (ScheduleSlot slot : masterTimetable.schedule) {
                    if (slot.course.courseCode.equals(course.courseCode)) {
                        // if the slot is different from already selected slot of the same course
                        if(!slot.day.equals(selectedSlots.get(course).day) || !slot.time.equals(selectedSlots.get(course).time) ) {
                        // Check if this slot conflicts with already scheduled slots
                            boolean conflicts = false;
                            for (ScheduleSlot studentSlot : studentTimetable.schedule) {
                                if (studentSlot.conflictsWith(slot)) {
                                    conflicts = true;
                                    break;
                                }
                            }
                    
                            if (!conflicts) {
                                studentTimetable.addSlot(slot);
                                selectedSlots.put(course, slot);
                                coursesChanged++;
                                slotChanged = true;
                                break;
                            }
                        }
                    }
                }   
                if(!slotChanged) {
                    studentTimetable.addSlot(selectedSlots.get(course));
                }

                
            }

            if(coursesChanged == tempCourses.size()) {
                finished = true;
            }

        }
        //empty the tempslots list for next iteration
        tempCourses.clear();
    }
    
    private void updateAutoTimetablePanel() {
        JPanel autoPanel = (JPanel) ((JScrollPane) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1)).getViewport().getView();
        autoPanel.removeAll();
        
        // Create days and times for display
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] times =  {"9:00", "10:00", "11:00", "12:00","13:00", "14:00", "15:00", "16:00","17:00"};
        
        // Create a 2D grid to store course info
        String[][] grid = new String[times.length][days.length];
        
        // Fill grid with course information
        for (ScheduleSlot slot : studentTimetable.schedule) {
            int dayIndex = -1;
            for (int i = 0; i < days.length; i++) {
                if (days[i].equalsIgnoreCase(slot.day)) {
                    dayIndex = i;
                    break;
                }
            }
            
            int timeIndex = -1;
            for (int i = 0; i < times.length; i++) {
                if (times[i].equals(slot.time)) {
                    timeIndex = i;
                    break;
                }
            }
            
            if (dayIndex >= 0 && timeIndex >= 0) {
                grid[timeIndex][dayIndex] = slot.course.courseCode + "\n" + 
                                            slot.course.courseName + "\n" +
                                            "Room: " + slot.classroom.roomNumber + "\n" +
                                            "Instructor: " + slot.instructor.name;
            }
        }
        
        // Create the timetable display
        JPanel timetablePanel = new JPanel(new GridLayout(times.length + 1, days.length + 1));
        
        // Add empty corner cell
        timetablePanel.add(new JLabel(""));
        
        // Add day headers
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            timetablePanel.add(dayLabel);
        }
        
        // Add time rows with course info
        for (int i = 0; i < times.length; i++) {
            JLabel timeLabel = new JLabel(times[i], JLabel.CENTER);
            timeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            timetablePanel.add(timeLabel);
            
            for (int j = 0; j < days.length; j++) {
                JPanel cellPanel = new JPanel(new BorderLayout());
                cellPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                
                if (grid[i][j] != null) {
                    JTextArea courseArea = new JTextArea(grid[i][j]);
                    courseArea.setEditable(false);
                    courseArea.setBackground(new Color(230, 230, 250)); // Light lavender
                    cellPanel.add(courseArea, BorderLayout.CENTER);
                }
                
                timetablePanel.add(cellPanel);
            }
        }
        
        // Add a summary of courses not included in the timetable
        Set<Course> unscheduledCourses = new HashSet<>(selectedCourses);
        for (ScheduleSlot slot : studentTimetable.schedule) {
            unscheduledCourses.remove(slot.course);
        }
        
        if (!unscheduledCourses.isEmpty()) {
            JPanel warningPanel = new JPanel();
            warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
            
            JLabel warningLabel = new JLabel("Warning: The following courses could not be scheduled due to conflicts:");
            warningLabel.setForeground(Color.RED);
            warningPanel.add(warningLabel);
            
            for (Course course : unscheduledCourses) {
                warningPanel.add(new JLabel("• " + course.courseCode + " - " + course.courseName));
            }
            
            autoPanel.add(warningPanel);
        }
        
        autoPanel.add(timetablePanel);
        autoPanel.revalidate();
        autoPanel.repaint();
    }
    
    // Main method to run the application
    public static void main(String[] args) {
        // Make the GUI use the system's look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the application on the EDT
        SwingUtilities.invokeLater(() -> {
            new TimetableManagementSystem();
        });
    }
}

// Need to make these classes Serializable to save to file
class ScheduleSlot implements Serializable {
    private static final long serialVersionUID = 1L;
    String day;
    String time;
    Course course; 
    Instructor instructor; 
    Classroom classroom;
    public ScheduleSlot(String day, String time, Course course, Instructor instructor, Classroom classroom) {
        this.day = day;
        this.time = time;
        this.course = course;
        this.instructor = instructor;
        this.classroom = classroom;
    }
    
    // Returns true if this slot conflicts (same day and time) with another.
    public boolean conflictsWith(ScheduleSlot other) {
        return this.day.equals(other.day) && this.time.equals(other.time);
    }
    
    @Override
    public String toString() {
        return "ScheduleSlot{" +
                "day='" + day + '\'' +
                ", time='" + time + '\'' +
                ", course=" + course.courseCode +
                ", instructor=" + instructor.name +
                ", classroom=" + classroom.roomNumber +
                '}';
    }
}

class Classroom implements Serializable {
    private static final long serialVersionUID = 1L;
    int roomNumber;
    int capacity; 
    boolean hasAV; 
    int numComputers; 
    List<ScheduleSlot> slots;
    public Classroom(int roomNumber, int capacity, boolean hasAV, int numComputers) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.hasAV = hasAV;
        this.numComputers = numComputers;
        this.slots = new ArrayList<>();
    }
    
    // Checks if the classroom is free at the time of 'slot' (same day & time).
    public boolean isAvailable(ScheduleSlot slot) {
        for (ScheduleSlot s : slots) {
            if (s.day.equals(slot.day) && s.time.equals(slot.time)) {
                return false;
            }
        }
        return true;
    }
    
    public void addSlot(ScheduleSlot slot) {
        slots.add(slot);
    }
    
    @Override
    public String toString() {
        return "Classroom{" +
                "roomNumber=" + roomNumber +
                ", capacity=" + capacity +
                '}';
    }
}

class Course implements Serializable {
    private static final long serialVersionUID = 1L;
    String courseCode; 
    String courseName; 
    int totalHours; 
    List<ScheduleSlot> scheduleSlots;   
    public Course(String courseCode, String courseName, int totalHours) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.totalHours = totalHours;
        this.scheduleSlots = new ArrayList<>();
    }
    
    public void addSchedule(ScheduleSlot slot) {
        scheduleSlots.add(slot);
    }
    
    @Override
    public String toString() {
        return "Course{" +
                "courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseCode.equals(course.courseCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
}

class Instructor implements Serializable {
    private static final long serialVersionUID = 1L;
    String name; 
    int instructorID; 
    List<Course> assignedCourses; 
    List<ScheduleSlot> schedule; // records the instructor's bookings
    public Instructor(String name, int instructorID) {
        this.name = name;
        this.instructorID = instructorID;
        this.assignedCourses = new ArrayList<>();
        this.schedule = new ArrayList<>();
    }
    
    public void assignCourse(Course c) {
        assignedCourses.add(c);
    }
    
    // Checks if the instructor is available (i.e. does not have a slot at the same day and time).
    public boolean checkAvailability(ScheduleSlot slot) {
        for (ScheduleSlot s : schedule) {
            if (s.day.equals(slot.day) && s.time.equals(slot.time)) {
                return false;
            }
        }
        return true;
    }
    
    public void addSlot(ScheduleSlot slot) {
        schedule.add(slot);
    }
    
    @Override
    public String toString() {
        return "Instructor{" +
                "name='" + name + '\'' +
                ", instructorID=" + instructorID +
                '}';
    }
}

class Timetable implements Serializable {
    private static final long serialVersionUID = 1L;
    List<ScheduleSlot> schedule;
    public Timetable() {
        schedule = new ArrayList<>();
    }
    
    public void addSlot(ScheduleSlot slot) {
        schedule.add(slot);
    }
    
    // Validates the timetable by checking for overlapping bookings.
    // A conflict occurs if two slots occur at the same day and time with either the same classroom or the same instructor.
    public boolean validateSchedule() {
        for (int i = 0; i < schedule.size(); i++) {
            for (int j = i + 1; j < schedule.size(); j++) {
                ScheduleSlot s1 = schedule.get(i);
                ScheduleSlot s2 = schedule.get(j);
                if (s1.conflictsWith(s2)) {
                    if (s1.classroom.roomNumber == s2.classroom.roomNumber) {
                        System.out.println("Conflict: Classroom " + s1.classroom.roomNumber +
                                " is scheduled twice at " + s1.day + " " + s1.time);
                        return false;
                    }
                    if (s1.instructor.instructorID == s2.instructor.instructorID) {
                        System.out.println("Conflict: Instructor " + s1.instructor.name +
                                " is scheduled twice at " + s1.day + " " + s1.time);
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
    