import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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