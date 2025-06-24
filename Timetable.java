import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Timetable implements Serializable {
    private static final long serialVersionUID = 1L;
    List<ScheduleSlot> schedule;
    public Timetable() {
        schedule = new ArrayList<>();
    }
    
    public void addSlot(ScheduleSlot slot) {
        schedule.add(slot);
    }
    
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