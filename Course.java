import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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