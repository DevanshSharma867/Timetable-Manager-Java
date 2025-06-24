# Timetable Management System

A Java Swing-based application for managing academic timetables, supporting both admin and student workflows.

## Features

- **Admin Panel:**
  - Upload and validate a CSV file containing timetable data (courses, instructors, classrooms, etc.).
  - Detects and prevents scheduling conflicts (classroom/instructor double-booking).
  - Saves validated timetable data for student use.

- **Student Panel:**
  - Log in with roll number.
  - Select courses from available options.
  - Create a personal timetable by either:
    - Auto-generating a conflict-free schedule.
    - Manually selecting slots for each course.
  - View and save the generated timetable.

## How to Use

1. **Run the Application:**
   - Compile and run `Main.java` using your preferred Java IDE or command line.

2. **Admin Workflow:**
   - Select **Admin** on the main screen.
   - Upload a CSV file with the following format:
     ```
     Day,Time,CourseCode,CourseName,TotalHours,InstructorName,InstructorID,RoomNumber,Capacity,HasAV,NumComputers
     Monday,9:00,CS101,Intro to CS,3,Dr. Smith,1,101,40,true,20
     ...
     ```
   - The system will validate and save the timetable.

3. **Student Workflow:**
   - Select **Student** on the main screen.
   - Enter your roll number.
   - Select your desired courses.
   - Choose to auto-generate or manually create your timetable.
   - Save your timetable when done.

## Project Structure

- `Main.java`: Contains all source code, including GUI, logic, and data models.
- `timetable.ser`: Serialized file storing the validated master timetable and related data (created after admin upload).

## Requirements

- Java SE 8 or higher
- No external dependencies (uses only standard Java libraries)

## Notes

- All data is stored locally; there is no network or database integration.
- The application uses Java Swing for the GUI.
- For best results, ensure the CSV file is properly formatted and free of conflicts.
