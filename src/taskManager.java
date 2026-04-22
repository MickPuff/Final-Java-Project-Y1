import java.util.*;
import java.time.LocalDate;

// Listener interface for task changes
interface TaskManagerListener {
    void onTasksChanged();
}

class taskManager {
    // Static instance
    private static taskManager instance;
    
    private List<CheckListItem> tasks;
    private List<TaskManagerListener> listeners;

    // private constructor
    private taskManager() {
        System.out.println("Task Manager Created!");
        tasks = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    // Method Access to object
    public static taskManager getInstance() {
        if (instance == null) {
            instance = new taskManager();
        }
        return instance;
    }

    // Register a listener to be notified of changes
    public void addListener(TaskManagerListener listener) {
        listeners.add(listener);
    }

    // Unregister a listener
    public void removeListener(TaskManagerListener listener) {
        listeners.remove(listener);
    }

    // Notify all listeners of changes
    private void notifyListeners() {
        for (TaskManagerListener listener : listeners) {
            listener.onTasksChanged();
        }
    }

    public void addTasks(CheckListItem task) {
        tasks.add(task);
        notifyListeners();
    }

    public void showAllTasks() {
        sortByDueDate();
        for (CheckListItem task: tasks) {
            System.out.println(task.getDescription());
        }
    }

    public List<CheckListItem> showList() {
        sortByDueDate();
        return new ArrayList<>(tasks);
    }

    public void removeTask(CheckListItem task) {
        tasks.remove(task);
        notifyListeners();
    }

    public void sortByDueDate() {
        tasks.sort((a, b) -> {
            boolean aPriority = TaskDecorator.hasDecorator(a, PriorityTask.class);
            boolean bPriority = TaskDecorator.hasDecorator(b, PriorityTask.class);

            if (aPriority && !bPriority) return -1;
            if (!aPriority && bPriority) return 1;

            LocalDate dateA = extractDate(a);
            LocalDate dateB = extractDate(b);

            if (dateA == null && dateB == null) return 0;
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateA.compareTo(dateB);
        });
    }

    private LocalDate extractDate(CheckListItem task) {
        if (task instanceof DueDateTask) {
            return ((DueDateTask) task).getDate();
        }
        if (task instanceof TaskDecorator) {
            return extractDate(((TaskDecorator) task).getWrappedTask());
        }
        return null;
    }
}
