import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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