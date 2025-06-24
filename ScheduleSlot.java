import java.io.Serializable;

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