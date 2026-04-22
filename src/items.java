import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

interface CheckListItem {
    String getDescription();
    String getName();
}

// Concrete Component

class BasicTask implements CheckListItem {
    protected String taskName;
    protected String description;

    public BasicTask(String taskName, String description) {
        this.taskName = taskName;
        this.description = description;
    }

    public String getDescription() {
        return taskName + ": " + description;
    }

    public String getName() {
        return taskName;
    }
}

// Decorator Base Class

abstract class TaskDecorator implements CheckListItem {
    protected CheckListItem task;

    public TaskDecorator(CheckListItem task) {
        this.task = task;
    }

    public CheckListItem getWrappedTask() {
        return task;
    }

    public static boolean hasDecorator(CheckListItem task, Class<?> type) {
        if (type.isInstance(task)) return true;
        if (task instanceof TaskDecorator) {
            return hasDecorator(((TaskDecorator) task).getWrappedTask(), type);
        }
        return false;
    }

    // Helper method to get the base task description (without decorators)
    protected String getBaseDescription(CheckListItem task) {
        if (task instanceof TaskDecorator) {
            return getBaseDescription(((TaskDecorator) task).getWrappedTask());
        }
        return task.getDescription();
    }

    // Helper method to get due date string if task has DueDateTask
    protected String getDueDateString(CheckListItem task) {
        if (task instanceof DueDateTask) {
            DueDateTask dueTask = (DueDateTask) task;
            long daysUntil = dueTask.getDaysUntilDue();
            
            if (daysUntil < 0) {
                return " IS OVERDUE by " + Math.abs(daysUntil) + " day(s)";
            } else if (daysUntil == 0) {
                return " IS DUE TODAY";
            } else if (daysUntil == 1) {
                return " IS DUE TOMORROW";
            } else {
                return " IS DUE ON " + dueTask.getDate().format(DueDateTask.FORMATTER) + " (in " + daysUntil + " days)";
            }
        }
        if (task instanceof TaskDecorator) {
            return getDueDateString(((TaskDecorator) task).getWrappedTask());
        }
        return "";
    }

    // Helper method to check if task has priority
    protected boolean hasPriority(CheckListItem task) {
        return hasDecorator(task, PriorityTask.class);
    }
}

// Concrete Decorator #1 - Priority

class PriorityTask extends TaskDecorator {

    public PriorityTask(CheckListItem task) {
        super(task);
    }

    public String getDescription() {
        String base = getBaseDescription(task);
        StringBuilder result = new StringBuilder(base);
        
        result.append(" IS A PRIORITY");
        
        // Add due date info if it exists
        String dueDateStr = getDueDateString(task);
        if (!dueDateStr.isEmpty()) {
            result.append(dueDateStr);
        }
        
        return result.toString();
    }

    public String getName() {
        return task.getName();
    }
}

// Concrete Decorator #2 - Due Date

class DueDateTask extends TaskDecorator {
    protected LocalDate date;
    public static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public DueDateTask(CheckListItem task, LocalDate date) {
        super(task);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getDaysUntilDue() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public String getDescription() {
        String base = getBaseDescription(task);
        StringBuilder result = new StringBuilder(base);
        
        // Add priority info if it exists
        if (hasPriority(task)) {
            result.append(" IS A PRIORITY");
        }
        
        // Add due date info
        long daysUntil = getDaysUntilDue();
        
        if (daysUntil < 0) {
            result.append(" IS OVERDUE by ").append(Math.abs(daysUntil)).append(" day(s)");
        } else if (daysUntil == 0) {
            result.append(" IS DUE TODAY");
        } else if (daysUntil == 1) {
            result.append(" IS DUE TOMORROW");
        } else {
            result.append(" IS DUE ON ").append(date.format(FORMATTER)).append(" (in ").append(daysUntil).append(" days)");
        }
        
        return result.toString();
    }

    public String getName() {
        return task.getName();
    }
}